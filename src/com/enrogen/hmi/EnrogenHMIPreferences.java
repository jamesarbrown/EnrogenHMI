/*
 * EnrogenHMIAboutBox.java
 */
package com.enrogen.hmi;

import com.enrogen.xml.xmlio;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.HashMap;
import java.util.List;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.application.Action;

public class EnrogenHMIPreferences extends javax.swing.JDialog implements hmi {

    HashMap Preferences = null;

    public EnrogenHMIPreferences(java.awt.Frame parent) {
        super(parent);

        initComponents();
        //getRootPane().setDefaultButton(saveButton);

        //Sets up the spinner high and low
        setupSpinners();
    }

    @Action
    public void closeBox() {
        setVisible(false);
        dispose();
    }

    public void insertPreferenceValues(HashMap Values) {
        Preferences = Values;
    }

    public void refreshPreferencesForm() {
        //Fills with current settings
        insertCurrentValues(Preferences);
    }

    ////////////////////////////////////////////////////////////////////////////
    //First Start
    ////////////////////////////////////////////////////////////////////////////
    //Holding list for the recipients
    List SMTPRecipients = null;

    public void setupSpinners() {
        //Gridsize spinner
        SpinnerListModel listModel = new SpinnerListModel(new String[]{"10", "20", "30", "40", "50", "60", "70", "80", "100"});
        edit_spin_gridsize.setModel(listModel);

        //SMTP Port
        SpinnerNumberModel PortsModel = new SpinnerNumberModel(1,
                1, //min
                9999, //max
                1);
        spinSMTPPort.setModel(PortsModel);
    }
    public int LoginLevel = 1;

    public void insertCurrentValues(HashMap IncomingPref) {
        edit_spin_gridsize.setValue(String.valueOf(IncomingPref.get("gridsize")));
        textSiteName.setText(String.valueOf(IncomingPref.get("siteName")));
        LoginLevel = (Integer) IncomingPref.get("LoginLevel");

        //SQL
        MySQLServerIP.setText((String) IncomingPref.get("MySQLServerIP"));
        MySQLServerPort.setText((String) IncomingPref.get("MySQLServerPort"));
        MySQLUsername.setText((String) IncomingPref.get("MySQLUsername"));
        MySQLPassword.setText((String) IncomingPref.get("MySQLPassword"));
        MySQLDatabaseName.setText((String) IncomingPref.get("MySQLDatabaseName"));

        //SMTP
        String SMTPIsEnabled = null;
        SMTPIsEnabled = (String) IncomingPref.get("SMTPIsEnabled");
        if (SMTPIsEnabled.compareTo("false") == 0) {
            checkSMTPEnabled.setSelected(false);
        } else {
            checkSMTPEnabled.setSelected(true);
        }

        textSMTPServer.setText((String) IncomingPref.get("SMTPServer"));
        spinSMTPPort.setValue(Integer.valueOf((String) IncomingPref.get("SMTPPort")));
        textSMTPUsername.setText((String) IncomingPref.get("SMTPUsername"));
        textSMTPPassword.setText((String) IncomingPref.get("SMTPPassword"));

        //Get the recipients and store locally
        SMTPRecipients = (List) IncomingPref.get("SMTPRecipients");
    }

    public void redrawLogin() {
        labelLogin.setText("Access Level :" + LoginLevel);
        redrawPreferences();
        redrawSMTPPref();
    }

