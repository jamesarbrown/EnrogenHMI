package com.enrogen.hmi.components;

import com.enrogen.sql.SQLCommand;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.border.Border;

//Transmit button gets all the transmittable values on the pane
//and writes them to the sql database.
public class transmitbutton extends ComponentConstructorBaseComponent {

    public int height = 40; //Design height for this component
    public int width = 250; //Design width for this component
    public SQLCommand sqlConnection = null;

    ////////////////////////////////////////////////////////////////////////////
    // Parameters for the Label
    ////////////////////////////////////////////////////////////////////////////
    public transmitbutton() {
        //Create the default hasparameterMapap
        parameterMap.put("ComponentType", "transmitbutton");
        parameterMap.put("PositionX", "10");
        parameterMap.put("PositionY", "10");
        parameterMap.put("Width", 210);
        parameterMap.put("Height", 40);
        parameterMap.put("Label", "Value");

        setParameterTypes();
    }

    //Overload
    public transmitbutton(HashMap parameters) {
        parameterMap = parameters;
        setParameterTypes();

        //Update the height and width of component
        width = Integer.valueOf(parameterMap.get("Width").toString());
        height = Integer.valueOf(parameterMap.get("Height").toString());
    }

    private void setParameterTypes() {
        //Create the parameterTypes info
        editableAttributes.put("ComponentType", "readonly");
        editableAttributes.put("PositionX", "readonly");
        editableAttributes.put("PositionY", "readonly");
        editableAttributes.put("Label", "String");
        editableAttributes.put("Width", "int");
        editableAttributes.put("Height", "int");

        //Draw the component
        thisinit();
    }

    public void thisinit() {
        this.removeAll();
        this.setOpaque(false);
        this.setLayout(new GridLayout());

        JPanel box = createPanel();
        box.add(createButton());
        add(box);
    }

    public JPanel createPanel() {
        //draw Jpanels with border for each component to be dealt with
        JPanel box = new JPanel();
        box.setOpaque(false);
        Border etchedBdr = BorderFactory.createLineBorder(Color.BLACK, 1);
        box.setBorder(etchedBdr);
        box.setLayout(null);
        box.setLocation(0, 0);
        return box;
    }

