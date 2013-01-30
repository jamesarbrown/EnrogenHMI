package com.enrogen.hmi.components;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.HashMap;

public class label extends ComponentConstructorBaseComponent {

    public int height = 70; //Design height for this component
    public int width = 150; //Design width for this component

    ////////////////////////////////////////////////////////////////////////////
    // Parameters for the Label
    ////////////////////////////////////////////////////////////////////////////
    public label(String name) {
        setParameterTypes();

        //Create the default hasparameterMapap
        parameterMap.put("ComponentType", "label");
        parameterMap.put("Text", name);
        parameterMap.put("FontSize", "10");
        parameterMap.put("PositionX", "10");
        parameterMap.put("PositionY", "10");
        parameterMap.put("Width", "70");
        parameterMap.put("Height", "150");
        parameterMap.put("Underline", "true");
        parameterMap.put("Bold", "true");
    }

    //Overload
    public label(HashMap parameters) {
        setParameterTypes();
        parameterMap = parameters;

        //Update the height and width of component
        width = Integer.valueOf(parameterMap.get("Width").toString());
        height = Integer.valueOf(parameterMap.get("Height").toString());
    }

    private void setParameterTypes() {
        //Create the parameterTypes info
        editableAttributes.put("ComponentType", "readonly");
        editableAttributes.put("Text", "String");
        editableAttributes.put("FontSize", "int");
        editableAttributes.put("PositionX", "readonly");
        editableAttributes.put("PositionY", "readonly");
        editableAttributes.put("Width", "int");
        editableAttributes.put("Height", "int");
        editableAttributes.put("Underline", "boolean");
        editableAttributes.put("Bold", "boolean");
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

    ////////////////////////////////////////////////////////////////////////////
    // Drawing
    ////////////////////////////////////////////////////////////////////////////
    public void drawLabel(Graphics2D g2) {
        //Get the parameters for the label
        String text = (String) parameterMap.get("Text");
        int fontSize = Integer.valueOf((String) parameterMap.get("FontSize"));
        Boolean isUnderlined = Boolean.valueOf((String) parameterMap.get("Underline"));
        Boolean isBold = Boolean.valueOf((String) parameterMap.get("Bold"));

        //Java2d with rendering issues?!
        int fontEffect = Font.PLAIN;
        if (isBold) {
            fontEffect = Font.BOLD;
        }

        Font font = new Font("SansSerif", fontEffect, fontSize);

        AttributedString as = new AttributedString(text);
        as.addAttribute(TextAttribute.FONT, font);
        if (isUnderlined) {
            as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, 0, text.length());
        }

        g2.drawString(as.getIterator(), 0, fontSize + 2);
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

        drawLabel(g2);

        if (drawBorder) {
            drawBorder(g2, width, height);
        }

        //Update the height and width of component
        width = Integer.valueOf(parameterMap.get("Width").toString());
        height = Integer.valueOf(parameterMap.get("Height").toString());
        this.setSize(width, height);
    }

    //Method only called after edit window has been used
    @Override
    public void reDrawOnce() {
    }
}
