//////////////////////////////////////////////////////////////////////////
//com.enrogen.hmi.components.analoguemeter.java
//2010 - James A R Brown
//Released under GPL V2
//////////////////////////////////////////////////////////////////////////
package com.enrogen.hmi.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import javax.swing.ImageIcon;

public class digitalmeter extends ComponentConstructorBaseComponent {

    public int height = 30; //Design height for this component
    public int width = 100; //Design width for this component

    ////////////////////////////////////////////////////////////////////////////
    // Parameters for the Meter
    ////////////////////////////////////////////////////////////////////////////
    public digitalmeter(String name) {
        setParameterTypes();

        //Create the default hasparameterMapap
        parameterMap.put("ComponentType", "digitalmeter");
        parameterMap.put("PositionX", "10");
        parameterMap.put("PositionY", "10");
        parameterMap.put("Name", "Meter");
        parameterMap.put("Units", "%");
        parameterMap.put("SlaveID", "0");
        parameterMap.put("ScalingFactor", "1");
        parameterMap.put("is32bit", "false");
        parameterMap.put("MaxValue", "100");
        parameterMap.put("MinValue", "0");
        parameterMap.put("ModbusRegister_CurrentValue", "0");
        parameterMap.put("CurrentValue", new HashMap());
    }

    //Overload
    public digitalmeter(HashMap parameters) {
        setParameterTypes();
        parameterMap = parameters;
        parameterMap.put("CurrentValue", new HashMap());
    }

    private void setParameterTypes() {
        //Create the parameterTypes info
        editableAttributes.put("ComponentType", "readonly");
        editableAttributes.put("PositionX", "readonly");
        editableAttributes.put("PositionY", "readonly");
        editableAttributes.put("Name", "String");
        editableAttributes.put("Units", "String");
        editableAttributes.put("SlaveID", "int");
        editableAttributes.put("ScalingFactor", "long");
        editableAttributes.put("is32bit", "boolean");
        editableAttributes.put("MaxValue", "long");
        editableAttributes.put("MinValue", "long");
        editableAttributes.put("ModbusRegister_CurrentValue", "int");
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

    public String getCurrentValue() {
        HashMap hm = (HashMap) parameterMap.get("CurrentValue");
        try {
            boolean is32bit = Boolean.valueOf((String) hm.get("is32bit"));
            String valueString = "0";

            if (is32bit) {
                valueString = (String) hm.get("32integer");
            } else {
                valueString = (String) hm.get("16integer");

            }

            //Scale Value per scale factor
            Integer value = Integer.valueOf(valueString);
            Double scaleFactor = Double.valueOf((String) parameterMap.get("ScalingFactor"));
            Double scaledValue = value * scaleFactor;

            //Remove lots of useless decimal places
            int decimalplaces = getNumberOfDecimalPlace(scaleFactor);
            scaledValue = round(scaledValue, decimalplaces);

            String StrValue = "";

            //Get max and min
            Double max = Double.valueOf((String) parameterMap.get("MaxValue"));
            Double min = Double.valueOf((String) parameterMap.get("MinValue"));

            //Blank out if out of range
            if (scaledValue>max) {
                StrValue = "---";
            } else {
                if (scaledValue < min) {
                    StrValue = "---";
                } else {
                    StrValue = Double.toString(scaledValue);
                }
            }

            //Check data is alive
            if (!isLive) {
                StrValue = "---";
            }

            return StrValue;
        } catch (Exception e) {
            isLive = false;
            return "---";
        }
    }

    public static double round(double d, int decimalPlace) {
        // see the Javadoc about why we use a String in the constructor
        // http://java.sun.com/j2se/1.5.0/docs/api/java/math/BigDecimal.html#BigDecimal(double)
        BigDecimal bd = new BigDecimal(Double.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

    public static int getNumberOfDecimalPlace(double value) {
        //For whole numbers like 0
        if (Math.round(value) == value) {
            return 0;
        }
        final String s = Double.toString(value);
        final int index = s.indexOf('.');
        if (index < 0) {
            return 0;
        }
        return s.length() - 1 - index;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Drawing
    ////////////////////////////////////////////////////////////////////////////
    public void drawBackground(Graphics g) {
        Image i = new ImageIcon(getClass().getResource("resources/gaugeround.png")).getImage();
        g.drawImage(i, 0, 0, null);
    }

    public Font getFontDigital() {
        Font digiFont = null;
        try {
            InputStream fontStream = getClass().getResourceAsStream("resources/DS-DIGIB.TTF");
            Font onePoint = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            fontStream.close();
            digiFont = onePoint.deriveFont(Font.PLAIN, 18);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return digiFont;
    }

    public void drawSurround(Graphics2D g2) {
        g2.setStroke(new BasicStroke(1));
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, width, height);
        g2.setColor(Color.WHITE);
        g2.fillRect(35, 3, width - 38, height - 6);
    }

    public void drawLabel(Graphics2D g2) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.setColor(Color.WHITE);
        FontMetrics metrics = g2.getFontMetrics();
        int fontHeight = metrics.getHeight();
        String value = (String) parameterMap.get("Units");
        g2.drawString(value, 5, (height / 2) + (fontHeight / 2) - 2);
    }

    public void drawValue(Graphics2D g2) {
        g2.setFont(getFontDigital());
        FontMetrics metrics = g2.getFontMetrics();
        String value = getCurrentValue();
        int fontWidth = metrics.stringWidth(value);
        int fontHeight = metrics.getHeight();
        g2.setColor(Color.BLACK);
        g2.drawString(value, width - 5 - fontWidth, (height / 2) + (fontHeight / 2) - 2);

    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        //Anti Aliasing
        RenderingHints renderHints =
                new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        renderHints.put(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHints(renderHints);

        drawSurround(g2);
        drawLabel(g2);
        drawValue(g2);

        if (drawBorder) {
            g2.setStroke(new BasicStroke(1));
            drawBorder(g2, width, height);
        }
    }
}
