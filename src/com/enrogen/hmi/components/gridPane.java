/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.enrogen.hmi.components;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 *
 * @author jamesarbrown
 */
public class gridPane extends JPanel {

    private int gridwidth;
    private int gridheight;
    private int grid_x;
    private int grid_y;

    public gridPane(int width, int height, int gridx, int gridy) {
        gridwidth = width;
        gridheight = height;
        grid_x = gridx;
        grid_y = gridy;
        this.setSize(width, height);
    }

    @Override
    public void paint(Graphics g) {
        for (int i = 0; i < gridwidth;) {
            g.setColor(components.DESIGN_GRID_COLOR);
            g.drawLine(i, 0, i, gridheight);
            i = i + grid_x;
        }
        for (int i = 0; i < gridheight;) {
            g.setColor(components.DESIGN_GRID_COLOR);
            g.drawLine(0, i, gridwidth, i);
            i = i + grid_y;
        }
    }
}
