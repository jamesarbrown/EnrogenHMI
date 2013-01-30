//////////////////////////////////////////////////////////////////////////
//com.enrogen.hmi.components.transmittablevalue
//2010 - James A R Brown
//Released under GPL V2
//////////////////////////////////////////////////////////////////////////
package com.enrogen.hmi.components;

import com.enrogen.sql.SQLCommand;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.util.HashMap;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;

public class transmittablevalue extends ComponentConstructorBaseComponent {

    public int height = 40; //Design height for this component
    public int width = 250; //Design width for this component
    public Number initialValue = null;
    private int decimalplaces;

    ////////////////////////////////////////////////////////////////////////////
    // Parameters for the Label
    ////////////////////////////////////////////////////////////////////////////
    public transmittablevalue() {
        //Create the default hashMap parameterMap
        parameterMap.put("ComponentType", "transmittablevalue");
        parameterMap.put("FontSize", "10");
        parameterMap.put("PositionX", "10");
        parameterMap.put("PositionY", "10");
        parameterMap.put("Width", 210);
        parameterMap.put("Height", 40);
        parameterMap.put("Label", "Value");
        parameterMap.put("ScalingFactor", "1");
        parameterMap.put("Units", "sec");
        parameterMap.put("is32bit", "false");
        parameterMap.put("isSigned", "false");

        //Modbus Data
        parameterMap.put("SlaveID", "0");
        parameterMap.put("ModbusRegister_CurrentValue", "0");
        parameterMap.put("CurrentValue", new HashMap());

        setParameterTypes();
    }

    //Overload
    public transmittablevalue(HashMap parameters) {
        parameterMap = parameters;
        parameterMap.put("CurrentValue", new HashMap());
        setParameterTypes();

        //Update the height and width of component
        width = Integer.valueOf(parameterMap.get("Width").toString());
        height = Integer.valueOf(parameterMap.get("Height").toString());
    }

    private void setParameterTypes() {
        //Create the parameterTypes info
        editableAttributes.put("ComponentType", "readonly");
        editableAttributes.put("FontSize", "int");
        editableAttributes.put("PositionX", "readonly");
        editableAttributes.put("PositionY", "readonly");
        editableAttributes.put("Label", "String");
        editableAttributes.put("ScalingFactor", "long");
        editableAttributes.put("Units", "String");
        editableAttributes.put("Width", "int");
        editableAttributes.put("Height", "int");
        editableAttributes.put("is32bit", "boolean");
        editableAttributes.put("isSigned", "boolean");

        editableAttributes.put("SlaveID", "int");
        editableAttributes.put("ModbusRegister_CurrentValue", "int");

        //Draw the component
        thisinit();
    }

