//////////////////////////////////////////////////////////////////////////
//com.enrogen.hmi.components.ComponentConstructorBaseComponent
//2010 - James A R Brown
//Released under GPL V2
//////////////////////////////////////////////////////////////////////////
package com.enrogen.hmi.components;

import com.enrogen.sql.SQLCommand;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

////////////////////////////////////////////////////////////////////////////////
// Extending Class Build Up
// ------------------------
// JPanel
// > ComponentConstructorDraggable
// >> ComponentConstructorClickable
// >>> ComponentConstructorBaseComponent
// >>>> Custom Component (breaker etc)
////////////////////////////////////////////////////////////////////////////////
public class ComponentConstructorBaseComponent extends ComponentConstructorDraggable
        implements components {

    public HashMap parameterMap = new HashMap(); //The values for the component
    public HashMap editableAttributes = new HashMap(); // The types of the values, int, String
    private boolean gridSnap = true;
    private int gridSizeSnap = 80;
    public String componentName = "";
    public boolean draggable = false;
    public boolean isLive = false;
    //This is the difference between where is clicked on component and top lh corner
    //Needed to adjust drag and drop
    private int dragXOffsetToCorner = 0;
    private int dragYOffsetToCorner = 0;
    public boolean drawBorder = false;
    public boolean componentUpdated = false;

    public ComponentConstructorBaseComponent() {
        MouseListener();
        this.addKeyListener(movementKeyListener());
        this.setFocusable(true);
    }

    public void setGridSnap(boolean snap) {
        gridSnap = snap;
    }

    public void setGridSize(int size) {
        gridSizeSnap = size;
    }

    public void setDraggable(boolean isDraggable) {
        draggable = isDraggable;
    }

    public void setBorder(boolean border) {
        drawBorder = border;
    }

    public void setComponentUpdated(boolean updated) {
        componentUpdated = updated;
    }

    public boolean getComponentUpdated() {
        return componentUpdated;
    }

    ////////////////////////////////////////////////////////////////////////
    // Global Settings
    ////////////////////////////////////////////////////////////////////////////
    public HashMap getParametersMap() {
        updateSettings();
        return parameterMap;
    }

    /*public HashMap getEditable() {
    return parameterTypesMap;
    }*/
    public void setParametersMap(HashMap hm) {
        parameterMap = hm;
    }

    public void updateSettings() {
        Rectangle position = this.getBounds();
        parameterMap.put("PositionX", String.valueOf(position.x));
        parameterMap.put("PositionY", String.valueOf(position.y));
    }

    public boolean isLive() {
        return isLive;
    }

    //Tobe overrided (eg transmittablevalue)
    public void reDrawOnce() {
    }

    ////////////////////////////////////////////////////////////////////////////
    // sqlDataRefresh
    ////////////////////////////////////////////////////////////////////////////
    public void sqlDataRefresh(SQLCommand sqlc) {
            String sqlcommand = "";
        try {
            //Get the keys for the items
            Set ks = parameterMap.keySet();
            Object[] keySetList = (Object[]) ks.toArray();

            //Itterate over all the keys
            for (int i = 0; i < keySetList.length; i++) {
                HashMap resultsMap = new HashMap();
                String key = (String) keySetList[i];
                String value = parameterMap.get(key).toString();
                String slaveid = parameterMap.get("SlaveID").toString();
                boolean Lockout = false;
                
                //Only paint keys with live "ModbusRegister_"
                if (key.contains("ModbusRegister_")) {

                    //The value of the register must be >0
                    //if (value.compareTo("0") != 0) {
                    if (Integer.valueOf(slaveid) > 0) {

                        //Check the timestamps
                        //This must be a bug in mysql?!
                        //mysql> select time("09:44:01") - time("09:43:59");
                        //+-------------------------------------+
                        //| time("09:44:01") - time("09:43:59") |
                        //+-------------------------------------+
                        //|                                  42 |
                        //+-------------------------------------+
                        //
                        //Never knew there was 100sec in a minute!
                        
                        //Less elegant, review?
                        sqlcommand = "SELECT if((360*extract(hour from " +
                                "timediff(now(),timestamp))+60*extract(minute from " +
                                "timediff(now(),timestamp))+" +
                                "extract(second from timediff(now(),timestamp)))>" + MYSQL_MODBUS_DATA_CURRENT +
                                ",1,0) " +
                                "from slave" + slaveid + " where register=" + value +";";

                        List resultList = sqlc.SQLSelectCommand(sqlcommand);
                        //if result was valid
                        if (resultList.size() > 0) {
                            List row1 = (List) resultList.get(0);
                            Object[] values = row1.toArray();
                                if (Lockout == false) {
                                if ((Long) values[0] == 1.00) {
                                    isLive = false;
                                    Lockout = true;
                                } else {
                                    isLive = true;
                                }
                            }
                        }

                        sqlcommand = "SELECT register, description, timestamp, livedata, 16binary, 16integer, 16hex, "
                                + "32binary, 32integer, 32hex, is32bit, issigned "
                                + "FROM slave" + slaveid + " WHERE register="
                                + value + ";";
                        resultList = sqlc.SQLSelectCommand(sqlcommand);

                        //if result was valid
                        if (resultList.size() > 0) {
                            List row1 = (List) resultList.get(0);
                            Object[] values = row1.toArray();
                            resultsMap.put("register", ObjectIntegertoString(values[0]));
                            resultsMap.put("description", (String) values[1]);
                            resultsMap.put("timestamp", TimeStampObjecttoString(values[2]));

                            //redesign. checked using time stamp
                            resultsMap.put("livedata", ObjectBooleantoString(values[3]));
                            resultsMap.put("16binary", (String) values[4]);
                            resultsMap.put("16integer", ObjectLongtoString(values[5]));
                            resultsMap.put("16hex", (String) values[6]);
                            resultsMap.put("32binary", (String) values[7]);
                            resultsMap.put("32integer", ObjectLongtoString(values[8]));
                            resultsMap.put("32hex", (String) values[9]);
                            resultsMap.put("is32bit", ObjectBooleantoString(values[10]));
                            resultsMap.put("issigned", ObjectBooleantoString(values[11]));

                            //Now insert the values into the component
                            String newKey = key.substring(15);
                            parameterMap.remove(newKey);
                            parameterMap.put(newKey, resultsMap);
                        }
                        reDrawOnce();
                    }
                }
            }
        } catch (Exception e) {
            //isLive = false;
        }
        refresh();
    }

    public String ObjectIntegertoString(Object o) {
        try {
            Integer i = (Integer) o;
            String a = String.valueOf(i);
            return a;
        } catch (NullPointerException npe) {
            return "0";
        }
    }

    public String ObjectBooleantoString(Object o) {
        try {
            Boolean b = (Boolean) o;
            String a = "false";
            if (b) {
                a = "true";
            }
            return a;
        } catch (NullPointerException npe) {
            return "";
        }
    }

    public String TimeStampObjecttoString(Object o) {
        try {
            Timestamp t = (Timestamp) o;
            return t.toString();
        } catch (NullPointerException npe) {
            return "00-00-0000";
        }
    }

    public String ObjectLongtoString(Object o) {
        try {
            Long l = (Long) o;
            return l.toString();
        } catch (NullPointerException npe) {
            return "0.00";
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Refresh
    ////////////////////////////////////////////////////////////////////////////
    //Override with subcomponent
    public void refresh() {
    }

    ////////////////////////////////////////////////////////////////////////////
    // Component Highlighting for mouse over during edit
    ////////////////////////////////////////////////////////////////////////////
    public void drawBorder(Graphics g, int width, int height) {
        g.setColor(Color.RED);
        g.drawLine(1, 1, width - 1, 1);
        g.drawLine(width - 1, 1, width - 1, height - 1);
        g.drawLine(width, height - 1, 1, height - 1);
        g.drawLine(1, height - 1, 1, 1);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Component Layering (Send to Back) - Crude but enough
    ////////////////////////////////////////////////////////////////////////////
    public void sendToBack() {
        JPanel main = (JPanel) this.getParent();

        //Highest Zorder = first to paint
        main.setComponentZOrder(this, main.getComponentCount() - 1);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Draggable
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void dragGestureRecognized(DragGestureEvent evt) {

        //DragOrigin is relative to the top lh corner of object
        Point origin_point = evt.getDragOrigin();
        dragXOffsetToCorner = origin_point.x;
        dragYOffsetToCorner = origin_point.y;

        Transferable t = new StringSelection("aStricdcdcdcdcdcng");
        dragSource.startDrag(evt, DragSource.DefaultCopyDrop, t, this);

    }

    @Override
    public void dragDropEnd(DragSourceDropEvent evt) {
        if (draggable) {
            int x = evt.getX();
            int y = evt.getY();

            //Find current onscreen position and relative frame move
            Point screen_point = this.getLocationOnScreen();
            Point frame_point = this.getLocation();
            int x_adjust = (screen_point.x - frame_point.x);
            int y_adjust = (screen_point.y - frame_point.y);

            //Destination
            int dest_x = x - x_adjust;
            int dest_y = y - y_adjust;

            //Adjust for snap
            if (gridSnap) {
                int x_quotient = dest_x / gridSizeSnap;
                int y_quotient = dest_y / gridSizeSnap;
                dest_x = x_quotient * gridSizeSnap;
                dest_y = y_quotient * gridSizeSnap;
            } else {
                dest_x = dest_x - dragXOffsetToCorner;
                dest_y = dest_y - dragYOffsetToCorner;
            }


            this.setLocation(dest_x, dest_y);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Key Listener
    ////////////////////////////////////////////////////////////////////////////
    public KeyListener movementKeyListener() {
        KeyListener listener = new KeyListener() {

            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 37) {
                    Object o = e.getSource();
                    ComponentConstructorBaseComponent ccbc = (ComponentConstructorBaseComponent) o;
                    Point old_position = ccbc.getLocation();
                    if (ccbc.draggable) {
                        ccbc.setLocation(old_position.x - 1, old_position.y);
                        ccbc.repaint();
                    }
                }
                if (e.getKeyCode() == 38) {
                    Object o = e.getSource();
                    ComponentConstructorBaseComponent ccbc = (ComponentConstructorBaseComponent) o;
                    Point old_position = ccbc.getLocation();
                    if (ccbc.draggable) {
                        ccbc.setLocation(old_position.x, old_position.y - 1);
                        ccbc.repaint();
                    }
                }
                if (e.getKeyCode() == 39) {
                    Object o = e.getSource();
                    ComponentConstructorBaseComponent ccbc = (ComponentConstructorBaseComponent) o;
                    Point old_position = ccbc.getLocation();
                    if (ccbc.draggable) {
                        ccbc.setLocation(old_position.x + 1, old_position.y);
                        ccbc.repaint();
                    }
                }
                if (e.getKeyCode() == 40) {
                    Object o = e.getSource();
                    ComponentConstructorBaseComponent ccbc = (ComponentConstructorBaseComponent) o;
                    Point old_position = ccbc.getLocation();
                    if (ccbc.draggable) {
                        ccbc.setLocation(old_position.x, old_position.y + 1);
                        ccbc.repaint();
                    }
                }
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
            }
        };
        return listener;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Mouse 
    ////////////////////////////////////////////////////////////////////////////
    public void MouseListener() {
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent evt) {
                //For a standard click give the component the focus
                requestFocusInWindow();

                //If draggable = we are in designmode
                if (draggable) {
                    //Right click is custom menu
                    if (evt.getButton() == evt.BUTTON3) {
                        int x = evt.getX();
                        int y = evt.getY();

                        JPopupMenu jpm = new JPopupMenu("Popup");
                        JMenuItem jmi = new JMenuItem("Edit");
                        jmi.addActionListener(new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                openEditWindow(parameterMap, editableAttributes);
                            }
                        });
                        jpm.add(jmi);

                        JMenuItem jmi2 = new JMenuItem("Delete");
                        jmi2.addActionListener(new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                deleteThis();
                            }
                        });
                        jpm.add(jmi2);

                        JMenuItem jmi3 = new JMenuItem("Send To Back Layer");
                        jmi3.addActionListener(new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                sendToBack();
                            }
                        });
                        jpm.add(jmi3);

                        jpm.show(evt.getComponent(), x, y);
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent evt) {
                //Highlight
                ComponentConstructorBaseComponent c = (ComponentConstructorBaseComponent) evt.getComponent();

                //Ensure Tooltip is set
                c.setToolTipText(parameterMap.get("ComponentType").toString());

                if (draggable) {
                    //Draw a border
                    c.setBorder(true);
                } else {
                    c.setBorder(false);
                }
                c.repaint();
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                ComponentConstructorBaseComponent c = (ComponentConstructorBaseComponent) evt.getComponent();
                c.setBorder(false);
                c.repaint();
            }
        });
    }

    public void openEditWindow(HashMap parameterMap, HashMap editableAttributes) {
        //Build the frame
        JFrame jf = new JFrame();
        JScrollPane jsp = new JScrollPane();
        JPanel mainPanel = new JPanel();

        int width = EDITOR_BOX_WIDTH;
        int height = EDITOR_BOX_HEIGHT;
        jf.setSize(width, height);

        //Get the keys for the items
        Set ks = editableAttributes.keySet();
        Object[] keySetList = (Object[]) ks.toArray();

        //setup the main panel
        mainPanel.setPreferredSize(new Dimension(width, height));
        mainPanel.setBackground(Color.gray);

        //draw Jpanels with border for each component to be dealt with
        int x;
        for (x = 0; x < ks.size(); x++) {
            JPanel box = new JPanel();
            Dimension d = new Dimension(EDITOR_BOX_ROWWIDTH, EDITOR_BOX_ROWHEIGHT);
            box.setPreferredSize(d);
            box.setLocation(0, x * 21);
            box.setLayout(new GridLayout());
            Border etchedBdr = BorderFactory.createEtchedBorder();
            box.setBorder(etchedBdr);

            //Add the labels first
            String key = (String) keySetList[x];
            JLabel jl = new JLabel();
            jl.setText(key);
            box.add(jl);
            box.setName(key);

            //Now add the values in approproate containers
            String editableValue = editableAttributes.get(key).toString();

            //int = JSpinner
            if (editableValue.startsWith("int")) {
                JSpinner js = new JSpinner();
                js.setPreferredSize(new Dimension(150, 20));

                String value = parameterMap.get(key).toString();
                int intValue = Integer.valueOf(value);
                js.setValue(intValue);
                box.add(js);
            }

            //string = jtextarea
            if (editableValue.startsWith("String")) {
                JTextArea ja = new JTextArea();
                ja.setPreferredSize(new Dimension(150, 20));

                String value = parameterMap.get(key).toString();
                ja.setText(value);
                box.add(ja);
            }

            //jlabel = readonly
            if (editableValue.startsWith("readonly")) {
                JLabel jlab = new JLabel();
                jlab.setPreferredSize(new Dimension(150, 20));

                String value = parameterMap.get(key).toString();
                jlab.setText(value);
                box.add(jlab);
            }

            //drop = dropdown
            if (editableValue.startsWith("drop,")) {
                JComboBox jCombo = new JComboBox();
                jCombo.setPreferredSize(new Dimension(150, 20));

                String types = editableValue;
                String value = parameterMap.get(key).toString();

                //Now find the values
                boolean complete = false;
                String dropValues = types.substring(5);

                while (!complete) {
                    //if no more , this must be last value
                    if (!dropValues.contains(",")) {
                        complete = true;
                    }

                    int end = dropValues.indexOf(",", 1);

                    String subString = null;
                    if (end == -1) {
                        //if we are at end
                        subString = dropValues.substring(0);
                    } else {
                        //Get the value to be added
                        subString = dropValues.substring(0, end);
                    }

                    //remove it from our list
                    dropValues = dropValues.substring(end + 1);

                    jCombo.addItem(subString);
                }
                jCombo.setSelectedItem(value);
                box.add(jCombo);
            }

            if (editableValue.startsWith("boolean")) {
                JComboBox jCombo = new JComboBox();
                jCombo.setPreferredSize(new Dimension(150, 20));

                String types = editableValue;
                String value = parameterMap.get(key).toString();

                jCombo.addItem("true");
                jCombo.addItem("false");
                jCombo.setSelectedItem(value);
                box.add(jCombo);
            }

            //long = JSpinner
            if (editableValue.startsWith("long")) {
                JSpinner js = new JSpinner();
                SpinnerModel model = new SpinnerNumberModel(1, 0L, 999999L, 0.01);
                js.setModel(model);
                js.setPreferredSize(new Dimension(150, 20));

                String value = parameterMap.get(key).toString();
                Double doubValue = Double.valueOf(value);
                js.setValue(doubValue);
                box.add(js);
            }
            mainPanel.add(box);
        }

        //Add the buttons
        x++;
        JPanel box = new JPanel();
        Dimension d = new Dimension(EDITOR_BOX_ROWWIDTH, EDITOR_BOX_ROWHEIGHT);
        box.setPreferredSize(d);
        box.setLocation(0, x * 21);
        box.setLayout(new GridLayout());
        Border etchedBdr = BorderFactory.createEtchedBorder();
        box.setBorder(etchedBdr);

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
        box.add(btnClose);


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
                HashMap hm = new HashMap();
                for (int y = 0; y < mainPanel.getComponentCount() - 1; y++) {
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

                    hm.put(jl.getText().toString(), value);
                    setParametersMap(hm);
                    setComponentUpdated(true);
                    w.dispose();
                }

            }
        });
        box.add(btnApply);
        mainPanel.add(box);

        //Add a scroll panel and jpanel
        Dimension jspDim = new Dimension(width, height);
        jsp.setPreferredSize(jspDim);
        jsp.setVerticalScrollBarPolicy(jsp.VERTICAL_SCROLLBAR_ALWAYS);
        jsp.setViewportView(mainPanel);

        jf.add(jsp);
        jf.validate();
        jf.setVisible(true);
    }

    public void deleteThis() {
        Object parent = this.getParent();
        String firingObject = this.getName().toString();

        //The parent should be a container
        JPanel overviewPanel = (JPanel) parent;

        //Get all the components
        Component[] components = overviewPanel.getComponents();

        //Build a new overviewPanel skipping this component
        overviewPanel.removeAll();

        for (int i = 0; i < components.length; i++) {
            if (firingObject != components[i].getName().toString()) {
                overviewPanel.add(components[i]);
            }
        }

        //Fire it all back into the main panel.
        parent = overviewPanel;

    }
}
