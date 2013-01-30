package com.enrogen.hmi.components;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.util.HashMap;

public class generator extends ComponentConstructorBaseComponent {

    public int height = 70; //Design height for this component
    public int width = 70; //Design height for this component

    ////////////////////////////////////////////////////////////////////////////
    // Parameters for the Busbar
    // All Modbus Regsiters to be polled must start "ModbusRegister_"
    ////////////////////////////////////////////////////////////////////////////
    public generator(String name) {
        setParameterTypes();

        //Create the default hasparameterMapap
        parameterMap.put("ComponentType", "generator");
        parameterMap.put("ComponentName", name);
        parameterMap.put("DownStreamComponent", "");
        parameterMap.put("PositionX", "10");
        parameterMap.put("PositionY", "10");
    }

    //Overload
    public generator(HashMap parameters) {
        setParameterTypes();
        parameterMap = parameters;
    }

    private void setParameterTypes() {
        //Create the parameterTypes info
        editableAttributes.put("ComponentType", "readonly");
        editableAttributes.put("ComponentName", "String");
        editableAttributes.put("DownStreamComponent", "String");
        editableAttributes.put("PositionX", "readonly");
        editableAttributes.put("PositionY", "readonly");
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
    public void drawGen(Graphics g) {
        g.setColor(components.BUS_BAR_COLOR);
        g.drawOval(5, 20, 30, 30);
        g.drawLine(20, 50, 20, 70);
        Font plainFont = new Font("Serif", Font.BOLD, 14);
        g.drawString("G", 15, 40);
    }

    @Override
    public void paint(Graphics g) {
        drawGen(g);

        if (drawBorder) {
            drawBorder(g, width, height);
        }
    }
}
