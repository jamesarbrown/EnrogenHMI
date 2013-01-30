//////////////////////////////////////////////////////////////////////////
//EnrogenHMIView.java
//2010 - James A R Brown
//Released under GPL V2
//////////////////////////////////////////////////////////////////////////
package com.enrogen.hmi;

import com.enrogen.CustomRenderer.tableRenderer;
import com.enrogen.hmi.components.ComponentConstructorBaseComponent;
import com.enrogen.hmi.components.analoguemeter;
import com.enrogen.hmi.components.breaker;
import com.enrogen.hmi.components.busbar;
import com.enrogen.hmi.components.digitalmeter;
import com.enrogen.hmi.components.generator;
import com.enrogen.hmi.components.gridPane;
import com.enrogen.hmi.components.label;
import com.enrogen.hmi.components.sldPane;
import com.enrogen.hmi.components.transformer;
import com.enrogen.hmi.components.transmitbutton;
import com.enrogen.hmi.components.transmittablevalue;
import com.enrogen.logger.EgLogger;
import com.enrogen.sql.SQLCommand;
import com.enrogen.xml.xmlio;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Time;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import org.jdesktop.application.Action;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 * The application's main frame.
 */
public class EnrogenHMIView extends FrameView implements hmi {

    boolean debug = true;

    public EnrogenHMIView(SingleFrameApplication app) {
        super(app);
        initComponents();

        System.setProperty("com.enrogen.sql.debug", "true");
        System.setProperty("com.enrogen.hmi.components.debug", "true");

        //Makesure .modbus2sql is created
        checkHomeDir();

        //Startup the logging
        startEgLogger();
        addMessage("Enrogen HMI Startup");

        //Loads setting.xml for preferences
        firstStart();

        //Loads the display configuartion from display.xml
        loadXML();

        //Adds the edit mouse listener to the tabpane
        addMouseListenerTabbedPane();

        //Setup the alarm table
        initAlarmTable();

        //Adds the edit mouse listener to the main layer
        addListenerLayeredPane();

        //Puts display into Non edit mode
        //Switchdesignmode checks the design checkbox on menubar
        switchDesignMode();

        //Removes the default tab keys from the tabbed pane
        removeKeysJTP();

        //Start the SQL Connection and polling thread
        StartSQL();
        StartSQLThread();

        //Start the repainting thread
        StartRepaintThread();

        //Set first display
        btnOverviewClick();
        try {
            tabPanelMain.setSelectedIndex(1);
        } catch (Exception e) {
            tabPanelMain.setSelectedIndex(0);

        }
        //Push upto Fullscreen
        btn_fullscreen.setSelected(true);
        switchFullScreen();

        //Lock certain things
        UpdateToLoginLevel();

        //setup the login pin spinners
        setupLoginWindow();

        //Hide the login button
        btnlogin.setSelected(false);
        openLoginWindow();

        //Open the TCP Port for polling the backend
        StartTCPThread();


    }
    //////////////////////////////////////////////////////////////////////////
    //Logging
    //////////////////////////////////////////////////////////////////////////
    private Logger Logger;

    private void startEgLogger() {
        EgLogger EgLogger = new EgLogger();
        EgLogger.setFileHandler(LOG_ENROGENHMI_FILENAME, LOG_SIZE_LIMIT, LOG_MAX_FILES);
        EgLogger.setWindowHandler(text_messages);
        EgLogger.initEgLogger();
        Logger = EgLogger.getLogger();
    }

    //////////////////////////////////////////////////////////////////////////
    //Messages (Text Box in GUI)
    //////////////////////////////////////////////////////////////////////////
    public void addMessage(String Message) {
        Logger.log(Level.INFO, Message);
    }

