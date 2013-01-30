/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.enrogen.hmi.components;

import java.util.HashMap;
import javax.swing.JPanel;

public class sldPane {
    HashMap parameterMap = new HashMap();

    public JPanel createSLDPane() {
        parameterMap.put("TabName", "Display");
        return createPane();
    }

    public JPanel createSLDPane(String TabTitle) {
        parameterMap.put("TabName", TabTitle);
        return createPane();
    }

    private JPanel createPane() {
        JPanel overviewPanel = new JPanel(); //holding panel
        JPanel gridpane = new JPanel(); //grided panel
        JPanel displaypane = new JPanel(); //the panel to hold components

        overviewPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        overviewPanel.setName("overviewPanel"); // NOI18N

        gridpane.setName("gridpane"); // NOI18N
        gridpane.setOpaque(false);

        javax.swing.GroupLayout gridpaneLayout = new javax.swing.GroupLayout(gridpane);
        gridpane.setLayout(gridpaneLayout);
        gridpaneLayout.setHorizontalGroup(
                gridpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 500, Short.MAX_VALUE));
        gridpaneLayout.setVerticalGroup(
                gridpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 344, Short.MAX_VALUE));

        displaypane.setName("displaypane"); // NOI18N
        displaypane.setOpaque(false);

        javax.swing.GroupLayout displaypaneLayout = new javax.swing.GroupLayout(displaypane);
        displaypane.setLayout(displaypaneLayout);
        displaypaneLayout.setHorizontalGroup(
                displaypaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 500, Short.MAX_VALUE));
        displaypaneLayout.setVerticalGroup(
                displaypaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 344, Short.MAX_VALUE));

        javax.swing.GroupLayout overviewPanelLayout = new javax.swing.GroupLayout(overviewPanel);
        overviewPanel.setLayout(overviewPanelLayout);
        overviewPanelLayout.setHorizontalGroup(
                overviewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(displaypane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(overviewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(gridpane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        overviewPanelLayout.setVerticalGroup(
                overviewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(displaypane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(overviewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(gridpane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        overviewPanel.setOpaque(false);
        return overviewPanel;
    }

    public HashMap getParameters() {
        return parameterMap;
    }

    public HashMap getParametersTypes() {
        HashMap parameterTypes = new HashMap();
        parameterTypes.put("TabName", "String");
        return parameterTypes;
    }
}
