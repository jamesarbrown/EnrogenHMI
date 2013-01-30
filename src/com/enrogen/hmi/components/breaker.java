/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.enrogen.hmi.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Set;
import javax.swing.ImageIcon;

public class breaker extends ComponentConstructorBaseComponent {

    private boolean isClosed = false;
    public int height = 70; //Design height for this component
    public int width = 70; //Design height for this component

    ////////////////////////////////////////////////////////////////////////////
    // Parameters for the Breaker
    // All Modbus Regsiters to be polled must start "ModbusRegister_"
    ////////////////////////////////////////////////////////////////////////////
    public breaker(String name) {
        setParameterTypes();

        //Create the default hasparameterMapap
        parameterMap.put("ComponentType", "breaker");
        parameterMap.put("ComponentName", name);
        parameterMap.put("VerticalOrientation", "true");
        parameterMap.put("UpStreamComponent", "");
        parameterMap.put("DownStreamComponent", "");
        parameterMap.put("PositionX", "10");
        parameterMap.put("PositionY", "10");
        parameterMap.put("Width", "70");
        parameterMap.put("Height", "70");
        parameterMap.put("IncomingSide", "bottom");
        parameterMap.put("KW_Scale_Factor", "1");
        parameterMap.put("DecimalPlaces", "2");
        parameterMap.put("Show_KW", "false");
        parameterMap.put("KW_Label", "kW");

        //Modbus Info
        parameterMap.put("SlaveID", "0");
        parameterMap.put("ModbusRegister_Closed", "0");
        parameterMap.put("ModbusRegister_KW", "0");
        parameterMap.put("Closed_bitno", "1");

        //Results Info keyname = right trimmed ModbusRegister_
        //Result should be a hashmap containing results
        parameterMap.put("Closed", new HashMap());
        parameterMap.put("KW", new HashMap());
    }

    //Overload
    public breaker(HashMap parameters) {
        setParameterTypes();
        parameterMap = parameters;

        //But override the reading parameters
        parameterMap.put("Closed", new HashMap());
        parameterMap.put("KW", new HashMap());

        //Update the height and width of component
        width = Integer.valueOf(parameterMap.get("Width").toString());
        height = Integer.valueOf(parameterMap.get("Height").toString());

    }

    private void setParameterTypes() {
        //Create the parameterTypes info
        editableAttributes.put("ComponentType", "readonly");
        editableAttributes.put("ComponentName", "String");
        editableAttributes.put("VerticalOrientation", "boolean");
        editableAttributes.put("UpStreamComponent", "String");
        editableAttributes.put("DownStreamComponent", "String");
        editableAttributes.put("PositionX", "readonly");
        editableAttributes.put("PositionY", "readonly");
        editableAttributes.put("Width", "int");
        editableAttributes.put("Height", "int");
        editableAttributes.put("IncomingSide", "drop,top,bottom,left,right");
        editableAttributes.put("KW_Scale_Factor", "long");
        editableAttributes.put("DecimalPlaces", "drop,0,1");
        editableAttributes.put("Show_KW", "boolean");
        editableAttributes.put("KW_Label", "String");
        editableAttributes.put("SlaveID", "int");
        editableAttributes.put("ModbusRegister_Closed", "int");
        editableAttributes.put("ModbusRegister_KW", "int");
        editableAttributes.put("Closed_bitno", "int");
    }

