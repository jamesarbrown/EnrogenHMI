//////////////////////////////////////////////////////////////////////////
//com.enrogen.hmi.components.analoguemeter.java
//2010 - James A R Brown
//Released under GPL V2
//////////////////////////////////////////////////////////////////////////
package com.enrogen.hmi.components;

import com.enrogen.java2d.ShapeStroke;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import javax.swing.ImageIcon;

public class analoguemeter extends ComponentConstructorBaseComponent {

    public int height = 150; //Design height for this component
    public int width = 150; //Design width for this component

    ////////////////////////////////////////////////////////////////////////////
    // Parameters for the Meter
    ////////////////////////////////////////////////////////////////////////////
    public analoguemeter(String name) {
        setParameterTypes();

        //Create the default hasparameterMapap
        parameterMap.put("ComponentType", "analoguemeter");
        parameterMap.put("LowValue", "0");
        parameterMap.put("HighValue", "120");
        parameterMap.put("PositionX", "10");
        parameterMap.put("PositionY", "10");
        parameterMap.put("Name", "Meter");
        parameterMap.put("Units", "%");
        parameterMap.put("SlaveID", "0");
        parameterMap.put("ScalingFactor", "1");
        parameterMap.put("is32bit", "false");
        parameterMap.put("IsColourBarVisible", "true");
        parameterMap.put("ModbusRegister_CurrentValue", "0");
        parameterMap.put("CurrentValue", new HashMap());
    }

    //Overload
    public analoguemeter(HashMap parameters) {
        setParameterTypes();
        parameterMap = parameters;

        parameterMap.put("CurrentValue", new HashMap());
    }

