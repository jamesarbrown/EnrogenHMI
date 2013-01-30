/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.enrogen.hmi.components;

import java.awt.Color;

/**
 *
 * @author jamesarbrown
 */
public interface components {

    public static final Color BUS_BAR_COLOR = Color.BLACK;
    public static final Color DESIGN_GRID_COLOR = Color.CYAN;
    public static final Color BACKGROUND_REPAINT_COLOR = new Color(237,236,235); //This is to ensure overpainting is right color.

    public static final int EDITOR_BOX_HEIGHT = 800;
    public static final int EDITOR_BOX_WIDTH =800;
    public static final int EDITOR_BOX_ROWHEIGHT = 30;
    public static final int EDITOR_BOX_ROWWIDTH = 550;
    
    public static final int MAX_ALARMS_PERSLAVE = 50;

    public static final int STANDARD_TEXT_FONT_SIZE = 12;

    public static final int MYSQL_MODBUS_DATA_CURRENT = 15; //sec

}