    public void thisinit() {
        this.removeAll();
        this.setOpaque(false);
        this.setLayout(new GridLayout());

        JPanel box = createPanel();
        box.add(createLabel());
        box.add(createSpinner());
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

    public JLabel createLabel() {
        //Create the label
        String label = null;
        JLabel jl = new JLabel(parameterMap.get("Label").toString());
        jl.setBounds(3, 0, width / 2 - 5, height);
        return jl;
    }

    public JSpinner createSpinner() {
        //Create the spinner
        JSpinner js = new JSpinner();

        //Identify how many decimal places to display from
        //the items scaling factor
        Double scalefactor = Double.valueOf((String) parameterMap.get("ScalingFactor"));
        Double steppingValue = 1.00;

        decimalplaces = 0;
        if (scalefactor < 1) {
            if (scalefactor < 0.1) {
                decimalplaces = 2;
                steppingValue = 0.01;
            } else {
                decimalplaces = 1;
                steppingValue = 0.1;
            }
        }

        //Build Spinner Model
        //Base it on is32Bit, isSigned
        boolean isSigned = Boolean.valueOf((String) parameterMap.get("isSigned"));
        boolean is32bit = Boolean.valueOf((String) parameterMap.get("is32bit"));

        //Default for 16 bit unsigned
        Double lowerValue = 0.0;
        Double upperValue = 65535.0;

        if (isSigned) {
            if (!is32bit) {
                //16 bit Signed
                lowerValue = -32767.0;
                upperValue = 32767.0;
            } else {
                //32 bit signed
                lowerValue = -2147483647.0;
                upperValue = 2147483647.0;
            }
        } else {
            if (is32bit) {
                //32bit Unsigned
                lowerValue = 0.0;
                upperValue = 4294967295.0;
            }
        }

        //Adjust upper and lower for scalefactor
        lowerValue = lowerValue / Math.pow(10, decimalplaces);
        upperValue = upperValue / Math.pow(10, decimalplaces);

        //Create the model
        SpinnerNumberModel model = new SpinnerNumberModel(0.00,
                lowerValue.doubleValue(),
                upperValue.doubleValue(),
                steppingValue.doubleValue());


        //Add model to spinner
        js.setModel(model);
        js.setBounds(width / 2 + 5, height / 2 - 15, 80, 30);

        //Remove any formatting marks
        JComponent editor = new JSpinner.NumberEditor(js, "#.##");
        js.setEditor(editor);

        return js;
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
            box.add(createLabel());
            box.add(createSpinner());
            add(box);

            //Remove old panel, insert new
            this.remove(0);
            this.add(box);

            //Reset value
            initialValue = null;

            //reset flag
            setComponentUpdated(false);
        }

        //Draw Highlighting border if in edit mode
        if (drawBorder) {
            drawBorder(g, width, height);
        }

        //Get the spinner
        JPanel box = (JPanel) this.getComponent(0);
        JSpinner js = (JSpinner) box.getComponent(1);

        //First time init spinner value
       /* try {
            if (initialValue == null) {
                Double value = getCurrentValue();
                initialValue = value;
                int decimalplaces = Integer.valueOf((String) parameterMap.get("DecimalPlaces"));

                Number n = value;
                if (decimalplaces == 0) {
                    js.setValue(n.intValue());
                } else {
                    js.setValue(n);
                }
            }
        } catch (Exception e) {
        }*/



        //Paint the standard components
        super.paint(g);
    }

    @Override
    public void reDrawOnce() {
        JPanel box = (JPanel) this.getComponent(0);
        JSpinner js = (JSpinner) box.getComponent(1);

        //First time init spinner value
        try {
            if (initialValue == null) {
                Double value = getCurrentValue();
                initialValue = value;
                
                Number n = value;
                if (decimalplaces == 0) {
                    js.setValue(n.intValue());
                } else {
                    js.setValue(n);
                }
            }
        } catch (Exception e) {
            System.err.println("Transmittable Value - Could not set spinner value");
            e.printStackTrace();
        }

        //Paint the foreground color
        DefaultEditor de = (DefaultEditor) js.getEditor();
        JFormattedTextField ftf = de.getTextField();
        Number currentValue = (Number) js.getValue();
        if (initialValue.doubleValue() == currentValue.doubleValue()) {
            ftf.setForeground(Color.GREEN);
        } else {
            ftf.setForeground(Color.BLACK);
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

    public Double getCurrentValue() {
        HashMap hm = (HashMap) parameterMap.get("CurrentValue");
        try {
            boolean is32bit = Boolean.valueOf((String) hm.get("is32bit"));
            String valueString = "0";

            if (is32bit) {
                valueString = (String) hm.get("32integer");
            } else {
                valueString = (String) hm.get("16integer");
            }

            Integer value = Integer.valueOf(valueString);
            Double scaleFactor = Double.valueOf((String) parameterMap.get("ScalingFactor"));
            Double scaledValue = value * scaleFactor;

            //So we know the original value
            if (initialValue == null) {
                initialValue = scaledValue;
            }
            return scaledValue;

        } catch (Exception e) {
            e.printStackTrace();
            isLive = false;
            return 0.00;
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    // Drawing
    ////////////////////////////////////////////////////////////////////////////


}