    ////////////////////////////////////////////////////////////////////////////
    // Refresh
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void refresh() {
        try {
            isClosed();
            repaint();
        } catch (Exception e) {
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ////////////////////////////////////////////////////////////////////////////
    public void setBreakerName(String name) {
        parameterMap.put("ComponentName", name);
    }

    public String getBreakerName() {
        return (String) parameterMap.get("ComponentName");
    }

    public void setIsClosed(boolean closed) {
        isClosed = closed;
    }

    public boolean isClosed() {
        HashMap hm = (HashMap) parameterMap.get("Closed");

        //Breaker is 16 bit data block
        //Bit re satus is selectable
        String binary16 = hm.get("16binary").toString();

        //remember bit1-16 where bit 1 is rhs, bit 16lhs
        String bitposition = parameterMap.get("Closed_bitno").toString();
        double pos = Double.valueOf(bitposition);
        pos = -1 * (pos - 16);
        String bitvalue = binary16.substring((int) pos, (int) pos + 1);

        if (bitvalue.compareTo("1") == 0) {
            isClosed = true;
        } else {
            isClosed = false;
        }

        return isClosed;
    }

    public void setUpStreamComponent(String name) {
        parameterMap.put("UpStreamComponent", name);
    }

    public String getUpStreamComponent() {
        return (String) parameterMap.get("UpStreamComponent");
    }

    public void setDownStreamComponent(String name) {
        parameterMap.put("DownStreamComponent", name);
    }

    public String getDownStreamComponent() {
        return (String) parameterMap.get("DownStreamComponent");
    }

    public Point getStoredPosition() {
        Point p = new Point();
        int pos_x = Integer.valueOf(parameterMap.get("PositionX").toString());
        int pos_y = Integer.valueOf(parameterMap.get("PositionY").toString());
        p.setLocation(pos_x, pos_y);
        return p;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Pixel Moving
    ////////////////////////////////////////////////////////////////////////////
    public void addKeyListener() {
        this.addKeyListener(new java.awt.event.KeyAdapter() {

            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeyPressed(evt);
            }
        });
    }

    private void KeyPressed(java.awt.event.KeyEvent evt) {

    }

    public void moveDown() {
        int x = this.getX();
        int y = this.getY();
        this.setLocation(x, y + 1);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Vertical Orientation
    ////////////////////////////////////////////////////////////////////////////
    private void drawVerticalOutgoing(Graphics g) {
        g.setColor(components.BUS_BAR_COLOR);
        g.drawLine(20, 0, 20, 20);
        g.drawLine(18, 18, 22, 22);
        g.drawLine(18, 22, 22, 18);
    }

    private void drawVerticalOpenContact(Graphics g) {
        //Paint over Closed Contact
        g.setColor(components.BACKGROUND_REPAINT_COLOR);
        g.drawLine(20, 50, 22, 20);
        g.drawLine(20, 50, 20, 70);

        //Paint open contact
        g.setColor(components.BUS_BAR_COLOR);
        g.drawLine(20, 50, 35, 20);
        g.drawLine(20, 50, 20, 70);
    }

    private void drawVerticalClosedContact(Graphics g) {
        //Paint over open contact
        g.setColor(components.BACKGROUND_REPAINT_COLOR);
        g.drawLine(20, 50, 35, 20);
        g.drawLine(20, 50, 20, 70);

        //Paint closed contact
        g.setColor(components.BUS_BAR_COLOR);
        g.drawLine(20, 50, 22, 20);
        g.drawLine(20, 50, 20, 70);
    }

    private void drawVerticalBreakerLamp(Graphics g) {
        int y = 10;
        int x = 5;
        if (isClosed) {
            g.setColor(Color.RED);
            g.fillOval(x, y, 10, 10);
        } else {
            g.setColor(Color.GREEN);
            g.fillOval(x, y, 10, 10);
        }
        g.setColor(Color.BLACK);
        g.drawOval(x, y, 10, 10);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Horiz Orientation
    ////////////////////////////////////////////////////////////////////////////
    private void drawHorizontalOutgoing(Graphics g) {
        g.setColor(components.BUS_BAR_COLOR);
        g.drawLine(0, 20, 20, 20);
        g.drawLine(18, 18, 22, 22);
        g.drawLine(18, 22, 22, 18);
    }

    private void drawHorizontalOpenContact(Graphics g) {
        //Paint over Closed Contact
        g.setColor(components.BACKGROUND_REPAINT_COLOR);
        g.drawLine(20, 22, 50, 20);
        g.drawLine(50, 20, 70, 20);

        //Paint open contact
        g.setColor(components.BUS_BAR_COLOR);
        g.drawLine(20, 35, 50, 20);
        g.drawLine(50, 20, 70, 20);
    }

    private void drawHorizontalClosedContact(Graphics g) {
        //Paint over open Contact
        g.setColor(components.BACKGROUND_REPAINT_COLOR);
        g.drawLine(20, 35, 50, 20);
        g.drawLine(50, 20, 70, 20);

        //Paint closed contact
        g.setColor(components.BUS_BAR_COLOR);
        g.drawLine(20, 22, 50, 20);
        g.drawLine(50, 20, 70, 20);
    }

    private void drawBreakerLamp(Graphics g, int x, int y) {
        if (isClosed) {
            g.setColor(Color.RED);
            g.fillOval(x, y, 10, 10);
        } else {
            g.setColor(Color.GREEN);
            g.fillOval(x, y, 10, 10);
        }
        g.setColor(Color.BLACK);
        g.drawOval(x, y, 10, 10);
    }

    public void drawString(String s, Graphics g, int x, int y) {
        //Use graphics 2d to draw string
        Graphics2D g2 = (Graphics2D) g;
        Font font = new Font("Serif", Font.PLAIN, STANDARD_TEXT_FONT_SIZE);
        AttributedString as = new AttributedString(s);
        as.addAttribute(TextAttribute.FONT, font);
        g2.drawString(as.getIterator(), x, y);
    }

    public void drawKW(Graphics g) {
        if (parameterMap.get("Show_KW").toString().compareTo("true") == 0) {
            try {
                if (isLive) {
                    //Get the value
                    HashMap hm = (HashMap) parameterMap.get("KW");
                    String label = hm.get("32integer").toString();
                    Double value = Double.valueOf(label);
                    Double scaleFactor = Double.valueOf((String) parameterMap.get("KW_Scale_Factor"));
                    Double scaledValue = value * scaleFactor;


                    //Sort out the decimal places
                    String result = Double.toString(scaledValue);
                    int decPointPos = result.indexOf(".");
                    int DecimalPlaces = Integer.valueOf((String) parameterMap.get("DecimalPlaces"));

                    if (DecimalPlaces == 0) {
                        result = result.substring(0, decPointPos);
                    }
                    if (DecimalPlaces == 1) {
                        String intresult = result.substring(0, decPointPos);
                        String decimals = result.substring(decPointPos);
                        
                        while (decimals.length() < 1) {
                            decimals = decimals + "0";
                        }
                        result = intresult + decimals;
                    }

                    label = result + " " + parameterMap.get("KW_Label").toString();
                    drawString(label, g, 25, 60);
                }
            } catch (NullPointerException npe) {
                String label = "Err kW";
                drawString(label, g, 25, 60);
            }
        }
    }

    public void drawWarning(Graphics g) {
        if (!isLive) {
            Image i = new ImageIcon(getClass().getResource("resources/alert_icon_warning.png")).getImage();
            g.drawImage(i, 35, 30, null);
        } else {
            //paint over
            //g.clearRect(35, 30, 30, 30);
        }
    }

    @Override
    public void paint(Graphics g) {
        drawString(parameterMap.get("ComponentName").toString(), g, 30, 10);

        if (parameterMap.get("VerticalOrientation").toString().compareTo("true") == 0) {
            drawVerticalOutgoing(g);
            drawBreakerLamp(g, 5, 10);
            if (!isClosed) {
                drawVerticalOpenContact(g);
            } else {
                drawVerticalClosedContact(g);
            }
        } else {
            drawHorizontalOutgoing(g);
            drawBreakerLamp(g, 5, 5);
            if (!isClosed) {
                drawHorizontalOpenContact(g);
            } else {
                drawHorizontalClosedContact(g);
            }
        }

        drawWarning(g);
        drawKW(g);

        if (drawBorder) {
            drawBorder(g, width, height);
        }

        //Update the height and width of component
        width = Integer.valueOf(parameterMap.get("Width").toString());
        height = Integer.valueOf(parameterMap.get("Height").toString());
        this.setSize(width,height);
    }
}