    ///////////////////////////////////////////////////////////////////////////
    //Dialog Windows
    ///////////////////////////////////////////////////////////////////////////
    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = EnrogenHMIApp.getApplication().getMainFrame();
            aboutBox = new EnrogenHMIAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        EnrogenHMIApp.getApplication().show(aboutBox);
    }

    @Action
    public void showPreferencesBox() {
        if (preferencesBox == null) {
            JFrame mainFrame = EnrogenHMIApp.getApplication().getMainFrame();
            preferencesBox = (EnrogenHMIPreferences) new EnrogenHMIPreferences(mainFrame);
            preferencesBox.setLocationRelativeTo(mainFrame);
            preferencesBox.setTitle("Preferences");
            preferencesBox.setModalityType(ModalityType.APPLICATION_MODAL);
        }

        //Insert the values and refresh
        preferencesBox.insertPreferenceValues(createPreferencesHashMap());
        preferencesBox.refreshPreferencesForm();

        EnrogenHMIApp.getApplication().show(preferencesBox);

        //Re read the XML file
        readSettingXML();

        //Restart the threads
        StopRepaintThread();
        StopSQLThread();
        StopSQL();
        StartSQL();
        StartSQLThread();
        StartRepaintThread();

        //Set display
        btnOverviewClick();
        tabPanelMain.setSelectedIndex(1);
    }

    ///////////////////////////////////////////////////////////////////////////
    //FullScreen Switching
    ///////////////////////////////////////////////////////////////////////////
    @Action
    public void switchFullScreen() {
        if (btn_fullscreen.isSelected()) {
            //Hide the menu
            menuBar.setVisible(false);

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            super.getFrame().setBounds(0, 0, screenSize.width, screenSize.height);
            super.getFrame().setExtendedState(Frame.MAXIMIZED_BOTH);
            super.getFrame().dispose();
            super.getFrame().setUndecorated(true);
            super.getFrame().setVisible(true);
        } else {
            //Show the menu
            menuBar.setVisible(true);

            super.getFrame().setExtendedState(Frame.NORMAL);
            super.getFrame().dispose();
            super.getFrame().setUndecorated(false);
            super.getFrame().setVisible(true);
        }
    }
    ///////////////////////////////////////////////////////////////////////////
    //Preferences - Parameters
    ///////////////////////////////////////////////////////////////////////////
    int gridsize = hmi.DEFAULT_GRID_SIZE;
    String siteName = null;
    int LoginLevel = 1;
    //SQL PREF
    String MySQLServerIP = null;
    String MySQLServerPort = null;
    String MySQLUsername = null;
    String MySQLPassword = null;
    String MySQLDatabaseName = null;
    //SMTP PREF
    String SMTPServer = null;
    String SMTPPort = null;
    String SMTPUsername = null;
    String SMTPPassword = null;
    String SMTPIsEnabled = null;
    List SMTPRecipients = new LinkedList();
    List messagesToSend = new LinkedList();

    public HashMap createPreferencesHashMap() {
        HashMap hm = new HashMap();
        hm.put("gridsize", gridsize);
        hm.put("siteName", siteName);
        hm.put("LoginLevel", LoginLevel);

        //SQL
        hm.put("MySQLServerIP", MySQLServerIP);
        hm.put("MySQLServerPort", MySQLServerPort);
        hm.put("MySQLUsername", MySQLUsername);
        hm.put("MySQLPassword", MySQLPassword);
        hm.put("MySQLDatabaseName", MySQLDatabaseName);

        //SMTP
        hm.put("SMTPIsEnabled", SMTPIsEnabled);
        hm.put("SMTPServer", SMTPServer);
        hm.put("SMTPPort", SMTPPort);
        hm.put("SMTPUsername", SMTPUsername);
        hm.put("SMTPPassword", SMTPPassword);
        hm.put("SMTPRecipients", SMTPRecipients);

        return hm;
    }
    //////////////////////////////////////////////////////////////////////////
    //Connect TCP to Backend - Check Alive
    //////////////////////////////////////////////////////////////////////////
    boolean backendAlive = false;

    public void TCPPollBackend() {
        Socket Socket = null;
        DataOutputStream os = null;
        DataInputStream is = null;

        //Open the TCP Connection
        try {
            Socket = new Socket("127.0.0.1", 25237);
            os = new DataOutputStream(Socket.getOutputStream());
            is = new DataInputStream(Socket.getInputStream());
        } catch (UnknownHostException e) {
            System.err.println("Unknown Host : 127.0.0.1");
        } catch (IOException e) {
            System.err.println("Could not open TCP Port");
        }

        //Ping the backend
        if (Socket != null && os != null && is != null) {
            try {
                os.writeBytes("ping\n");
                String responseLine;
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                backendAlive = false;
                while ((responseLine = br.readLine()) != null) {
                    if (responseLine.indexOf("OK") != -1) {
                        backendAlive = true;
                        break;
                    }
                }

                //Close TCP Connection
                os.close();
                is.close();
                Socket.close();
            } catch (UnknownHostException e) {
                System.err.println("Unknown Host : 127.0.0.1");
            } catch (IOException e) {
                System.err.println("Could not open TCP Port");
            }

            if (!backendAlive) {
                String sqlcmd = "INSERT INTO alarmlog SET curdate=curdate(), curtime=curtime(), "
                        + "slavename='PanelPC', alarmdesc='Server Backend not Running', alarmtype='warning', "
                        + "alarmnotifications=1";
                SQLConnection.SQLUpdateCommand(sqlcmd);
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    //First Startup and XML Access
    ////////////////////////////////////////////////////////////////////////////
    private xmlio xmlIO = new xmlio();

    private void checkHomeDir() {
        //Check we have a default directory created and if not create it
        String enrogenHMIDir = hmi.FULL_ENROGEN_HMI_PATH;
        boolean exists = (new File(enrogenHMIDir)).exists();
        if (!exists) {
            System.out.println("No Home directory found");
            System.out.println("Creating : " + enrogenHMIDir);
            boolean success = (new File(hmi.FULL_ENROGEN_HMI_PATH)).mkdir();
        }
    }

    @Action
    public boolean firstStart() {
        //Check we have a default directory created and if not create it
        String enrogenHMIDir = hmi.FULL_ENROGEN_HMI_PATH;
        boolean exists = (new File(enrogenHMIDir)).exists();

        //Create if necessary the setting.xml File
        String SettingsXMLFile = hmi.FULL_SETTING_XML_PATH;
        exists = (new File(SettingsXMLFile)).exists();
        if (!exists) {
            addMessage("No setting.xml File");
            addMessage("Creating Default: " + SettingsXMLFile);

            xmlIO.createNewXmlFile(SettingsXMLFile);
            xmlIO.addRootNode("EnrogenHMIPreferences");
            xmlIO.addSubNode("EnrogenHMIPreferences", "default");
            xmlIO.addSubNode("default", "XMLversion", String.valueOf(hmi.REQUIRED_XML_VERSION));
            xmlIO.addSubNode("EnrogenHMIPreferences", "Preferences");
            xmlIO.addSubNode("Preferences", "GridSize", String.valueOf(hmi.DEFAULT_GRID_SIZE));
            xmlIO.addSubNode("Preferences", "siteName", "My Site");

            //SQL Settings
            xmlIO.addSubNode("EnrogenHMIPreferences", "mysql");
            xmlIO.addSubNode("mysql", "ServerIP", MYSQL_DEFAULT_SERVER);
            xmlIO.addSubNode("mysql", "Port", MYSQL_DEFAULT_PORT);
            xmlIO.addSubNode("mysql", "Username", MYSQL_DEFAULT_USER);
            xmlIO.addSubNode("mysql", "Password", MYSQL_DEFAULT_PASSWORD);
            xmlIO.addSubNode("mysql", "DatabaseName", MYSQL_DEFAULT_DATABASE);

            //SMTP Settings
            xmlIO.addSubNode("EnrogenHMIPreferences", "SMTP");
            xmlIO.addSubNode("SMTP", "IsEnabled", SMTP_ISENABLED);
            xmlIO.addSubNode("SMTP", "Server", SMTP_SERVER);
            xmlIO.addSubNode("SMTP", "Port", SMTP_PORT);
            xmlIO.addSubNode("SMTP", "Username", SMTP_USERNAME);
            xmlIO.addSubNode("SMTP", "Password", SMTP_PASSWORD);

            //Recipients
            xmlIO.addSubNode("EnrogenHMIPreferences", "SMTP_RECIPIENTS");
            xmlIO.addSubNode("SMTP_RECIPIENTS", "Email1", "mail@enrogen.com");

            boolean success = xmlIO.writeXMLFile();
            if (!success) {
                addMessage("Error Creating setting.xml File");
            }
        }

        //read the new created XML
        readSettingXML();

        return true;
    }

    public boolean readSettingXML() {
        String SettingsXMLFile = hmi.FULL_SETTING_XML_PATH;

        addMessage("Reading setting.xml");
        xmlIO.setFileName(SettingsXMLFile);
        xmlIO.parseXmlFile();

        addMessage("Checking setting.xml version");
        String value = xmlIO.readXmlTagValue("default", "XMLversion");
        if (Double.parseDouble(value) < hmi.REQUIRED_XML_VERSION) {
            addMessage("The XML Setting file setting.xml is incorrect version");
            return false;
        }

        text_messages.setText(text_messages.getText().toString() + "\nReading setting.xml");
        System.out.println("Reading XML");
        gridsize = Integer.valueOf(xmlIO.readXmlTagValue("Preferences", "GridSize"));
        siteName = String.valueOf(xmlIO.readXmlTagValue("Preferences", "siteName"));

        //SQL
        MySQLServerIP = String.valueOf(xmlIO.readXmlTagValue("mysql", "ServerIP"));
        MySQLServerPort = String.valueOf(xmlIO.readXmlTagValue("mysql", "Port"));
        MySQLUsername = String.valueOf(xmlIO.readXmlTagValue("mysql", "Username"));
        MySQLPassword = String.valueOf(xmlIO.readXmlTagValue("mysql", "Password"));
        MySQLDatabaseName = String.valueOf(xmlIO.readXmlTagValue("mysql", "DatabaseName"));

        //SMTP
        SMTPIsEnabled = String.valueOf(xmlIO.readXmlTagValue("SMTP", "IsEnabled"));
        SMTPServer = String.valueOf(xmlIO.readXmlTagValue("SMTP", "Server"));
        SMTPPort = String.valueOf(xmlIO.readXmlTagValue("SMTP", "Port"));
        SMTPUsername = String.valueOf(xmlIO.readXmlTagValue("SMTP", "Username"));
        SMTPPassword = String.valueOf(xmlIO.readXmlTagValue("SMTP", "Password"));

        //Read the recipients
        List recipientsTagList = xmlIO.getTagList("SMTP_RECIPIENTS");
        for (int x = 0; x < recipientsTagList.size(); x++) {
            SMTPRecipients.add(String.valueOf(xmlIO.readXmlTagValue("SMTP_RECIPIENTS", recipientsTagList.get(x).toString())));
        }
        return true;
    }
    ///////////////////////////////////////////////////////////////////////////
    //Operator Control
    ///////////////////////////////////////////////////////////////////////////
    //1 - Basic
    //2 - Alarm Reset
    //3 - Design Mode
    //4 - Full Factory - Empty Database Logs

    @Action
    public void setupLoginWindow() {
        //Create the JSpinners
        //Create the model
        SpinnerNumberModel jsmodel1 = new SpinnerNumberModel(0, //init
                0, //Min
                9, //Max
                1); //Step
        js1.setModel(jsmodel1);

        SpinnerNumberModel jsmodel2 = new SpinnerNumberModel(0, //init
                0, //Min
                9, //Max
                1); //Step
        js2.setModel(jsmodel2);

        SpinnerNumberModel jsmodel3 = new SpinnerNumberModel(0, //init
                0, //Min
                9, //Max
                1); //Step
        js3.setModel(jsmodel3);
    }

    public void openLoginWindow() {
        if (btnlogin.isSelected()) {
            pinPanel.setVisible(true);
        } else {
            pinPanel.setVisible(false);
        }
    }

    @Action
    public void btnEnterPin() {
        String pin = js1.getValue().toString()
                + js2.getValue().toString()
                + js3.getValue().toString();

        if (pin.compareTo(LEVEL2_PASSWORD) == 0) {
            LoginLevel = 2;
        } else {
            if (pin.compareTo(LEVEL3_PASSWORD) == 0) {
                LoginLevel = 3;
            } else {
                if (pin.compareTo(LEVEL4_PASSWORD) == 0) {
                    LoginLevel = 4;
                } else {
                    LoginLevel = 1;
                }
            }
        }

        UpdateToLoginLevel();
        if (LoginLevel > 1) {
            startLoginTicker();
        }
    }
    Timer loginTimer = null;

    public void startLoginTicker() {
        if (loginTimer == null) {
            int period = MAX_LOGIN_TIME;
            ActionListener taskPerformer = new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    LoginLevel = 1;
                    loginTimer.stop();
                    UpdateToLoginLevel();
                    js1.setValue(0);
                    js2.setValue(0);
                    js3.setValue(0);
                }
            };

            loginTimer = new Timer(period, taskPerformer);
            loginTimer.setInitialDelay(period);
        }
        loginTimer.start();
    }

    public void UpdateToLoginLevel() {
        if (LoginLevel >= 2) {
            btnAckAlarm.setEnabled(true);
            btnAckAllAlarm.setEnabled(true);
            btnresetAlarm.setEnabled(true);
        } else {
            btnAckAlarm.setEnabled(false);
            btnAckAllAlarm.setEnabled(false);
            btnresetAlarm.setEnabled(false);
        }

        if (LoginLevel >= 3) {
            menu_design.setEnabled(true);
            btn_fullscreen.setVisible(true);
        } else {
            menu_design.setEnabled(false);
            btn_fullscreen.setVisible(false);
        }

        loginLevel.setText("Access Level : " + LoginLevel);
    }
    //////////////////////////////////////////////////////////////////////////
    //System Tickers/Threads
    //////////////////////////////////////////////////////////////////////////
    private Thread sqlthread = null;
    private Thread repaintingThread = null;
    private Thread tcpThread = null;

    public class SQLThread extends Thread { // This method is called when the thread runs

        @Override
        public void run() {
            addMessage("Starting SQL Keep Alive Ticker at : " + SQL_ALIVE_POLL_TICKER + "mSec");
            while (true) {
                try {
                    flashLamps();
                    Thread.sleep(SQL_ALIVE_POLL_TICKER);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // very important
                    break;
                }
            }
        }
    }

    @Action
    public void StartSQLThread() {
        sqlthread = new SQLThread();
        sqlthread.start();
    }

    @Action
    public void StopSQLThread() //throws InterruptedException
    {
        sqlthread.interrupt();
    }

    class RepaintingThread extends Thread { // This method is called when the thread runs

        @Override
        public void run() {
            addMessage("Starting Repainting Ticker at : " + REPAINT_POLL_TICKER + "mSec");
            while (true) {
                try {
                    sqlRefreshLoop();
                    Thread.sleep(REPAINT_POLL_TICKER);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // very important
                    break;
                }
            }
        }
    }

    @Action
    public void StartRepaintThread() {
        //if (repaintingThread == null) {
        repaintingThread = new RepaintingThread();
        repaintingThread.start();
        // }
    }

    @Action
    public void StopRepaintThread() {
        repaintingThread.interrupt();
    }

    class TCPThread extends Thread { // This method is called when the thread runs

        @Override
        public void run() {
            while (true) {
                try {
                    TCPPollBackend();
                    Thread.sleep(REPAINT_POLL_TICKER);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // very important
                    break;
                }
            }
        }
    }

    public void StartTCPThread() {
        //if (repaintingThread == null) {
        tcpThread = new TCPThread();
        tcpThread.start();
        // }
    }

    public void StopTCPThread() {
        tcpThread.interrupt();
    }
    //////////////////////////////////////////////////////////////////////////
    //MySQL Tab
    //////////////////////////////////////////////////////////////////////////
    private SQLCommand SQLConnection;

    //Start and if necessary restart sequence
    private void StartSQL() {
        if (SQLConnection == null) {
            SQLConnection = new SQLCommand();
        }

        //Open the SQL Connection
        SQLConnection.setSQLParams(MySQLServerIP, MySQLUsername, MySQLPassword, MySQLDatabaseName);
        addMessage("Starting SQL Connection");
        SQLConnection.restartSQLConnection();
        addMessage("Starting SQL Keep Alive");
        SQLConnection.StartKeepAlive();
    }

    public void StopSQL() {
        SQLConnection.closeSQLConnection();
    }

    private void flashLamps() {
        if (SQLConnection.isAlive()) {
            //Start Things
            redlamp.setEnabled(false);

            //Flash Lamps
            if (greenlamp.isEnabled()) {
                greenlamp.setEnabled(false);
            } else {
                greenlamp.setEnabled(true);
            }

            //Flash alarm icon if alarms present
            if (AlarmPresent) {
                alarmindicatorpanel.setVisible(true);
                if (alarmicon.isVisible()) {
                    alarmicon.setVisible(false);
                } else {
                    alarmicon.setVisible(true);
                }
            } else {
                alarmindicatorpanel.setVisible(false);
            }

            //Paint the display correctly
            tabPanelMain.setEnabled(true);
            alarmPanel.setEnabled(false);
            faultPanel.setVisible(false);
            btnoverview.setEnabled(true);
            btnlogin.setEnabled(true);

            //if sql was down and is now up
            if (SQLConnection.isRestarted()) {
                SQLConnection.resetRestartedFlag();

                //Now display the main panel
                tabPanelMain.setVisible(true);
            }
        } else {
            //Stop Things
            greenlamp.setEnabled(false);
            try {
            } catch (Exception e) {
            }

            //Flash lamps
            if (redlamp.isEnabled()) {
                redlamp.setEnabled(false);
            } else {
                redlamp.setEnabled(true);
            }

            //Paint the display correctly
            tabPanelMain.setEnabled(false);
            alarmPanel.setEnabled(false);
            tabPanelMain.setVisible(false);
            alarmPanel.setVisible(false);
            faultPanel.setVisible(true);
            btnoverview.setEnabled(false);
            btnlogin.setEnabled(false);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //Component Refresh
    ///////////////////////////////////////////////////////////////////////////
    public void sqlRefreshLoop() {
        if (SQLConnection.isAlive()) {
            //Itterate over our custom components and trigger
            //All components in the displaypane ARE baseComponent class.
            //Only refresh the visible components
            if (tabPanelMain.isVisible()) {
                Component selected = tabPanelMain.getSelectedComponent();
                String selectedName = selected.getName();

                if (selectedName.compareTo("Messages") != 0) {
                    JPanel thisJP = (JPanel) selected;
                    JPanel displayPanel = (JPanel) thisJP.getComponent(0);

                    Component[] displayPanelComponents = displayPanel.getComponents();

                    for (int x = 0; x < displayPanelComponents.length; x++) {
                        ComponentConstructorBaseComponent c = (ComponentConstructorBaseComponent) displayPanelComponents[x];
                        c.sqlDataRefresh(SQLConnection);
                       //Does this just happen anyway?
                        // c.repaint();
                    }
                }
                tabPanelMain.repaint();
                //Paints all components
        /*
                Component[] allComponents = tabPanelMain.getComponents();

                for (int i = 0; i < allComponents.length; i++) {
                //Get the subcomponents IF we don't have the message window
                if (allComponents[i].getName().compareTo("Messages") != 0) {
                //As we have created the tabbed panes... we know they are JPanels
                //so cast away.. and we know the display pane is component 1
                JPanel thisJP = (JPanel) allComponents[i];
                JPanel displayPanel = (JPanel) thisJP.getComponent(1);
                Component[] displayPanelComponents = displayPanel.getComponents();

                for (int x = 0; x < displayPanelComponents.length; x++) {
                ComponentConstructorBaseComponent c = (ComponentConstructorBaseComponent) displayPanelComponents[x];
                c.sqlDataRefresh(SQLConnection);
                c.repaint();
                }
                }
                }*/
            }

            if (alarmPanel.isVisible()) {
                //Paint the alarm table
                refreshAlarmTable();
            } else {
                //If alarm table not in view... check for alarms
                AlarmQuickCheck();

                //If we have a flag to goto alarm page, do so
                if (flick1TimeSetFlag) {
                    btnAlarmClick();
                    flick1TimeSetFlag = false;
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //Design Menu
    ///////////////////////////////////////////////////////////////////////////
    @Action
    public void switchDesignMode() {
        if (checkDesignMode.isSelected()) {
            checkShowGrid.setEnabled(true);
            checkShowGrid.setSelected(true);
            checkSnaptoGrid.setEnabled(true);
            menu_tab.setEnabled(true);
            menu_sld_component.setEnabled(true);
            menu_instrument.setEnabled(true);
            menu_other.setEnabled(true);

            //Enable tooltips
            ToolTipManager.sharedInstance().setEnabled(true);
        } else {
            checkShowGrid.setEnabled(false);
            checkShowGrid.setSelected(false);
            checkSnaptoGrid.setEnabled(false);
            menu_tab.setEnabled(false);
            menu_sld_component.setEnabled(false);
            menu_instrument.setEnabled(false);
            menu_other.setEnabled(false);

            //Disable tooltips
            ToolTipManager.sharedInstance().setEnabled(false);
        }
        paintDisplayPanel();
    }

    ///////////////////////////////////////////////////////////////////////////
    //JLayeredPane Drawing
    ///////////////////////////////////////////////////////////////////////////
    public void addListenerLayeredPane() {
        mainLayeredPane.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent evt) {
                Rectangle r = mainLayeredPane.getBounds();

                //Shrink it a bit
                r.grow(0, -20);

                //Set the inner component bounds
                tabPanelMain.setBounds(r);
                alarmPanel.setBounds(r);

                //redraw them
                tabPanelMain.repaint();
                alarmPanel.repaint();
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    //Painting of DisplayPanel
    ///////////////////////////////////////////////////////////////////////////
    @Action
    public void paintDisplayPanel() {
        //Get all the displayPanes
        JTabbedPane jtp = tabPanelMain;
        Component[] allComponents = tabPanelMain.getComponents();

        //Get the titles of the tabs (get lost in rewrite)
        String[] TabTitles = new String[tabPanelMain.getTabCount()];

        for (int i = 0; i < tabPanelMain.getTabCount(); i++) {
            TabTitles[i] = tabPanelMain.getTitleAt(i).toString();
        }

        //Get the selected tab
        int selectedTab = tabPanelMain.getSelectedIndex();

        for (int i = 0; i < allComponents.length; i++) {
            //Get the subcomponents IF we don't have the message window
            if (allComponents[i].getName().compareTo("Messages") != 0) {
                //As we have created the tabbed panes... we know the are JPanels
                //so cast away.. and we know the Grid pane is comp 0
                JPanel thisJP = (JPanel) allComponents[i];
                JPanel gridpane1 = (JPanel) thisJP.getComponent(1);

                if (!checkShowGrid.isSelected()) {
                    //remove panel
                    try {
                        gridpane1.remove(0);
                    } catch (Exception e) {
                    }
                } else {
                    //OR add panel
                    gridPane gp = new gridPane(2000, 2000, gridsize, gridsize);
                    gp.setSize(2000, 2000);
                    gp.setOpaque(false);
                    gridpane1.add(gp);
                } //Write our new component out
                allComponents[i] = thisJP;
            } //Write back to the TabbedPane
            jtp.addTab(TabTitles[i], allComponents[i]);
        }

        //Write the new tabbed pane out
        tabPanelMain = jtp;

        //Select the original tab
        tabPanelMain.setSelectedIndex(selectedTab);

        //Tell components about snap
        setComponentsToSnap(checkSnaptoGrid.isSelected());

        //Repaint the display pane
        tabPanelMain.repaint();
    }

    public void setComponentsToSnap(boolean snap) {
        //All components in the displaypane ARE baseComponent class.
        Component[] allComponents = tabPanelMain.getComponents();

        for (int i = 0; i < allComponents.length; i++) {
            //Get the subcomponents IF we don't have the message window
            if (allComponents[i].getName().compareTo("Messages") != 0) {
                //As we have created the tabbed panes... we know they are JPanels
                //so cast away.. and we know the display pane is component 1
                JPanel thisJP = (JPanel) allComponents[i];
                JPanel displayPanel = (JPanel) thisJP.getComponent(0);

                Component[] displayPanelComponents = displayPanel.getComponents();

                for (int x = 0; x < displayPanelComponents.length; x++) {
                    ComponentConstructorBaseComponent c = (ComponentConstructorBaseComponent) displayPanelComponents[x];
                    c.setGridSize(gridsize);
                    c.setGridSnap(snap);
                    c.setDraggable(checkDesignMode.isSelected());
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //Saving Layout
    ///////////////////////////////////////////////////////////////////////////
    @Action
    public void save() {
        saveDisplay();
        saveAlarms();
    }

    public void saveDisplay() {
        //Create the storing XML file
        String DisplayXMLFile = FULL_DISPLAY_XML_PATH;
        xmlio xmlIO = new xmlio();
        xmlIO.createNewXmlFile(DisplayXMLFile);

        //Create the base nodes
        xmlIO.addRootNode("EnrogenHMIDisplay");

        //Get the Tabs
        Component[] allTabs = tabPanelMain.getComponents();
        String[] TabTitles = new String[tabPanelMain.getTabCount()];

        for (int i = 0; i < TabTitles.length; i++) {
            if (allTabs[i].getName().compareTo("Messages") != 0) {
                TabTitles[i] = tabPanelMain.getTitleAt(i);
                xmlIO.addSubNode("EnrogenHMIDisplay", "DisplayPanel" + i);
                xmlIO.addValueToNode("DisplayPanel" + i, "DisplayPanelName", TabTitles[i]);

                //Now for this tabtitle get the sub components
                JPanel jp = (JPanel) allTabs[i];
                JPanel displayPane = (JPanel) jp.getComponent(0);

                Component[] subComponents = displayPane.getComponents();

                for (int x = 0; x < subComponents.length; x++) {
                    ComponentConstructorBaseComponent bc = (ComponentConstructorBaseComponent) subComponents[x];
                    HashMap hm = bc.getParametersMap();
                    String ComponentUniqueID = "Component:" + i + ":" + x;
                    xmlIO.addSubNode("DisplayPanel" + i, ComponentUniqueID, "");
                    Set ks = hm.keySet();
                    Object[] keySetList = (Object[]) ks.toArray();

                    //add the subnodes
                    for (int y = 0; y
                            < hm.size(); y++) {
                        String key = (String) keySetList[y];
                        String value = hm.get(key).toString();
                        xmlIO.addSubNode(ComponentUniqueID.toString(), key, value);
                    }
                }
            }
        }
        xmlIO.writeXMLFile();
    }

    public void saveAlarms() {
        //Create the storing XML file
        String AlarmXMLFile = FULL_ALARM_XML_PATH;
        xmlio xmlIO = new xmlio();
        xmlIO.createNewXmlFile(AlarmXMLFile);

        //Create the base nodes
        xmlIO.addRootNode("EnrogenHMIDisplay");
    }

    ///////////////////////////////////////////////////////////////////////////
    //Loading Layout
    ///////////////////////////////////////////////////////////////////////////
    @Action
    public void loadXML() {
        //Create the storing XML file
        String DisplayXMLFile = FULL_DISPLAY_XML_PATH;

        boolean exists = (new File(DisplayXMLFile)).exists();

        if (exists) {
            xmlIO.setFileName(DisplayXMLFile);
            xmlIO.parseXmlFile();

            //Create the tabs
            List tagNames = xmlIO.getTagList("EnrogenHMIDisplay");

            for (int i = 0; i < tagNames.size(); i++) {
                addDisplayPane(xmlIO.getAttribute(tagNames.get(i).toString(), "DisplayPanelName"));
            }

            //Add the respective components
            for (int i = 0; i < tagNames.size(); i++) {
                int DisplayPanelNo = i + 1;
                List components = xmlIO.getTagList("DisplayPanel" + DisplayPanelNo);

                for (int x = 0; x < components.size(); x++) {
                    //For each component construct its hashmap
                    String ComponentUniqueID = "Component:" + DisplayPanelNo + ":" + x;
                    List componentParameterNames = xmlIO.getTagList(ComponentUniqueID);
                    HashMap componentParameters = new HashMap();

                    for (int y = 0; y < componentParameterNames.size(); y++) {
                        String value = xmlIO.readXmlTagValue(ComponentUniqueID, componentParameterNames.get(y).toString());
                        componentParameters.put(componentParameterNames.get(y).toString(), value.toString());
                    }

                    //Select the Tab we are working with and grab its Panel
                    tabPanelMain.setSelectedIndex(i + 1);

                    //Now ID the component
                    String componentType = componentParameters.get("ComponentType").toString();

                    //Now lets see what we have
                    if (componentType.compareTo("breaker") == 0) {
                        addComponentBreaker(componentParameters, ComponentUniqueID);
                    }
                    if (componentType.compareTo("busbar") == 0) {
                        addComponentBusbar(componentParameters, ComponentUniqueID);
                    }
                    if (componentType.compareTo("label") == 0) {
                        addComponentLabel(componentParameters, ComponentUniqueID);
                    }
                    if (componentType.compareTo("generator") == 0) {
                        addComponentGenerator(componentParameters, ComponentUniqueID);
                    }
                    if (componentType.compareTo("transformer") == 0) {
                        addComponentTransformer(componentParameters, ComponentUniqueID);
                    }
                    if (componentType.compareTo("analoguemeter") == 0) {
                        addComponentAnalogueMeter(componentParameters, ComponentUniqueID);
                    }
                    if (componentType.compareTo("digitalmeter") == 0) {
                        addComponentDigitalMeter(componentParameters, ComponentUniqueID);
                    }
                    if (componentType.compareTo("transmittablevalue") == 0) {
                        addComponentTransmittableValue(componentParameters, ComponentUniqueID);
                    }
                    if (componentType.compareTo("transmitbutton") == 0) {
                        addComponentTransmitButton(componentParameters, ComponentUniqueID);
                    }
                    tabPanelMain.repaint();
                }
            }
        }
    }
    ///////////////////////////////////////////////////////////////////////////
    //Adding Mouse Listener to the Tabs
    ///////////////////////////////////////////////////////////////////////////
    String currentTabTitle;
    int currentTabIndex;

    public void addMouseListenerTabbedPane() {
        JTabbedPane tabs = tabPanelMain;
        tabs.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent evt) {
                JTabbedPane tabs = (JTabbedPane) evt.getSource();

                if (checkDesignMode.isSelected()) {
                    //Check that click was on tabbed pane itself an that there is no tab at that location.
                    if (tabs.findComponentAt(evt.getX(), evt.getY()) == tabs
                            && tabs.indexAtLocation(evt.getX(), evt.getY()) != -1) {
                        if (evt.getButton() == evt.BUTTON3) {
                            int x = evt.getX();
                            int y = evt.getY();

                            JPopupMenu jpm = new JPopupMenu("Popup");
                            jpm.setLocation(x, y);
                            Dimension size = new Dimension();
                            //size.setSize(100, 100);

                            currentTabIndex = tabs.indexAtLocation(evt.getX(), evt.getY());
                            currentTabTitle = tabPanelMain.getTitleAt(currentTabIndex);

                            //jpm.setPreferredSize(size);
                            JMenuItem jmi = new JMenuItem("Rename");
                            jmi.addActionListener(new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    openRenameWindow(currentTabTitle);
                                }
                            });

                            jpm.add(jmi);
                            jpm.show(evt.getComponent(), x, y);
                        }
                    }
                }
            }
        });
        tabPanelMain = tabs;
        tabPanelMain.revalidate();
    }

    public void openRenameWindow(String name) {
        //Build the frame
        JFrame jf = new JFrame();
        JScrollPane jsp = new JScrollPane();
        JPanel mainPanel = new JPanel();

        int width = EDITOR_BOX_WIDTH;
        int height = EDITOR_BOX_HEIGHT;
        jf.setSize(width, height);

        //setup the main panel
        mainPanel.setPreferredSize(new Dimension(width, height));
        mainPanel.setBackground(Color.yellow);

        //draw Jpanels with border for each component to be dealt with
        JPanel box = new JPanel();
        Dimension d = new Dimension(EDITOR_BOX_ROWWIDTH, EDITOR_BOX_ROWHEIGHT);
        box.setPreferredSize(d);
        box.setLocation(0, 21);
        box.setLayout(new GridLayout());
        Border etchedBdr = BorderFactory.createEtchedBorder();
        box.setBorder(etchedBdr);

        //Add the labels first
        JLabel jl = new JLabel();
        jl.setText("Name");
        box.add(jl);

        //Add the JtextArea
        JTextArea ja = new JTextArea();
        ja.setPreferredSize(new Dimension(150, 20));
        ja.setText(name);
        box.add(ja);

        mainPanel.add(box);
        //Add the buttons
        JPanel box2 = new JPanel();
        box2.setPreferredSize(d);
        box2.setLocation(0, 42);
        box2.setLayout(new GridLayout());
        box2.setBorder(etchedBdr);

        //Close button
        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Component c = (Component) e.getSource();
                Window w = SwingUtilities.getWindowAncestor(c);
                w.dispose();
            }
        });
        btnClose.setText("Close");
        box2.add(btnClose);

        JButton btnApply = new JButton("Apply");
        btnApply.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //Work down to the boxes
                Component c = (Component) e.getSource();
                Window w = SwingUtilities.getWindowAncestor(c);
                JRootPane jrp = (JRootPane) w.getComponent(0);
                JLayeredPane jlp = (JLayeredPane) jrp.getComponent(1);
                JPanel Panel = (JPanel) jlp.getComponent(0);
                JScrollPane jsp = (JScrollPane) Panel.getComponent(0);
                JViewport jvp = (JViewport) jsp.getComponent(0);
                JPanel mainPanel = (JPanel) jvp.getComponent(0);

                //Now get the values
                for (int y = 0; y
                        < mainPanel.getComponentCount() - 1; y++) {
                    JPanel box = (JPanel) mainPanel.getComponent(y);
                    JLabel jl = (JLabel) box.getComponent(0);
                    Object o = box.getComponent(1);

                    String className = o.getClass().toString();
                    String value = "";

                    if (className.contains("JSpinner")) {
                        JSpinner js = (JSpinner) o;
                        value = js.getValue().toString();
                    }

                    if (className.contains("JLabel")) {
                        JLabel jlab = (JLabel) o;
                        value = jlab.getText().toString();
                    }

                    if (className.contains("JTextArea")) {
                        JTextArea jt = (JTextArea) o;
                        value = jt.getText().toString();
                    }

                    if (className.contains("JComboBox")) {
                        JComboBox jc = (JComboBox) o;
                        value = jc.getSelectedItem().toString();
                    }

                    tabPanelMain.setTitleAt(currentTabIndex, value);
                    w.dispose();
                }
            }
        });
        box2.add(btnApply);
        mainPanel.add(box2);

        //Add a scroll panel and jpanel
        Dimension jspDim = new Dimension(width, height);
        jsp.setPreferredSize(jspDim);
        jsp.setVerticalScrollBarPolicy(jsp.VERTICAL_SCROLLBAR_ALWAYS);
        jsp.setViewportView(mainPanel);

        jf.add(jsp);
        jf.validate();
        jf.setVisible(true);
    }

    @Action
    public void deleteTab() {
        int SelectedTab = tabPanelMain.getSelectedIndex();
        String tabName = tabPanelMain.getTitleAt(SelectedTab);

        if (tabName.compareTo("Messages") != 0) {
            tabPanelMain.remove(SelectedTab);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //Modify Jtabbepane key Listener
    ///////////////////////////////////////////////////////////////////////////
    public void removeKeysJTP() {
        KeyStroke stroke1 = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
        KeyStroke stroke2 = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
        InputMap im = tabPanelMain.getInputMap(JComponent.WHEN_FOCUSED);

        while (im != null) {
            im.remove(stroke1);
            im.remove(stroke2);
            im = im.getParent();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //Switching between onscreen modes
    ///////////////////////////////////////////////////////////////////////////
    @Action
    public void btnOverviewClick() {
        tabPanelMain.setVisible(true);
        alarmPanel.setVisible(false);
        mainPanel.repaint();
    }

    @Action
    public void btnAlarmClick() {
        tabPanelMain.setVisible(false);
        alarmPanel.setVisible(true);
        mainPanel.repaint();
    }
    ///////////////////////////////////////////////////////////////////////////
    //Alarm Log
    ///////////////////////////////////////////////////////////////////////////
    DefaultTableModel aModel = new DefaultTableModel();

    public void initAlarmTable() {
        String[] tableColumnsName = {"Unique ID", "Date", "Time", "SlaveID", "Name", "Alarm", "Type", "Status"};
        aModel.setColumnIdentifiers(tableColumnsName);
        table_alarm.setModel(aModel);
    }

    public void refreshAlarms() {
        String sqlcmd = "SELECT rowid, curdate, curtime, slaveid, slavename, "
                + "alarmdesc, alarmtype, alarmacknowledged, alarmnotifications FROM alarmlog "
                + "ORDER BY rowid DESC;";
        List resultList = SQLConnection.SQLSelectCommand(sqlcmd);

        //Get current selected table row
        int rowSelected = table_alarm.getSelectedRow();

        //If Tablemodel has existing rows delete them
        aModel.setRowCount(0);

        //Create objects to contain the highlighed row info
        List warningRowList = new LinkedList();
        List shutdownRowList = new LinkedList();
        List tripRowList = new LinkedList();
        List toolTipList = new LinkedList();

        //Clear batch commands from SQLBatch
        SQLConnection.ClearBatch();

        //Fill the table
        for (int z = 0; z < resultList.size(); z++) {
            List resultValues = (List) resultList.get(z);
            Object[] objects = resultValues.toArray();

            //Determin hightlighting (Alarm Type)
            String alarmType = (String) objects[6];

            int alarmAcknowledged = (Integer) objects[7];
            if (alarmAcknowledged == 0) {
                if (alarmType.compareTo("Shutdown") == 0) {
                    shutdownRowList.add(z);
                }
                if (alarmType.compareTo("Warning") == 0) {
                    warningRowList.add(z);
                    //System.out.println("adding warning");
                }
                if (alarmType.compareTo("Trip") == 0) {
                    tripRowList.add(z);
                }
            }

            //Rewrite the status object
            Integer status = (Integer) objects[7];
            String statusString = "Acknowledged";

            if (status == 0) {
                statusString = "Un-Acknowledged";
            }
            objects[7] = statusString;

            //Rewrite the date object
            Date date = (Date) objects[1];
            objects[1] = date.toString();

            //Write the table
            aModel.addRow(objects);

            //Email the alarms etc if applicable
            Boolean alarmnotifications = (Boolean) objects[8];

            if (!alarmnotifications) {
                addMessage("There are Alarms to Email, preparing...");
                String message = CreateMessage(objects);
                messagesToSend.add(message);
                String sqlSent = "UPDATE alarmlog SET "
                        + "alarmnotifications=1 WHERE "
                        + "rowid=" + objects[0];
                SQLConnection.AddToBatch(sqlSent);
            }
            //Set Tooltip
            toolTipList.add((String) objects[6]);
        }

        table_alarm.setModel(aModel);

        //Create a custom renderer using com.enrogen.CustomRenderer
        tableRenderer TR = new tableRenderer();
        TR.setTableHighlightON();
        TR.setToolTipDelays(0, 5000);
        TR.setHighLightShutdownRowsList(shutdownRowList);
        TR.setHighLightWarningRowsList(warningRowList);
        TR.setHighLightTripRowsList(tripRowList);
        TR.setToolTipList(toolTipList);
        table_alarm.setDefaultRenderer(Object.class, TR);

        //Select the users previously selected row.
        if (rowSelected >= 0) {
            table_alarm.setRowSelectionInterval(rowSelected, rowSelected);
        }

        // Disable auto resizing
        table_alarm.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        TableColumn col = table_alarm.getColumnModel().getColumn(5);
        col.setPreferredWidth(250);
        TableColumn col1 = table_alarm.getColumnModel().getColumn(6);
        col1.setPreferredWidth(75);

        //update the database with transmitted emails
        SQLConnection.SQLExecuteBatch();
        SQLConnection.ClearBatch();

        //Ensure sending emails does not hinder the GUI
        //Transmit the emails
        SwingUtilities.invokeLater(
                new Runnable() {

                    public void run() {
                        transmitEmails();
                    }
                });
    }
    //Added to stop table flicker
    Runnable r = new Runnable() {

        public void run() {
            refreshAlarms();
        }
    };

    public void refreshAlarmTable() {
        SwingUtilities.invokeLater(r);
    }

    @Action
    public void acknowledgeSelectedAlarm() {
        //Get current selected table row
        int rowSelected = table_alarm.getSelectedRow();

        //Bug out if no row selected
        if (rowSelected == -1) {
            return;
        }

        //get the rowid of the selected row
        int rowid = (Integer) table_alarm.getValueAt(rowSelected, 0);

        String sqlcmd = "UPDATE alarmlog SET alarmacknowledged=1 WHERE "
                + "rowid=" + rowid + ";";
        SQLConnection.SQLUpdateCommand(sqlcmd);

        //try to reset indicator
        AlarmPresent = false;
        AlarmQuickCheck();
    }

    @Action
    public void acknowledgeAllAlarms() {
        String sqlcmd = "UPDATE alarmlog SET alarmacknowledged=1;";
        SQLConnection.SQLUpdateCommand(sqlcmd);

        //reset indicator
        AlarmPresent = false;
        AlarmQuickCheck();
    }

    @Action
    public void resetAllAlarms() {
        acknowledgeAllAlarms();
        String sqlcmd = "UPDATE alarmannunciator SET hmiresetflag=1;";
        SQLConnection.SQLUpdateCommand(sqlcmd);
    }
    ///////////////////////////////////////////////////////////////////////////
    //Alarm Quick Check
    ///////////////////////////////////////////////////////////////////////////
    private boolean AlarmPresent = false;
    private boolean flick1TimeSetFlag = false;
    private Long currAlarmCount = 0L;

    private void AlarmQuickCheck() {
        String sqlcmd = "SELECT count(rowid) FROM alarmlog";
        List resultList = SQLConnection.SQLSelectCommand(sqlcmd);
        List resultValues = (List) resultList.get(0);
        Object[] objects = resultValues.toArray();
        Long count = (Long) objects[0];

        //Init
        if (currAlarmCount == 0L) {
            currAlarmCount = count;
            AlarmPresent = false;
        } else {
            if (count > currAlarmCount) {
                AlarmPresent = true;
                flick1TimeSetFlag = true;
                currAlarmCount = count;
            }
        }

        return;
    }

    ///////////////////////////////////////////////////////////////////////////
    //Email Alarms
    ///////////////////////////////////////////////////////////////////////////
    //See XML section for SMTP variable definitions.
    private class PopupAuthenticator extends Authenticator {

        @Override
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(SMTPUsername.toString(), SMTPPassword.toString());
        }
    }

    public void transmitEmails() {
        for (int y = 0; y
                < SMTPRecipients.size(); y++) {
            for (int x = 0; x
                    < messagesToSend.size(); x++) {
                String message = messagesToSend.get(x).toString();
                Email(siteName + " - Alarm Message", message, SMTPRecipients.get(y).toString());
            }
        }
        messagesToSend.clear();
    }

    public void Email(String subject, String content, String Recipient) {
        //Set these as properties
        Authenticator auth = new PopupAuthenticator();

        Properties props = System.getProperties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", SMTPServer.toString());
        props.setProperty("mail.user", SMTPUsername.toString());
        props.setProperty("mail.password", SMTPPassword.toString());
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.port", SMTPPort.toString());
        Session mailSession = Session.getInstance(props, auth);
        mailSession.setDebug(true);

        try {
            Transport transport = mailSession.getTransport();
            MimeMessage message = new MimeMessage(mailSession);
            message.setSubject(subject.toString());
            message.setFrom(new InternetAddress("no_reply@enrogen.com"));
            message.setContent(content.toString(), "text/html");
            message.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(Recipient));

            transport.connect();
            transport.sendMessage(message,
                    message.getRecipients(Message.RecipientType.TO));
            transport.close();
        } catch (Exception e) {
            addMessage("Email Could Not Be Transmitted");
            addMessage(
                    "------------------------------");
            addMessage(
                    e.toString());
            String sqlcmd = "INSERT INTO alarmlog SET curdate=curdate(), curtime=curtime(), "
                    + "slavename='PanelPC', alarmdesc='Could Not Transmit Alarm via Email', alarmtype='warning', "
                    + "alarmnotifications=1";
            SQLConnection.SQLUpdateCommand(sqlcmd);
        }
    }

    public String CreateMessage(Object[] objects) {
        //Objects contains :-
        //String sqlcmd = "SELECT rowid, curdate, curtime, slaveid, slavename, "
        //        + "alarmdesc, alarmtype, alarmacknowledged, alarmnotifications FROM alarmlog "
        //        + "ORDER BY rowid DESC;";

        Integer rowid = (Integer) objects[0];
        String date = (String) objects[1];
        String time = String.valueOf((Time) objects[2]);
        String slavename = (String) objects[4];
        String alarmtype = (String) objects[6];
        String Description = (String) objects[5];

        //Build the message
        String Message = "<html>";
        Message = Message + "<h2>New Generator Alarm</h2>";
        Message = Message + "Unique Alarm No : " + rowid + "</br>";
        Message = Message + "Date : " + date + "</br>";
        Message = Message + "Time : " + time + "</br>";
        Message = Message + "Equipment Name : " + slavename + "</br>";
        Message = Message + "Alarm Type : " + alarmtype + "</br>";
        Message = Message + "</br>";
        Message = Message + "Description : " + Description + "</br>";
        Message = Message + "</html>";

        return Message;
    }

    ///////////////////////////////////////////////////////////////////////////
    //Adding Components
    ///////////////////////////////////////////////////////////////////////////
    @Action
    public void menu_add_breaker() {
        addComponentBreaker();
    }

    public void addComponentBreaker() {
        breaker breaker = new breaker("Breaker");

        //Set the dimensions
        Dimension d = new Dimension();
        d.setSize(breaker.width, breaker.height);
        breaker.setSize(d);

        if (checkSnaptoGrid.isSelected()) {
            breaker.setGridSize(gridsize);
            breaker.setGridSnap(true);
        }
        breaker.setDraggable(checkDesignMode.isSelected());

        //Find the tab frame
        int selectedTab = tabPanelMain.getSelectedIndex();
        JPanel overviewPanel = (JPanel) tabPanelMain.getComponent(selectedTab);
        JPanel displayPanel = (JPanel) overviewPanel.getComponent(0);

        //Name the breaker
        int ComponentNo = displayPanel.getComponentCount() + 1;
        String name = "Component:" + selectedTab + ":" + ComponentNo;
        breaker.setName(name);

        //Add it
        displayPanel.add(breaker);
    }

    public void addComponentBreaker(HashMap parameters, String Name) {
        breaker breaker = new breaker(parameters);
        breaker.setName(Name);

        //Set the dimensions
        Dimension d = new Dimension();
        d.setSize(breaker.width, breaker.height);
        breaker.setSize(d);

        //Set the position
        Point p = breaker.getStoredPosition();
        breaker.setLocation(p);

        if (checkSnaptoGrid.isSelected()) {
            breaker.setGridSize(gridsize);
            breaker.setGridSnap(true);
        }
        breaker.setDraggable(checkDesignMode.isSelected());

        //Find the tab frame
        int selectedTab = tabPanelMain.getSelectedIndex();
        JPanel overviewPanel = (JPanel) tabPanelMain.getComponent(selectedTab);
        JPanel displayPanel = (JPanel) overviewPanel.getComponent(0);

        displayPanel.add(breaker);
    }

    ///////////////////////////////////////////////////////////////////////////
    //Add SLD Diagram Page
    ///////////////////////////////////////////////////////////////////////////
    @Action
    public void addDisplayPane() {
        addDisplayPane("Display");
    }

    //Overload
    public void addDisplayPane(String TabName) {
        sldPane sld = new sldPane();
        JPanel sldPanel = sld.createSLDPane(TabName);
        tabPanelMain.addTab(TabName, sldPanel);
        tabPanelMain.repaint();
    }

    @Action
    public void moveTabLeft() {
        String[] TabTitles = new String[tabPanelMain.getTabCount()];

        //Lets have a tidy up... name the components same as the tabs
        for (int i = 0; i
                < tabPanelMain.getTabCount(); i++) {
            TabTitles[i] = tabPanelMain.getTitleAt(i).toString();
            tabPanelMain.getComponent(i).setName(TabTitles[i]);
        }

        //What is the selected Tab
        Component thisComponent = tabPanelMain.getSelectedComponent();
        String thisComponentName = thisComponent.getName();

        //Get all the components
        Component[] allComponents = tabPanelMain.getComponents();

        //Find the index of the component selected
        int ComponentPage = 0;
        for (int i = 0; i < allComponents.length; i++) {
            if (allComponents[i].getName().toString().compareTo(thisComponentName) == 0) {
                ComponentPage = i;
            }
        }

        JTabbedPane jtp = tabPanelMain;

        //We always want the Messages Tab to be LHS
        jtp.addTab(TabTitles[0], allComponents[0]);

        //Build the low side of components the same
        for (int i = 1; i <= (ComponentPage - 2); i++) {
            jtp.addTab(TabTitles[i], allComponents[i]);
        }
        //Now move the component we want up one left
        jtp.addTab(TabTitles[ComponentPage], allComponents[ComponentPage]);

        if (ComponentPage > 1) {
            jtp.addTab(TabTitles[ComponentPage - 1], allComponents[ComponentPage - 1]);
        }

        //Now insert the balance of components
        for (int i = ComponentPage + 1; i < allComponents.length; i++) {
            jtp.add(TabTitles[i], allComponents[i]);
        }
        //Write jtp back
        tabPanelMain = jtp;
    }

    @Action
    public void moveTabRight() {
        String[] TabTitles = new String[tabPanelMain.getTabCount()];

        //Lets have a tidy up... name the components same as the tabs
        for (int i = 0; i
                < tabPanelMain.getTabCount(); i++) {
            TabTitles[i] = tabPanelMain.getTitleAt(i).toString();
            tabPanelMain.getComponent(i).setName(TabTitles[i]);
        }

        //What is the selected Tab
        Component thisComponent = tabPanelMain.getSelectedComponent();
        String thisComponentName = thisComponent.getName();

        //Get all the components
        Component[] allComponents = tabPanelMain.getComponents();

        //Find the index of the component selected
        int ComponentPage = 0;

        for (int i = 0; i < allComponents.length; i++) {
            if (allComponents[i].getName().toString().compareTo(thisComponentName) == 0) {
                ComponentPage = i;
            }
        }

        JTabbedPane jtp = tabPanelMain;

        //We always want the Messages Tab to be LHS
        jtp.addTab(TabTitles[0], allComponents[0]);

        //Build the low side of components the same
        for (int i = 1; i
                <= (ComponentPage - 1); i++) {
            jtp.addTab(TabTitles[i], allComponents[i]);
        }
        //Now move the component we want up one right
        if (ComponentPage < allComponents.length - 1) {
            jtp.addTab(TabTitles[ComponentPage + 1], allComponents[ComponentPage + 1]);
        }
        jtp.addTab(TabTitles[ComponentPage], allComponents[ComponentPage]);

        //Now insert the balance of components
        for (int i = ComponentPage + 2; i < allComponents.length; i++) {
            jtp.add(TabTitles[i], allComponents[i]);
        }
        //Write jtp back
        tabPanelMain = jtp;
    }

    ///////////////////////////////////////////////////////////////////////////
    //Adding Components - Busbar
    ///////////////////////////////////////////////////////////////////////////
    @Action
    public void menu_add_busbar() {
        addComponentBusbar();
    }

    public void addComponentBusbar() {
        busbar busbar = new busbar("busbar");

        //Set the dimensions
        Dimension d = new Dimension();
        d.setSize(busbar.width, busbar.height);
        busbar.setSize(d);

        if (checkSnaptoGrid.isSelected()) {
            busbar.setGridSize(gridsize);
            busbar.setGridSnap(true);
        }
        busbar.setDraggable(checkDesignMode.isSelected());

        //Find the tab frame
        int selectedTab = tabPanelMain.getSelectedIndex();
        JPanel overviewPanel = (JPanel) tabPanelMain.getComponent(selectedTab);
        JPanel displayPanel = (JPanel) overviewPanel.getComponent(0);

        //Name the breaker
        int ComponentNo = displayPanel.getComponentCount() + 1;
        String name = "Component:" + selectedTab + ":" + ComponentNo;
        busbar.setName(name);

        //Add it
        displayPanel.add(busbar);
    }

    public void addComponentBusbar(HashMap parameters, String Name) {
        busbar busbar = new busbar(parameters);
        busbar.setName(Name);

        //Set the dimensions
        Dimension d = new Dimension();
        d.setSize(busbar.width, busbar.height);
        busbar.setSize(d);

        //Set the position
        Point p = busbar.getStoredPosition();
        busbar.setLocation(p);

        if (checkSnaptoGrid.isSelected()) {
            busbar.setGridSize(gridsize);
            busbar.setGridSnap(true);
        }
        busbar.setDraggable(checkDesignMode.isSelected());

        //Find the tab frame
        int selectedTab = tabPanelMain.getSelectedIndex();
        JPanel overviewPanel = (JPanel) tabPanelMain.getComponent(selectedTab);
        JPanel displayPanel = (JPanel) overviewPanel.getComponent(0);

        displayPanel.add(busbar);
    }

    ///////////////////////////////////////////////////////////////////////////
    //Adding Components - Label
    ///////////////////////////////////////////////////////////////////////////
    @Action
    public void menu_add_label() {
        addComponentLabel();
    }

    public void addComponentLabel() {
        label label = new label("Text");

        //Set the dimensions
        Dimension d = new Dimension();
        d.setSize(label.width, label.height);
        label.setSize(d);

        if (checkSnaptoGrid.isSelected()) {
            label.setGridSize(gridsize);
            label.setGridSnap(true);
        }
        label.setDraggable(checkDesignMode.isSelected());

        //Find the tab frame
        int selectedTab = tabPanelMain.getSelectedIndex();
        JPanel overviewPanel = (JPanel) tabPanelMain.getComponent(selectedTab);
        JPanel displayPanel = (JPanel) overviewPanel.getComponent(0);

        //Name the breaker
        int ComponentNo = displayPanel.getComponentCount() + 1;
        String name = "Component:" + selectedTab + ":" + ComponentNo;
        label.setName(name);

        //Add it
        displayPanel.add(label);
    }

    public void addComponentLabel(HashMap parameters, String Name) {
        label label = new label(parameters);
        label.setName(Name);

        //Set the dimensions
        Dimension d = new Dimension();
        d.setSize(label.width, label.height);
        label.setSize(d);

        //Set the position
        Point p = label.getStoredPosition();
        label.setLocation(p);

        if (checkSnaptoGrid.isSelected()) {
            label.setGridSize(gridsize);
            label.setGridSnap(true);
        }
        label.setDraggable(checkDesignMode.isSelected());

        //Find the tab frame
        int selectedTab = tabPanelMain.getSelectedIndex();
        JPanel overviewPanel = (JPanel) tabPanelMain.getComponent(selectedTab);
        JPanel displayPanel = (JPanel) overviewPanel.getComponent(0);

        displayPanel.add(label);
    }

    ///////////////////////////////////////////////////////////////////////////
    //Adding Components - Generator
    ///////////////////////////////////////////////////////////////////////////
    @Action
    public void menu_add_generator() {
        addComponentGenerator();
    }

    public void addComponentGenerator() {
        generator generator = new generator("Gen");

        //Set the dimensions
        Dimension d = new Dimension();
        d.setSize(generator.width, generator.height);
        generator.setSize(d);

        if (checkSnaptoGrid.isSelected()) {
            generator.setGridSize(gridsize);
            generator.setGridSnap(true);
        }
        generator.setDraggable(checkDesignMode.isSelected());

        //Find the tab frame
        int selectedTab = tabPanelMain.getSelectedIndex();
        JPanel overviewPanel = (JPanel) tabPanelMain.getComponent(selectedTab);
        JPanel displayPanel = (JPanel) overviewPanel.getComponent(0);

        //Name the generator
        int ComponentNo = displayPanel.getComponentCount() + 1;
        String name = "Component:" + selectedTab + ":" + ComponentNo;
        generator.setName(name);

        //Add it
        displayPanel.add(generator);
    }

    public void addComponentGenerator(HashMap parameters, String Name) {
        generator generator = new generator(parameters);
        generator.setName(Name);

        //Set the dimensions
        Dimension d = new Dimension();
        d.setSize(generator.width, generator.height);
        generator.setSize(d);

        //Set the position
        Point p = generator.getStoredPosition();
        generator.setLocation(p);

        if (checkSnaptoGrid.isSelected()) {
            generator.setGridSize(gridsize);
            generator.setGridSnap(true);
        }
        generator.setDraggable(checkDesignMode.isSelected());

        //Find the tab frame
        int selectedTab = tabPanelMain.getSelectedIndex();
        JPanel overviewPanel = (JPanel) tabPanelMain.getComponent(selectedTab);
        JPanel displayPanel = (JPanel) overviewPanel.getComponent(0);

        displayPanel.add(generator);
    }

    ///////////////////////////////////////////////////////////////////////////
    //Adding Components - Transformer
    ///////////////////////////////////////////////////////////////////////////
    @Action
    public void menu_add_transformer() {
        addComponentTransformer();
    }

    public void addComponentTransformer() {
        transformer transformer = new transformer("Gen");

        //Set the dimensions
        Dimension d = new Dimension();
        d.setSize(transformer.width, transformer.height);
        transformer.setSize(d);

        if (checkSnaptoGrid.isSelected()) {
            transformer.setGridSize(gridsize);
            transformer.setGridSnap(true);
        }
        transformer.setDraggable(checkDesignMode.isSelected());

        //Find the tab frame
        int selectedTab = tabPanelMain.getSelectedIndex();
        JPanel overviewPanel = (JPanel) tabPanelMain.getComponent(selectedTab);
        JPanel displayPanel = (JPanel) overviewPanel.getComponent(0);

        //Name the transformer
        int ComponentNo = displayPanel.getComponentCount() + 1;
        String name = "Component:" + selectedTab + ":" + ComponentNo;
        transformer.setName(name);

        //Add it
        displayPanel.add(transformer);
    }

    public void addComponentTransformer(HashMap parameters, String Name) {
        transformer transformer = new transformer(parameters);
        transformer.setName(Name);

        //Set the dimensions
        Dimension d = new Dimension();
        d.setSize(transformer.width, transformer.height);
        transformer.setSize(d);

        //Set the position
        Point p = transformer.getStoredPosition();
        transformer.setLocation(p);

        if (checkSnaptoGrid.isSelected()) {
            transformer.setGridSize(gridsize);
            transformer.setGridSnap(true);
        }
        transformer.setDraggable(checkDesignMode.isSelected());

        //Find the tab frame
        int selectedTab = tabPanelMain.getSelectedIndex();
        JPanel overviewPanel = (JPanel) tabPanelMain.getComponent(selectedTab);
        JPanel displayPanel = (JPanel) overviewPanel.getComponent(0);

        displayPanel.add(transformer);
    }

    ///////////////////////////////////////////////////////////////////////////
    //Adding Components - AnalogueMeter
    ///////////////////////////////////////////////////////////////////////////
    @Action
    public void menu_add_analoguemeter() {
        addComponentAnalogueMeter();
    }

    public void addComponentAnalogueMeter() {
        analoguemeter analoguemeter = new analoguemeter("Gen");

        //Set the dimensions
        Dimension d = new Dimension();
        d.setSize(analoguemeter.width, analoguemeter.height);
        analoguemeter.setSize(d);

        if (checkSnaptoGrid.isSelected()) {
            analoguemeter.setGridSize(gridsize);
            analoguemeter.setGridSnap(true);
        }
        analoguemeter.setDraggable(checkDesignMode.isSelected());

        //Find the tab frame
        int selectedTab = tabPanelMain.getSelectedIndex();
        JPanel overviewPanel = (JPanel) tabPanelMain.getComponent(selectedTab);
        JPanel displayPanel = (JPanel) overviewPanel.getComponent(0);

        //Name the analoguemeter
        int ComponentNo = displayPanel.getComponentCount() + 1;
        String name = "Component:" + selectedTab + ":" + ComponentNo;
        analoguemeter.setName(name);

        //Add it
        displayPanel.add(analoguemeter);
    }

    public void addComponentAnalogueMeter(HashMap parameters, String Name) {
        analoguemeter analoguemeter = new analoguemeter(parameters);
        analoguemeter.setName(Name);

        //Set the dimensions
        Dimension d = new Dimension();
        d.setSize(analoguemeter.width, analoguemeter.height);
        analoguemeter.setSize(d);

        //Set the position
        Point p = analoguemeter.getStoredPosition();
        analoguemeter.setLocation(p);

        if (checkSnaptoGrid.isSelected()) {
            analoguemeter.setGridSize(gridsize);
            analoguemeter.setGridSnap(true);
        }
        analoguemeter.setDraggable(checkDesignMode.isSelected());

        //Find the tab frame
        int selectedTab = tabPanelMain.getSelectedIndex();
        JPanel overviewPanel = (JPanel) tabPanelMain.getComponent(selectedTab);
        JPanel displayPanel = (JPanel) overviewPanel.getComponent(0);

        displayPanel.add(analoguemeter);
    }

    ///////////////////////////////////////////////////////////////////////////
    //Adding Components - DigitalMeter
    ///////////////////////////////////////////////////////////////////////////
    @Action
    public void menu_add_digitalmeter() {
        addComponentDigitalMeter();
    }

    public void addComponentDigitalMeter() {
        digitalmeter digitalmeter = new digitalmeter("Meter");

        //Set the dimensions
        Dimension d = new Dimension();
        d.setSize(digitalmeter.width, digitalmeter.height);
        digitalmeter.setSize(d);

        if (checkSnaptoGrid.isSelected()) {
            digitalmeter.setGridSize(gridsize);
            digitalmeter.setGridSnap(true);
        }
        digitalmeter.setDraggable(checkDesignMode.isSelected());

        //Find the tab frame
        int selectedTab = tabPanelMain.getSelectedIndex();
        JPanel overviewPanel = (JPanel) tabPanelMain.getComponent(selectedTab);
        JPanel displayPanel = (JPanel) overviewPanel.getComponent(0);

        //Name the analoguemeter
        int ComponentNo = displayPanel.getComponentCount() + 1;
        String name = "Component:" + selectedTab + ":" + ComponentNo;
        digitalmeter.setName(name);

        //Add it
        displayPanel.add(digitalmeter);
    }

    public void addComponentDigitalMeter(HashMap parameters, String Name) {
        digitalmeter digitalmeter = new digitalmeter(parameters);
        digitalmeter.setName(Name);

        //Set the dimensions
        Dimension d = new Dimension();
        d.setSize(digitalmeter.width, digitalmeter.height);
        digitalmeter.setSize(d);

        //Set the position
        Point p = digitalmeter.getStoredPosition();
        digitalmeter.setLocation(p);

        if (checkSnaptoGrid.isSelected()) {
            digitalmeter.setGridSize(gridsize);
            digitalmeter.setGridSnap(true);
        }
        digitalmeter.setDraggable(checkDesignMode.isSelected());

        //Find the tab frame
        int selectedTab = tabPanelMain.getSelectedIndex();
        JPanel overviewPanel = (JPanel) tabPanelMain.getComponent(selectedTab);
        JPanel displayPanel = (JPanel) overviewPanel.getComponent(0);

        displayPanel.add(digitalmeter);
    }

    ///////////////////////////////////////////////////////////////////////////
    //Adding Components - Transmittable Value
    ///////////////////////////////////////////////////////////////////////////
    @Action
    public void menu_add_transmittablevalue() {
        addComponentTransmittableValue();
    }

    public void addComponentTransmittableValue() {
        transmittablevalue transmittablevalue = new transmittablevalue();

        //Set the dimensions
        Dimension d = new Dimension();
        d.setSize(transmittablevalue.width, transmittablevalue.height);
        transmittablevalue.setSize(d);

        if (checkSnaptoGrid.isSelected()) {
            transmittablevalue.setGridSize(gridsize);
            transmittablevalue.setGridSnap(true);
        }
        transmittablevalue.setDraggable(checkDesignMode.isSelected());

        //Find the tab frame
        int selectedTab = tabPanelMain.getSelectedIndex();
        JPanel overviewPanel = (JPanel) tabPanelMain.getComponent(selectedTab);
        JPanel displayPanel = (JPanel) overviewPanel.getComponent(0);

        //Name the component
        int ComponentNo = displayPanel.getComponentCount() + 1;
        String name = "Component:" + selectedTab + ":" + ComponentNo;
        transmittablevalue.setName(name);

        //Add it
        displayPanel.add(transmittablevalue);
    }

    public void addComponentTransmittableValue(HashMap parameters, String Name) {
        transmittablevalue transmittablevalue = new transmittablevalue(parameters);
        transmittablevalue.setName(Name);

        //Set the dimensions
        Dimension d = new Dimension();
        d.setSize(transmittablevalue.width, transmittablevalue.height);
        transmittablevalue.setSize(d);

        //Set the position
        Point p = transmittablevalue.getStoredPosition();
        transmittablevalue.setLocation(p);

        if (checkSnaptoGrid.isSelected()) {
            transmittablevalue.setGridSize(gridsize);
            transmittablevalue.setGridSnap(true);
        }
        transmittablevalue.setDraggable(checkDesignMode.isSelected());

        //Find the tab frame
        int selectedTab = tabPanelMain.getSelectedIndex();
        JPanel overviewPanel = (JPanel) tabPanelMain.getComponent(selectedTab);
        JPanel displayPanel = (JPanel) overviewPanel.getComponent(0);

        displayPanel.add(transmittablevalue);
    }

    ///////////////////////////////////////////////////////////////////////////
    //Adding Components - Transmit Button
    ///////////////////////////////////////////////////////////////////////////
    @Action
    public void menu_add_transmitbutton() {
        addComponentTransmitButton();
    }

    public void addComponentTransmitButton() {
        transmitbutton transmitbutton = new transmitbutton();

        //Set the dimensions
        Dimension d = new Dimension();
        d.setSize(transmitbutton.width, transmitbutton.height);
        transmitbutton.setSize(d);

        if (checkSnaptoGrid.isSelected()) {
            transmitbutton.setGridSize(gridsize);
            transmitbutton.setGridSnap(true);
        }
        transmitbutton.setDraggable(checkDesignMode.isSelected());

        //Find the tab frame
        int selectedTab = tabPanelMain.getSelectedIndex();
        JPanel overviewPanel = (JPanel) tabPanelMain.getComponent(selectedTab);
        JPanel displayPanel = (JPanel) overviewPanel.getComponent(0);

        //Name the component
        int ComponentNo = displayPanel.getComponentCount() + 1;
        String name = "Component:" + selectedTab + ":" + ComponentNo;
        transmitbutton.setName(name);

        //Add it
        displayPanel.add(transmitbutton);
    }

    public void addComponentTransmitButton(HashMap parameters, String Name) {
        transmitbutton transmitbutton = new transmitbutton(parameters);
        transmitbutton.setName(Name);

        //Set the dimensions
        Dimension d = new Dimension();
        d.setSize(transmitbutton.width, transmitbutton.height);
        transmitbutton.setSize(d);

        //Set the position
        Point p = transmitbutton.getStoredPosition();
        transmitbutton.setLocation(p);

        if (checkSnaptoGrid.isSelected()) {
            transmitbutton.setGridSize(gridsize);
            transmitbutton.setGridSnap(true);
        }
        transmitbutton.setDraggable(checkDesignMode.isSelected());

        //Find the tab frame
        int selectedTab = tabPanelMain.getSelectedIndex();
        JPanel overviewPanel = (JPanel) tabPanelMain.getComponent(selectedTab);
        JPanel displayPanel = (JPanel) overviewPanel.getComponent(0);

        displayPanel.add(transmitbutton);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        mainLayeredPane = new javax.swing.JLayeredPane();
        alarmPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        table_alarm = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        btnAckAlarm = new javax.swing.JButton();
        btnAckAllAlarm = new javax.swing.JButton();
        btnresetAlarm = new javax.swing.JButton();
        tabPanelMain = new javax.swing.JTabbedPane();
        Messages = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        text_messages = new javax.swing.JTextArea();
        faultPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        buttonspanel = new javax.swing.JPanel();
        btn_fullscreen = new javax.swing.JToggleButton();
        btnoverview = new javax.swing.JButton();
        redlamp = new javax.swing.JLabel();
        greenlamp = new javax.swing.JLabel();
        pinPanel = new javax.swing.JPanel();
        js1 = new javax.swing.JSpinner();
        js2 = new javax.swing.JSpinner();
        js3 = new javax.swing.JSpinner();
        btnEnterPin = new javax.swing.JButton();
        alarmindicatorpanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        alarmicon = new javax.swing.JLabel();
        loginLevel = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        btnlogin = new javax.swing.JToggleButton();
        btnalarms = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        menu_design = new javax.swing.JMenu();
        checkDesignMode = new javax.swing.JCheckBoxMenuItem();
        checkShowGrid = new javax.swing.JCheckBoxMenuItem();
        checkSnaptoGrid = new javax.swing.JCheckBoxMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        menu_tab = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        menu_sld_component = new javax.swing.JMenu();
        menu_add_breaker = new javax.swing.JMenuItem();
        menu_add_busbar = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenuItem8 = new javax.swing.JMenuItem();
        menu_instrument = new javax.swing.JMenu();
        jMenuItem9 = new javax.swing.JMenuItem();
        jMenuItem10 = new javax.swing.JMenuItem();
        menu_other = new javax.swing.JMenu();
        menu_add_text = new javax.swing.JMenuItem();
        jMenuItem11 = new javax.swing.JMenuItem();
        jMenuItem12 = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.enrogen.hmi.EnrogenHMIApp.class).getContext().getResourceMap(EnrogenHMIView.class);
        mainPanel.setBackground(resourceMap.getColor("mainPanel.background")); // NOI18N
        mainPanel.setName("mainPanel"); // NOI18N

        mainLayeredPane.setName("mainLayeredPane"); // NOI18N

        alarmPanel.setName("alarmPanel"); // NOI18N
        alarmPanel.setOpaque(false);

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        table_alarm.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        table_alarm.setName("table_alarm"); // NOI18N
        jScrollPane2.setViewportView(table_alarm);

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.enrogen.hmi.EnrogenHMIApp.class).getContext().getActionMap(EnrogenHMIView.class, this);
        btnAckAlarm.setAction(actionMap.get("acknowledgeSelectedAlarm")); // NOI18N
        btnAckAlarm.setText(resourceMap.getString("btnAckAlarm.text")); // NOI18N
        btnAckAlarm.setName("btnAckAlarm"); // NOI18N

        btnAckAllAlarm.setAction(actionMap.get("acknowledgeAllAlarms")); // NOI18N
        btnAckAllAlarm.setText(resourceMap.getString("btnAckAllAlarm.text")); // NOI18N
        btnAckAllAlarm.setName("btnAckAllAlarm"); // NOI18N

        btnresetAlarm.setAction(actionMap.get("resetAllAlarms")); // NOI18N
        btnresetAlarm.setText(resourceMap.getString("btnresetAlarm.text")); // NOI18N
        btnresetAlarm.setName("btnresetAlarm"); // NOI18N

        javax.swing.GroupLayout alarmPanelLayout = new javax.swing.GroupLayout(alarmPanel);
        alarmPanel.setLayout(alarmPanelLayout);
        alarmPanelLayout.setHorizontalGroup(
            alarmPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(alarmPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(alarmPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 806, Short.MAX_VALUE)
                    .addComponent(jLabel1)
                    .addGroup(alarmPanelLayout.createSequentialGroup()
                        .addComponent(btnAckAlarm, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAckAllAlarm, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnresetAlarm, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        alarmPanelLayout.setVerticalGroup(
            alarmPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(alarmPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(alarmPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(btnresetAlarm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnAckAllAlarm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnAckAlarm, javax.swing.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)))
        );

        alarmPanel.setBounds(0, 0, 830, 580);
        mainLayeredPane.add(alarmPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        tabPanelMain.setBackground(resourceMap.getColor("tabPanelMain.background")); // NOI18N
        tabPanelMain.setName("tabPanelMain"); // NOI18N
        tabPanelMain.setPreferredSize(new java.awt.Dimension(184, 70));

        Messages.setBackground(resourceMap.getColor("Messages.background")); // NOI18N
        Messages.setName("Messages"); // NOI18N
        Messages.setOpaque(false);

        jScrollPane1.setBackground(resourceMap.getColor("jScrollPane1.background")); // NOI18N
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        text_messages.setBackground(resourceMap.getColor("text_messages.background")); // NOI18N
        text_messages.setColumns(20);
        text_messages.setRows(5);
        text_messages.setName("text_messages"); // NOI18N
        text_messages.setOpaque(false);
        jScrollPane1.setViewportView(text_messages);

        javax.swing.GroupLayout MessagesLayout = new javax.swing.GroupLayout(Messages);
        Messages.setLayout(MessagesLayout);
        MessagesLayout.setHorizontalGroup(
            MessagesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 818, Short.MAX_VALUE)
        );
        MessagesLayout.setVerticalGroup(
            MessagesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MessagesLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabPanelMain.addTab(resourceMap.getString("Messages.TabConstraints.tabTitle"), Messages); // NOI18N

        tabPanelMain.setBounds(0, 0, 830, 490);
        mainLayeredPane.add(tabPanelMain, javax.swing.JLayeredPane.DEFAULT_LAYER);

        faultPanel.setName("faultPanel"); // NOI18N
        faultPanel.setOpaque(false);

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        javax.swing.GroupLayout faultPanelLayout = new javax.swing.GroupLayout(faultPanel);
        faultPanel.setLayout(faultPanelLayout);
        faultPanelLayout.setHorizontalGroup(
            faultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(faultPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(faultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addContainerGap(501, Short.MAX_VALUE))
        );
        faultPanelLayout.setVerticalGroup(
            faultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(faultPanelLayout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addContainerGap(444, Short.MAX_VALUE))
        );

        faultPanel.setBounds(0, 0, 830, 490);
        mainLayeredPane.add(faultPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        buttonspanel.setMaximumSize(new java.awt.Dimension(171, 32767));
        buttonspanel.setMinimumSize(new java.awt.Dimension(171, 100));
        buttonspanel.setName("buttonspanel"); // NOI18N
        buttonspanel.setOpaque(false);

        btn_fullscreen.setAction(actionMap.get("switchFullScreen")); // NOI18N
        btn_fullscreen.setText(resourceMap.getString("btn_fullscreen.text")); // NOI18N
        btn_fullscreen.setName("btn_fullscreen"); // NOI18N

        btnoverview.setAction(actionMap.get("btnOverviewClick")); // NOI18N
        btnoverview.setText(resourceMap.getString("btnoverview.text")); // NOI18N
        btnoverview.setName("btnoverview"); // NOI18N

        redlamp.setIcon(resourceMap.getIcon("redlamp.icon")); // NOI18N
        redlamp.setText(resourceMap.getString("redlamp.text")); // NOI18N
        redlamp.setDisabledIcon(resourceMap.getIcon("redlamp.disabledIcon")); // NOI18N
        redlamp.setName("redlamp"); // NOI18N

        greenlamp.setIcon(resourceMap.getIcon("greenlamp.icon")); // NOI18N
        greenlamp.setText(resourceMap.getString("greenlamp.text")); // NOI18N
        greenlamp.setDisabledIcon(resourceMap.getIcon("greenlamp.disabledIcon")); // NOI18N
        greenlamp.setName("greenlamp"); // NOI18N

        pinPanel.setName("pinPanel"); // NOI18N
        pinPanel.setOpaque(false);

        js1.setName("js1"); // NOI18N

        js2.setName("js2"); // NOI18N

        js3.setName("js3"); // NOI18N

        btnEnterPin.setAction(actionMap.get("btnEnterPin")); // NOI18N
        btnEnterPin.setText(resourceMap.getString("btnEnterPin.text")); // NOI18N
        btnEnterPin.setName("btnEnterPin"); // NOI18N

        javax.swing.GroupLayout pinPanelLayout = new javax.swing.GroupLayout(pinPanel);
        pinPanel.setLayout(pinPanelLayout);
        pinPanelLayout.setHorizontalGroup(
            pinPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pinPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pinPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pinPanelLayout.createSequentialGroup()
                        .addComponent(js1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(js2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(js3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnEnterPin))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        pinPanelLayout.setVerticalGroup(
            pinPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pinPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pinPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(js3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(js2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(js1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnEnterPin))
        );

        alarmindicatorpanel.setName("alarmindicatorpanel"); // NOI18N
        alarmindicatorpanel.setOpaque(false);
        alarmindicatorpanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N
        alarmindicatorpanel.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, -1, -1));

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N
        alarmindicatorpanel.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 60, -1, -1));

        alarmicon.setIcon(resourceMap.getIcon("alarmicon.icon")); // NOI18N
        alarmicon.setText(resourceMap.getString("alarmicon.text")); // NOI18N
        alarmicon.setName("alarmicon"); // NOI18N
        alarmindicatorpanel.add(alarmicon, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 10, -1, -1));

        loginLevel.setText(resourceMap.getString("loginLevel.text")); // NOI18N
        loginLevel.setName("loginLevel"); // NOI18N

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        btnlogin.setText(resourceMap.getString("btnlogin.text")); // NOI18N
        btnlogin.setName("btnlogin"); // NOI18N
        btnlogin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnloginMouseClicked(evt);
            }
        });

        btnalarms.setAction(actionMap.get("btnAlarmClick")); // NOI18N
        btnalarms.setText(resourceMap.getString("btnalarms.text")); // NOI18N
        btnalarms.setName("btnalarms"); // NOI18N

        javax.swing.GroupLayout buttonspanelLayout = new javax.swing.GroupLayout(buttonspanel);
        buttonspanel.setLayout(buttonspanelLayout);
        buttonspanelLayout.setHorizontalGroup(
            buttonspanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonspanelLayout.createSequentialGroup()
                .addGroup(buttonspanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnalarms, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnoverview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btn_fullscreen, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
                    .addComponent(btnlogin, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(alarmindicatorpanel, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pinPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(buttonspanelLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(buttonspanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, buttonspanelLayout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(redlamp)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(greenlamp))
                            .addComponent(loginLevel, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addContainerGap(24, Short.MAX_VALUE))
        );
        buttonspanelLayout.setVerticalGroup(
            buttonspanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonspanelLayout.createSequentialGroup()
                .addComponent(btn_fullscreen, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(btnoverview, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnalarms, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnlogin, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(pinPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(alarmindicatorpanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 76, Short.MAX_VALUE)
                .addComponent(loginLevel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(buttonspanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(greenlamp)
                    .addComponent(redlamp)
                    .addComponent(jLabel6))
                .addContainerGap())
        );

        jLabel7.setIcon(resourceMap.getIcon("jLabel7.icon")); // NOI18N
        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addComponent(mainLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 929, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(buttonspanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonspanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(mainLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 664, Short.MAX_VALUE)
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        jMenuItem1.setAction(actionMap.get("showPreferencesBox")); // NOI18N
        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        fileMenu.add(jMenuItem1);

        jMenuItem2.setAction(actionMap.get("save")); // NOI18N
        jMenuItem2.setText(resourceMap.getString("jMenuItem2.text")); // NOI18N
        jMenuItem2.setName("jMenuItem2"); // NOI18N
        fileMenu.add(jMenuItem2);

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        menu_design.setText(resourceMap.getString("menu_design.text")); // NOI18N
        menu_design.setName("menu_design"); // NOI18N

        checkDesignMode.setAction(actionMap.get("switchDesignMode")); // NOI18N
        checkDesignMode.setText(resourceMap.getString("checkDesignMode.text")); // NOI18N
        checkDesignMode.setName("checkDesignMode"); // NOI18N
        menu_design.add(checkDesignMode);

        checkShowGrid.setAction(actionMap.get("paintDisplayPanel")); // NOI18N
        checkShowGrid.setSelected(true);
        checkShowGrid.setText(resourceMap.getString("checkShowGrid.text")); // NOI18N
        checkShowGrid.setName("checkShowGrid"); // NOI18N
        menu_design.add(checkShowGrid);

        checkSnaptoGrid.setAction(actionMap.get("paintDisplayPanel")); // NOI18N
        checkSnaptoGrid.setSelected(true);
        checkSnaptoGrid.setText(resourceMap.getString("checkSnaptoGrid.text")); // NOI18N
        checkSnaptoGrid.setName("checkSnaptoGrid"); // NOI18N
        menu_design.add(checkSnaptoGrid);

        jSeparator1.setName("jSeparator1"); // NOI18N
        menu_design.add(jSeparator1);

        menu_tab.setText(resourceMap.getString("menu_tab.text")); // NOI18N
        menu_tab.setName("menu_tab"); // NOI18N

        jMenuItem3.setAction(actionMap.get("addDisplayPane")); // NOI18N
        jMenuItem3.setText(resourceMap.getString("jMenuItem3.text")); // NOI18N
        jMenuItem3.setName("jMenuItem3"); // NOI18N
        menu_tab.add(jMenuItem3);

        jMenuItem6.setAction(actionMap.get("deleteTab")); // NOI18N
        jMenuItem6.setText(resourceMap.getString("jMenuItem6.text")); // NOI18N
        jMenuItem6.setName("jMenuItem6"); // NOI18N
        menu_tab.add(jMenuItem6);

        jSeparator2.setName("jSeparator2"); // NOI18N
        menu_tab.add(jSeparator2);

        jMenuItem4.setAction(actionMap.get("moveTabLeft")); // NOI18N
        jMenuItem4.setText(resourceMap.getString("jMenuItem4.text")); // NOI18N
        jMenuItem4.setName("jMenuItem4"); // NOI18N
        menu_tab.add(jMenuItem4);

        jMenuItem5.setAction(actionMap.get("moveTabRight")); // NOI18N
        jMenuItem5.setText(resourceMap.getString("jMenuItem5.text")); // NOI18N
        jMenuItem5.setName("jMenuItem5"); // NOI18N
        menu_tab.add(jMenuItem5);

        menu_design.add(menu_tab);

        menu_sld_component.setText(resourceMap.getString("menu_sld_component.text")); // NOI18N
        menu_sld_component.setName("menu_sld_component"); // NOI18N

        menu_add_breaker.setAction(actionMap.get("menu_add_breaker")); // NOI18N
        menu_add_breaker.setText(resourceMap.getString("menu_add_breaker.text")); // NOI18N
        menu_add_breaker.setName("menu_add_breaker"); // NOI18N
        menu_sld_component.add(menu_add_breaker);

        menu_add_busbar.setAction(actionMap.get("menu_add_busbar")); // NOI18N
        menu_add_busbar.setText(resourceMap.getString("menu_add_busbar.text")); // NOI18N
        menu_add_busbar.setName("menu_add_busbar"); // NOI18N
        menu_sld_component.add(menu_add_busbar);

        jMenuItem7.setAction(actionMap.get("menu_add_generator")); // NOI18N
        jMenuItem7.setText(resourceMap.getString("jMenuItem7.text")); // NOI18N
        jMenuItem7.setName("jMenuItem7"); // NOI18N
        menu_sld_component.add(jMenuItem7);

        jMenuItem8.setAction(actionMap.get("menu_add_transformer")); // NOI18N
        jMenuItem8.setText(resourceMap.getString("jMenuItem8.text")); // NOI18N
        jMenuItem8.setName("jMenuItem8"); // NOI18N
        menu_sld_component.add(jMenuItem8);

        menu_design.add(menu_sld_component);

        menu_instrument.setText(resourceMap.getString("menu_instrument.text")); // NOI18N
        menu_instrument.setName("menu_instrument"); // NOI18N

        jMenuItem9.setAction(actionMap.get("menu_add_analoguemeter")); // NOI18N
        jMenuItem9.setText(resourceMap.getString("jMenuItem9.text")); // NOI18N
        jMenuItem9.setName("jMenuItem9"); // NOI18N
        menu_instrument.add(jMenuItem9);

        jMenuItem10.setAction(actionMap.get("menu_add_digitalmeter")); // NOI18N
        jMenuItem10.setText(resourceMap.getString("jMenuItem10.text")); // NOI18N
        jMenuItem10.setActionCommand(resourceMap.getString("jMenuItem10.actionCommand")); // NOI18N
        jMenuItem10.setName("jMenuItem10"); // NOI18N
        menu_instrument.add(jMenuItem10);

        menu_design.add(menu_instrument);

        menu_other.setText(resourceMap.getString("menu_other.text")); // NOI18N
        menu_other.setName("menu_other"); // NOI18N

        menu_add_text.setAction(actionMap.get("menu_add_label")); // NOI18N
        menu_add_text.setLabel(resourceMap.getString("menu_add_text.label")); // NOI18N
        menu_add_text.setName("menu_add_text"); // NOI18N
        menu_other.add(menu_add_text);

        jMenuItem11.setAction(actionMap.get("menu_add_transmittablevalue")); // NOI18N
        jMenuItem11.setText(resourceMap.getString("jMenuItem11.text")); // NOI18N
        jMenuItem11.setName("jMenuItem11"); // NOI18N
        menu_other.add(jMenuItem11);

        jMenuItem12.setAction(actionMap.get("menu_add_transmitbutton")); // NOI18N
        jMenuItem12.setText(resourceMap.getString("jMenuItem12.text")); // NOI18N
        jMenuItem12.setName("jMenuItem12"); // NOI18N
        menu_other.add(jMenuItem12);

        menu_design.add(menu_other);

        menuBar.add(menu_design);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setComponent(mainPanel);
        setMenuBar(menuBar);
    }// </editor-fold>//GEN-END:initComponents

    private void btnloginMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnloginMouseClicked
        openLoginWindow();
    }//GEN-LAST:event_btnloginMouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Messages;
    private javax.swing.JPanel alarmPanel;
    private javax.swing.JLabel alarmicon;
    private javax.swing.JPanel alarmindicatorpanel;
    private javax.swing.JButton btnAckAlarm;
    private javax.swing.JButton btnAckAllAlarm;
    private javax.swing.JButton btnEnterPin;
    private javax.swing.JToggleButton btn_fullscreen;
    private javax.swing.JButton btnalarms;
    private javax.swing.JToggleButton btnlogin;
    private javax.swing.JButton btnoverview;
    private javax.swing.JButton btnresetAlarm;
    private javax.swing.JPanel buttonspanel;
    private javax.swing.JCheckBoxMenuItem checkDesignMode;
    private javax.swing.JCheckBoxMenuItem checkShowGrid;
    private javax.swing.JCheckBoxMenuItem checkSnaptoGrid;
    private javax.swing.JPanel faultPanel;
    private javax.swing.JLabel greenlamp;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem11;
    private javax.swing.JMenuItem jMenuItem12;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JSpinner js1;
    private javax.swing.JSpinner js2;
    private javax.swing.JSpinner js3;
    private javax.swing.JLabel loginLevel;
    private javax.swing.JLayeredPane mainLayeredPane;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem menu_add_breaker;
    private javax.swing.JMenuItem menu_add_busbar;
    private javax.swing.JMenuItem menu_add_text;
    private javax.swing.JMenu menu_design;
    private javax.swing.JMenu menu_instrument;
    private javax.swing.JMenu menu_other;
    private javax.swing.JMenu menu_sld_component;
    private javax.swing.JMenu menu_tab;
    private javax.swing.JPanel pinPanel;
    private javax.swing.JLabel redlamp;
    private javax.swing.JTabbedPane tabPanelMain;
    private javax.swing.JTable table_alarm;
    private javax.swing.JTextArea text_messages;
    // End of variables declaration//GEN-END:variables
    private JDialog aboutBox;
    private EnrogenHMIPreferences preferencesBox;
}
