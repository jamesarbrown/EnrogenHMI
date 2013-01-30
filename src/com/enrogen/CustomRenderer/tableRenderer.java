/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.enrogen.CustomRenderer;

import java.awt.Color;
import java.awt.Component;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.ToolTipManager;

public class tableRenderer extends DefaultTableCellRenderer {

    private boolean highlightingON = false;
    private List highlightShutdownRowsList = null;
    private List highlightWarningRowsList = null;
    private List highlightTripRowsList = null;
    private List toolTipList = null;

    public void setTableHighlightON() {
        highlightingON = true;
    }

    public void setHighLightShutdownRowsList(List highlightList) {
        highlightShutdownRowsList = highlightList;
    }

    public void setHighLightWarningRowsList(List highlightList) {
        highlightWarningRowsList = highlightList;
    }

    public void setHighLightTripRowsList(List highlightList) {
        highlightTripRowsList = highlightList;
    }

    public void setToolTipList(List TTL) {
        toolTipList = TTL;
    }

    public void setToolTipDelays(int showDelay, int dismissDelay) {
        ToolTipManager.sharedInstance().setInitialDelay(showDelay);
        ToolTipManager.sharedInstance().setDismissDelay(dismissDelay);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {

        Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowIndex, vColIndex);

        if (highlightingON) {
            if (highlightShutdownRowsList.contains(rowIndex)) {
                comp.setForeground(CustomRenderer.TABLE_SHUTDOWN_COLOR);
            } else {
                if (highlightWarningRowsList.contains(rowIndex)) {
                    comp.setForeground(CustomRenderer.TABLE_WARNING_COLOR);
                } else {
                    if (highlightTripRowsList.contains(rowIndex)) {
                        comp.setForeground(CustomRenderer.TABLE_TRIP_COLOR);
                    } else {
                        comp.setForeground(CustomRenderer.TABLE_FOREGROUND_DEFAULT_COLOR);
                        if (isSelected) {
                            comp.setForeground(Color.white);
                        }
                    }
                }
            }
            ;
        }

        setToolTipText(toolTipList.get(rowIndex).toString());

        // cell (and perhaps other cells) are selected
        if (hasFocus) {
            // this cell is the anchor and the table has the focus
        }
        return comp;
    }
}