    public void redrawPreferences() {
        if (LoginLevel < 4) {
            MySQLServerIP.setEnabled(false);
            MySQLServerPort.setEnabled(false);
            MySQLUsername.setEnabled(false);
            MySQLPassword.setEnabled(false);
            MySQLDatabaseName.setEnabled(false);
        } else {
            MySQLServerIP.setEnabled(true);
            MySQLServerPort.setEnabled(true);
            MySQLUsername.setEnabled(true);
            MySQLPassword.setEnabled(true);
            MySQLDatabaseName.setEnabled(true);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    //Redrawing SMTP
    ////////////////////////////////////////////////////////////////////////////
    @Action
    public void redrawSMTPPref() {
        if (LoginLevel >= 4) {
            checkSMTPEnabled.setEnabled(true);
            //SMTP
            if (checkSMTPEnabled.isSelected()) {
                textSMTPServer.setEnabled(true);
                spinSMTPPort.setEnabled(true);
                textSMTPUsername.setEnabled(true);
                textSMTPPassword.setEnabled(true);
            } else {
                textSMTPServer.setEnabled(false);
                spinSMTPPort.setEnabled(false);
                textSMTPUsername.setEnabled(false);
                textSMTPPassword.setEnabled(false);
            }
        } else {
            checkSMTPEnabled.setEnabled(false);
            textSMTPServer.setEnabled(false);
            spinSMTPPort.setEnabled(false);
            textSMTPUsername.setEnabled(false);
            textSMTPPassword.setEnabled(false);
        }
        //SMTP Recipients Table
        DefaultTableModel aModel = new DefaultTableModel();

        //Get current selected table row
        int rowSelected = table_recipients.getSelectedRow();

        //If Tablemodel has existing rows delete them
        aModel.setRowCount(0);

        //Set the columns
        String[] tableColumnsName = {"Recipient Email"};
        aModel.setColumnIdentifiers(tableColumnsName);

        //Add the recipients to the table
        for (int x = 0; x < SMTPRecipients.size(); x++) {
            Object[] o = {(String) SMTPRecipients.get(x)};
            aModel.addRow(o);
        }

        table_recipients.setModel(aModel);
    }

    public void addFocusListener() {
        jTabbedPane1.addFocusListener(new FocusAdapter() {

            public void focusGained(FocusEvent e) {

                            System.out.println("1");
                SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            System.out.println("2");
                            
                            redrawLogin();
                        }
                });
            }
        });
    }
    ////////////////////////////////////////////////////////////////////////////
    //Save
    ////////////////////////////////////////////////////////////////////////////

    @Action
    public boolean saveSettingXML() {
        //addMessage("Saving setting.xml");
        boolean sucess = false;

        String SettingsXMLFile = FULL_SETTING_XML_PATH;

        xmlio xmlIO = new xmlio();
        xmlIO.createNewXmlFile(SettingsXMLFile);
        xmlIO.addRootNode("EnrogenHMIPreferences");
        xmlIO.addSubNode("EnrogenHMIPreferences", "default");
        xmlIO.addSubNode("default", "XMLversion", String.valueOf(REQUIRED_XML_VERSION));
        xmlIO.addSubNode("EnrogenHMIPreferences", "Preferences");
        xmlIO.addSubNode("Preferences", "GridSize", String.valueOf(edit_spin_gridsize.getValue()));
        xmlIO.addSubNode("Preferences", "siteName", String.valueOf(textSiteName.getText().toString()));

        //SQL
        xmlIO.addSubNode("EnrogenHMIPreferences", "mysql");
        xmlIO.addSubNode("mysql", "ServerIP", MySQLServerIP.getText().toString());
        xmlIO.addSubNode("mysql", "Port", MySQLServerPort.getText().toString());
        xmlIO.addSubNode("mysql", "Username", MySQLUsername.getText().toString());
        xmlIO.addSubNode("mysql", "Password", MySQLPassword.getText().toString());
        xmlIO.addSubNode("mysql", "DatabaseName", MySQLDatabaseName.getText().toString());

        //SMTP Settings
        xmlIO.addSubNode("EnrogenHMIPreferences", "SMTP");
        if (checkSMTPEnabled.isSelected()) {
            xmlIO.addSubNode("SMTP", "IsEnabled", "true");
        } else {
            xmlIO.addSubNode("SMTP", "IsEnabled", "false");
        }
        xmlIO.addSubNode("SMTP", "Server", textSMTPServer.getText().toString());
        xmlIO.addSubNode("SMTP", "Port", String.valueOf(spinSMTPPort.getValue()));
        xmlIO.addSubNode("SMTP", "Username", textSMTPUsername.getText().toString());
        xmlIO.addSubNode("SMTP", "Password", textSMTPPassword.getText().toString());

        //Recipients
        xmlIO.addSubNode("EnrogenHMIPreferences", "SMTP_RECIPIENTS");

        for (int x = 0; x < SMTPRecipients.size(); x++) {
            String emailno = "Email" + String.valueOf(x);
            String email = (String) SMTPRecipients.get(x);
            xmlIO.addSubNode("SMTP_RECIPIENTS", emailno.toString(), email.toString());
        }

        sucess = xmlIO.writeXMLFile();

        //Hide the window
        dispose();

        //If ok start the sql with new settings
        if (sucess) {
            //  addMessage("Sucess : Save setting.xml");
        } else {
            //  addMessage("FAILED : Save setting.xml");
        }
        return sucess;


    }

    ////////////////////////////////////////////////////////////////////////////
    //Add new email
    ////////////////////////////////////////////////////////////////////////////
    @Action
    public void addEmailAddress() {
        if (!textEmail.getText().isEmpty()) {
            SMTPRecipients.add(textEmail.getText().toString());
        }
        redrawSMTPPref();
    }

    @Action
    public void deleteEmailAddress() {
        //Get current selected table row
        int rowSelected = table_recipients.getSelectedRow();

        //Drop it from the list
        SMTPRecipients.remove(rowSelected);

        //Redraw it all
        redrawSMTPPref();
    }

    private class SaveSettingXMLTask extends org.jdesktop.application.Task<Object, Void> {

        SaveSettingXMLTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to SaveSettingXMLTask fields, here.
            super(app);
        }

        @Override
        protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.
            return null;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        edit_spin_gridsize = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        textSiteName = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        MySQLServerIP = new javax.swing.JTextField();
        MySQLServerPort = new javax.swing.JTextField();
        MySQLUsername = new javax.swing.JTextField();
        MySQLPassword = new javax.swing.JTextField();
        MySQLDatabaseName = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        checkSMTPEnabled = new javax.swing.JCheckBox();
        SMTPPanel1 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        textSMTPServer = new javax.swing.JTextField();
        spinSMTPPort = new javax.swing.JSpinner();
        textSMTPUsername = new javax.swing.JTextField();
        textSMTPPassword = new javax.swing.JTextField();
        SMTPPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table_recipients = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        textEmail = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        btn_save = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        labelLogin = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.enrogen.hmi.EnrogenHMIApp.class).getContext().getResourceMap(EnrogenHMIPreferences.class);
        setTitle(resourceMap.getString("title")); // NOI18N
        setModal(true);
        setName("aboutBox"); // NOI18N
        setResizable(false);

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N
        jTabbedPane1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                focusGain(evt);
            }
        });

        jPanel1.setName("jPanel1"); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        edit_spin_gridsize.setName("edit_spin_gridsize"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N

        textSiteName.setText(resourceMap.getString("textSiteName.text")); // NOI18N
        textSiteName.setName("textSiteName"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(textSiteName)
                    .addComponent(edit_spin_gridsize, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(edit_spin_gridsize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(textSiteName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(114, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel3.border.title"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        MySQLServerIP.setText(resourceMap.getString("MySQLServerIP.text")); // NOI18N
        MySQLServerIP.setName("MySQLServerIP"); // NOI18N

        MySQLServerPort.setText(resourceMap.getString("MySQLServerPort.text")); // NOI18N
        MySQLServerPort.setName("MySQLServerPort"); // NOI18N

        MySQLUsername.setText(resourceMap.getString("MySQLUsername.text")); // NOI18N
        MySQLUsername.setName("MySQLUsername"); // NOI18N

        MySQLPassword.setText(resourceMap.getString("MySQLPassword.text")); // NOI18N
        MySQLPassword.setName("MySQLPassword"); // NOI18N

        MySQLDatabaseName.setText(resourceMap.getString("MySQLDatabaseName.text")); // NOI18N
        MySQLDatabaseName.setName("MySQLDatabaseName"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5))
                        .addGap(50, 50, 50))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(MySQLServerPort, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(MySQLServerIP, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(MySQLUsername, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(MySQLPassword, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(MySQLDatabaseName, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(22, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(MySQLServerIP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(MySQLServerPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(MySQLUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(MySQLPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(MySQLDatabaseName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(111, Short.MAX_VALUE))
        );

        jPanel2.getAccessibleContext().setAccessibleName(resourceMap.getString("jPanel2.AccessibleContext.accessibleName")); // NOI18N

        jTabbedPane1.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        jPanel4.setName("jPanel4"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.enrogen.hmi.EnrogenHMIApp.class).getContext().getActionMap(EnrogenHMIPreferences.class, this);
        checkSMTPEnabled.setAction(actionMap.get("redrawSMTPPref")); // NOI18N
        checkSMTPEnabled.setText(resourceMap.getString("checkSMTPEnabled.text")); // NOI18N
        checkSMTPEnabled.setName("checkSMTPEnabled"); // NOI18N

        SMTPPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("SMTPPanel1.border.title"))); // NOI18N
        SMTPPanel1.setName("SMTPPanel1"); // NOI18N

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        textSMTPServer.setText(resourceMap.getString("textSMTPServer.text")); // NOI18N
        textSMTPServer.setName("textSMTPServer"); // NOI18N

        spinSMTPPort.setName("spinSMTPPort"); // NOI18N

        textSMTPUsername.setText(resourceMap.getString("textSMTPUsername.text")); // NOI18N
        textSMTPUsername.setName("textSMTPUsername"); // NOI18N

        textSMTPPassword.setText(resourceMap.getString("textSMTPPassword.text")); // NOI18N
        textSMTPPassword.setName("textSMTPPassword"); // NOI18N

        javax.swing.GroupLayout SMTPPanel1Layout = new javax.swing.GroupLayout(SMTPPanel1);
        SMTPPanel1.setLayout(SMTPPanel1Layout);
        SMTPPanel1Layout.setHorizontalGroup(
            SMTPPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SMTPPanel1Layout.createSequentialGroup()
                .addGroup(SMTPPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8))
                .addGap(47, 47, 47)
                .addGroup(SMTPPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(spinSMTPPort)
                    .addComponent(textSMTPServer, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE))
                .addGap(35, 35, 35)
                .addGroup(SMTPPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SMTPPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textSMTPUsername, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE))
                    .addGroup(SMTPPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addGap(18, 18, 18)
                        .addComponent(textSMTPPassword, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)))
                .addContainerGap())
        );
        SMTPPanel1Layout.setVerticalGroup(
            SMTPPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SMTPPanel1Layout.createSequentialGroup()
                .addGroup(SMTPPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(textSMTPServer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(textSMTPUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(SMTPPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spinSMTPPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(jLabel10)
                    .addComponent(textSMTPPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        SMTPPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("SMTPPanel2.border.title"))); // NOI18N
        SMTPPanel2.setName("SMTPPanel2"); // NOI18N

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        table_recipients.setModel(new javax.swing.table.DefaultTableModel(
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
        table_recipients.setName("table_recipients"); // NOI18N
        jScrollPane1.setViewportView(table_recipients);

        jButton1.setAction(actionMap.get("deleteEmailAddress")); // NOI18N
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel7.border.title"))); // NOI18N
        jPanel7.setName("jPanel7"); // NOI18N

        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N

        textEmail.setText(resourceMap.getString("textEmail.text")); // NOI18N
        textEmail.setName("textEmail"); // NOI18N

        jButton2.setAction(actionMap.get("addEmailAddress")); // NOI18N
        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addComponent(textEmail, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addContainerGap(2, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout SMTPPanel2Layout = new javax.swing.GroupLayout(SMTPPanel2);
        SMTPPanel2.setLayout(SMTPPanel2Layout);
        SMTPPanel2Layout.setHorizontalGroup(
            SMTPPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SMTPPanel2Layout.createSequentialGroup()
                .addGroup(SMTPPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SMTPPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jButton1))
                .addContainerGap())
        );
        SMTPPanel2Layout.setVerticalGroup(
            SMTPPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SMTPPanel2Layout.createSequentialGroup()
                .addGroup(SMTPPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(SMTPPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(checkSMTPEnabled)
                    .addComponent(SMTPPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkSMTPEnabled)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(SMTPPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(SMTPPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel4.TabConstraints.tabTitle"), jPanel4); // NOI18N

        btn_save.setAction(actionMap.get("saveSettingXML")); // NOI18N
        btn_save.setText(resourceMap.getString("btn_save.text")); // NOI18N
        btn_save.setName("btn_save"); // NOI18N

        jButton3.setAction(actionMap.get("closeBox")); // NOI18N
        jButton3.setText(resourceMap.getString("jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N

        labelLogin.setText(resourceMap.getString("labelLogin.text")); // NOI18N
        labelLogin.setName("labelLogin"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 634, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                            .addComponent(btn_save, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)))
                    .addComponent(labelLogin))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btn_save)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addComponent(labelLogin)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE)))
                .addContainerGap())
        );

        getAccessibleContext().setAccessibleName(resourceMap.getString("aboutBox.AccessibleContext.accessibleName")); // NOI18N

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void focusGain(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_focusGain

        this.redrawLogin();
    }//GEN-LAST:event_focusGain
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField MySQLDatabaseName;
    private javax.swing.JTextField MySQLPassword;
    private javax.swing.JTextField MySQLServerIP;
    private javax.swing.JTextField MySQLServerPort;
    private javax.swing.JTextField MySQLUsername;
    private javax.swing.JPanel SMTPPanel1;
    private javax.swing.JPanel SMTPPanel2;
    private javax.swing.JButton btn_save;
    private javax.swing.JCheckBox checkSMTPEnabled;
    private javax.swing.JSpinner edit_spin_gridsize;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel labelLogin;
    private javax.swing.JSpinner spinSMTPPort;
    private javax.swing.JTable table_recipients;
    private javax.swing.JTextField textEmail;
    private javax.swing.JTextField textSMTPPassword;
    private javax.swing.JTextField textSMTPServer;
    private javax.swing.JTextField textSMTPUsername;
    private javax.swing.JTextField textSiteName;
    // End of variables declaration//GEN-END:variables
}