    public JButton createButton() {
        //Create the label
        String label = null;
        JButton jb = new JButton(parameterMap.get("Label").toString());
        jb.setBounds(3, 0, width / 2 - 10, height);
        jb.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JButton b = (JButton) e.getSource();
                transmitbutton tb = (transmitbutton) b.getParent().getParent();
                tb.Transmit();
                //Window w = SwingUtilities.getWindowAncestor(c);
                //w.dispose();

            }
        });

        return jb;
    }

    @Override
    public void paint(Graphics g) {
        if (componentUpdated) {
            //Update the height and width of component
            width = Integer.valueOf(parameterMap.get("Width").toString());
            height = Integer.valueOf(parameterMap.get("Height").toString());
            this.setSize(width, height);

            //Create the new panel
            JPanel box = createPanel();
            box.add(createButton());
            add(box);

            //Remove old panel, insert new
            this.remove(0);
            this.add(box);

            //reset flag
            setComponentUpdated(false);
        }

        //Draw Highlighting border if in edit mode
        if (drawBorder) {
            drawBorder(g, width, height);
        }

        //Paint the standard components
        super.paint(g);
    }

    public static double round(double d, int decimalPlace) {
        // see the Javadoc about why we use a String in the constructor
        // http://java.sun.com/j2se/1.5.0/docs/api/java/math/BigDecimal.html#BigDecimal(double)
        BigDecimal bd = new BigDecimal(Double.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

    public void Transmit() {
        //Get where we are
        JPanel displayPanel = (JPanel) this.getParent();

        //Get all the components in the same panel
        Component[] components = displayPanel.getComponents();

        //Now we need to find any transmittablevalues
        for (int x = 0; x < components.length; x++) {
            ComponentConstructorBaseComponent ccbc = (ComponentConstructorBaseComponent) components[x];
            HashMap compParameterMap = ccbc.getParametersMap();
            String compComponentType = (String) compParameterMap.get("ComponentType");

            if (compComponentType.compareTo("transmittablevalue") == 0) {
                //Recast the component and find spinner value
                transmittablevalue tv = (transmittablevalue) ccbc;
                JPanel box = (JPanel) tv.getComponent(0);
                JSpinner js = (JSpinner) box.getComponent(1);
                Number value = (Number) js.getValue();

                //Set the initial value
                tv.initialValue = value;

                //Get the values that matter
                String slaveid = (String) compParameterMap.get("SlaveID");
                String modbusregister = (String) compParameterMap.get("ModbusRegister_CurrentValue");
                Double scalefactor = Double.valueOf((String) compParameterMap.get("ScalingFactor"));
                Boolean is32bit = Boolean.valueOf((String) compParameterMap.get("is32bit"));

                //Convert the value back
                Double modbusValue = value.doubleValue() / scalefactor;

                //Now round it.. floating point arith errors and all bye bye
                Number n = (Number) round(modbusValue, 0);

                //Todo isSigned?
                boolean isSigned = Boolean.valueOf((String) compParameterMap.get("isSigned"));

                //Construct the sql command
                if (!is32bit) {
                    if (!isSigned) {
                        //16 bit UNSIGNED
                        String sqlcmd = "UPDATE slave" + slaveid
                                + " SET writedata=" + n.intValue() + ", changeflag=1 "
                                + "WHERE register=" + modbusregister + ";";
                        System.out.println(sqlcmd);
                        sqlConnection.SQLUpdateCommand(sqlcmd);
                    } else {
                        //16bit SIGNED
                        //if -ve lets make it +ve first
                        int positiveValue = n.intValue();
                        if (n.intValue() < 0) {
                            positiveValue = n.intValue() * -1;
                        }

                        //Now convert it to a bit string
                        String bitString = Integer.toBinaryString(positiveValue);

                        //Pad it to length
                        while (bitString.length() < 16) {
                            bitString = "0" + bitString;
                        }

                        //Now if it was -ve, toggle the MSB to 1
                        if (n.intValue() < 0) {
                            bitString = bitString.substring(1);
                            bitString = "1" + bitString;
                        }

                        //Now turn back to an int
                        int signedInt = Integer.valueOf(bitString, 2);

                        String sqlcmd = "UPDATE slave" + slaveid
                                + " SET writedata=" + signedInt + ", changeflag=1 "
                                + "WHERE register=" + modbusregister + ";";
                        sqlConnection.SQLUpdateCommand(sqlcmd);
                        System.out.println(sqlcmd);

                    }
                } else {
                    String bitString = null;
                    //32 bit signed
                    if (isSigned) {
                        //if -ve lets make it +ve first
                        int positiveValue = n.intValue();
                        if (n.intValue() < 0) {
                            positiveValue = n.intValue() * -1;
                        }

                        //Now convert it to a bit string
                        bitString = Integer.toBinaryString(positiveValue);

                        //Pad it to length
                        while (bitString.length() < 32) {
                            bitString = "0" + bitString;
                        }

                        //Now if it was -ve, toggle the MSB to 1
                        if (n.intValue() < 0) {
                            bitString = bitString.substring(1);
                            bitString = "1" + bitString;
                        }
                    } else {
                        //32bit unsigned
                        //Break the string into 2 x 16bit int
                        bitString = Integer.toBinaryString(n.intValue());

                        //Pad it to length
                        while (bitString.length() < 32) {
                            bitString = "0" + bitString;
                        }
                    }

                    String HighByte = bitString.substring(0, 16);
                    String LowByte = bitString.substring(16);
                    System.out.println(HighByte + ":" + LowByte);
                    int HSB = Integer.valueOf(HighByte, 2);
                    int LSB = Integer.valueOf(LowByte, 2);
                    int HighModRegister = Integer.valueOf(modbusregister);
                    int LowModRegister = HighModRegister + 1;

                    //Generate SQL Commands into a batch
                    String sqlcmd1 = "UPDATE slave" + slaveid
                            + " SET writedata=" + HSB
                            + "WHERE register=" + HighModRegister + ";";
                    String sqlcmd2 = "UPDATE slave" + slaveid
                            + " SET writedata=" + LSB
                            + "WHERE register=" + LowModRegister + ";";
                    String sqlcmd3 = "UPDATE slave" + slaveid
                            + " SET changeflag=1 WHERE " + HighModRegister
                            + " <= register AND register <= " + LowModRegister + ";";
                }
            }
        }
    }

    //We dont want to deal with this component in the usual
    //manner of refreshing... so overriding the datarefresh
    //but we do want the sql connection
    @Override
    public void sqlDataRefresh(SQLCommand sqlc) {
        try {
            sqlConnection = sqlc;
        } catch (Exception e) {
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Refresh
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void refresh() {
        repaint();
    }

    public Point getStoredPosition() {
        Point p = new Point();
        int pos_x = Integer.valueOf(parameterMap.get("PositionX").toString());
        int pos_y = Integer.valueOf(parameterMap.get("PositionY").toString());
        p.setLocation(pos_x, pos_y);
        return p;
    }
}
