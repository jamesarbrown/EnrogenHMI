/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.enrogen.hmi.components;

import java.awt.Graphics;
import java.awt.Point;
import java.util.HashMap;

/**
 *
 * @author jamesarbrown
 */
public class busbar extends ComponentConstructorBaseComponent {

    public int height = 70; //Design height for this component
    public int width = 70; //Design height for this component

    ////////////////////////////////////////////////////////////////////////////
    // Parameters for the Busbar
    // All Modbus Regsiters to be polled must start "ModbusRegister_"
    ////////////////////////////////////////////////////////////////////////////
    public busbar(String name) {
        setParameterTypes();

        //Create the default hasparameterMapap
        parameterMap.put("ComponentType", "busbar");
        parameterMap.put("ComponentName", name);
        parameterMap.put("TopSection", "true");
        parameterMap.put("BottomSection", "true");
        parameterMap.put("LeftSection", "true");
        parameterMap.put("RightSection", "true");
        parameterMap.put("UpStreamComponent", "");
        parameterMap.put("DownStreamComponent", "");
        parameterMap.put("PositionX", "10");
        parameterMap.put("PositionY", "10");
        parameterMap.put("IncomingSide", "bottom");
    }

    //Overload
    public busbar(HashMap parameters) {
        setParameterTypes();
        parameterMap = parameters;
    }

    private void setParameterTypes() {
        //Create the parameterTypes info
        editableAttributes.put("ComponentType", "readonly");
        editableAttributes.put("ComponentName", "String");
        editableAttributes.put("TopSection", "boolean");
        editableAttributes.put("BottomSection", "boolean");
        editableAttributes.put("LeftSection", "boolean");
        editableAttributes.put("RightSection", "boolean");
        editableAttributes.put("UpStreamComponent", "String");
        editableAttributes.put("DownStreamComponent", "String");
        editableAttributes.put("PositionX", "readonly");
        editableAttributes.put("PositionY", "readonly");
        editableAttributes.put("IncomingSide", "drop,top,bottom,left,right");
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
    public void drawTop(Graphics g) {
        g.setColor(components.BUS_BAR_COLOR);
        g.drawLine(20, 0, 20, 20);
    }

    public void drawBottom(Graphics g) {
        g.setColor(components.BUS_BAR_COLOR);
        g.drawLine(20, 20, 20, 70);
    }

    public void drawLeft(Graphics g) {
        g.setColor(components.BUS_BAR_COLOR);
        g.drawLine(0, 20, 20, 20);
    }

    public void drawRight(Graphics g) {
        g.setColor(components.BUS_BAR_COLOR);
        g.drawLine(20, 20, 70, 20);
    }

    @Override
    public void paint(Graphics g) {
        if (parameterMap.get("TopSection").toString().compareTo("true") == 0) {
            drawTop(g);
        }
        if (parameterMap.get("BottomSection").toString().compareTo("true") == 0) {
            drawBottom(g);
        }
        if (parameterMap.get("LeftSection").toString().compareTo("true") == 0) {
            drawLeft(g);
        }
        if (parameterMap.get("RightSection").toString().compareTo("true") == 0) {
            drawRight(g);
        }

        if (drawBorder) {
            drawBorder(g, width, height);
        }
    }
}