    private void setParameterTypes() {
        //Create the parameterTypes info
        editableAttributes.put("ComponentType", "readonly");
        editableAttributes.put("LowValue", "int");
        editableAttributes.put("HighValue", "int");
        editableAttributes.put("PositionX", "readonly");
        editableAttributes.put("PositionY", "readonly");
        editableAttributes.put("Name", "String");
        editableAttributes.put("Units", "String");
        editableAttributes.put("SlaveID", "int");
        editableAttributes.put("ScalingFactor", "long");
        editableAttributes.put("IsColourBarVisible", "boolean");
        editableAttributes.put("is32bit", "boolean");
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

    public Double getCurrentValue() {
        HashMap hm = (HashMap) parameterMap.get("CurrentValue");
        try {
            boolean is32bit = Boolean.valueOf((String) hm.get("is32bit"));
            String valueString = "";

            if (is32bit) {
                valueString = (String) hm.get("32integer");
            } else {
                valueString = (String) hm.get("16integer");
            }

            Integer value = Integer.valueOf(valueString);
            Double scaleFactor = Double.valueOf((String) parameterMap.get("ScalingFactor"));
            Double scaledValue = value * scaleFactor;

            //Remove lots of useless decimal places
            int decimalplaces = getNumberOfDecimalPlace(scaleFactor);
            scaledValue = round(scaledValue, decimalplaces);

            //Check high and low
            Double lowValue = Double.valueOf((String) parameterMap.get("LowValue"));
            Double HighValue = Double.valueOf((String) parameterMap.get("HighValue"));

            if (scaledValue < lowValue) {
                scaledValue = lowValue;
            }
            if (scaledValue > HighValue) {
                scaledValue = HighValue;
            }

            return scaledValue;
        } catch (Exception e) {
            isLive = false;
            return 0.00;
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

    public void drawScale(Graphics2D g2) {
        if (Boolean.valueOf((String) parameterMap.get("IsColourBarVisible"))) {
            //Draw the colour bar
            g2.setStroke(new BasicStroke(6));
            g2.setColor(Color.red);
            g2.drawArc(15, 15, 120, 120, -45, 20);
            g2.setColor(Color.orange);
            g2.drawArc(15, 15, 120, 120, -20, 20);
        }

        //Draw Outer Arc
        g2.setStroke(new BasicStroke(3));
        g2.setColor(new Color(85, 91, 107));
        g2.drawArc(15, 15, 120, 120, -45, 270);

        //Draw the scale marks
        g2.setStroke(
                new ShapeStroke(
                new Shape[]{
                    new Rectangle2D.Float(0, 0, 5, 7),
                    new Ellipse2D.Float(0, 0, 3, 5)
                },
                23.4f));

        g2.drawArc(15, 15, 120, 120, -45, 270);

        //Draw the values high to low
        g2.setFont(new Font("SansSerif", Font.PLAIN, 8));

        int lowValue = Integer.valueOf((String) parameterMap.get("LowValue"));
        int HighValue = Integer.valueOf((String) parameterMap.get("HighValue"));
        int range = HighValue - lowValue;
        int eachMark = range / 6;

        //Work out required positioning info
        int Diameter = 55;
        int center_x = 75;
        int center_y = 75;
        Double x = Diameter * Math.sin(Math.toRadians(45));
        Double y = Diameter * Math.cos(Math.toRadians(45)); //same as sin45 we are 45deg
        int offset_x = x.intValue();
        int offset_y = y.intValue();

        //Font metrics so we can position more accurately
        FontMetrics metrics = g2.getFontMetrics();
        int fontHeight = metrics.getHeight() / 2 - 1;

        //Draw Labels
        String label = String.valueOf(lowValue);
        g2.drawString(label,
                center_x - offset_x,
                center_y + offset_y + fontHeight);

        label = String.valueOf(lowValue + (eachMark * 1));
        g2.drawString(label,
                center_x - Diameter + 2,
                center_y + fontHeight);

        label = String.valueOf(lowValue + (eachMark * 2));
        g2.drawString(label,
                center_x - offset_x,
                center_y - offset_y + fontHeight);

        label = String.valueOf(lowValue + (eachMark * 3));
        g2.drawString(label,
                center_x - metrics.stringWidth(label) / 2,
                center_y - Diameter + fontHeight + 2);

        label = String.valueOf(lowValue + (eachMark * 4));
        g2.drawString(label,
                center_x + offset_x - metrics.stringWidth(label),
                center_y - offset_y + fontHeight);

        label = String.valueOf(lowValue + (eachMark * 5));
        g2.drawString(label,
                center_x + Diameter - metrics.stringWidth(label),
                center_y + fontHeight);

        label = String.valueOf(lowValue + (eachMark * 6));
        g2.drawString(label,
                center_x + offset_x - metrics.stringWidth(label),
                center_y + offset_y + fontHeight);
    }

    public void drawLabel(Graphics2D g2) {
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        FontMetrics metrics = g2.getFontMetrics();
        String value = (String) parameterMap.get("Name");
        g2.drawString(value, 75 - metrics.stringWidth(value) / 2, 60);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        metrics = g2.getFontMetrics();
        value = (String) parameterMap.get("Units");
        g2.drawString(value, 75 - metrics.stringWidth(value) / 2, 110);
    }

    public void drawNeedle(Graphics2D g) {
        //Draw the pointer
        Double value = getCurrentValue();

        //Scale low and high
        int lowValue = Integer.valueOf((String) parameterMap.get("LowValue"));
        int HighValue = Integer.valueOf((String) parameterMap.get("HighValue"));
        int range = HighValue - lowValue;
        Double positionAboveLowValue = value - lowValue;
        Double percentageOnDial = positionAboveLowValue / range;

        //Convert to an angle on dial
        double angle = 270 * percentageOnDial;
        //Adjust for image rotation
        angle = angle - 45;

        //Center of needle x=67 y=9
        Image i = new ImageIcon(getClass().getResource("resources/needle.png")).getImage();
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.setToTranslation(8, 65);
        affineTransform.rotate(Math.toRadians(angle), 67, 9);
        g.drawImage(i, affineTransform, this);
    }

    public void drawValue(Graphics2D g2) {
        g2.setFont(getFontDigital());
        FontMetrics metrics = g2.getFontMetrics();
        String value = String.valueOf(getCurrentValue());
        int fontWidth = metrics.stringWidth(value);
        g2.drawString(value, 75 - fontWidth / 2, 130);
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

        drawBackground(g2);
        drawScale(g2);
        drawLabel(g2);
        drawNeedle(g2);
        drawValue(g2);

        if (drawBorder) {
            g2.setStroke(new BasicStroke(1));
            drawBorder(g2, width, height);
        }
    }
}
