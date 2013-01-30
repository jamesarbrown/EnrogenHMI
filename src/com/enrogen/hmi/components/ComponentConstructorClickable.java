/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.enrogen.hmi.components;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JPanel;

////////////////////////////////////////////////////////////////////////////////
// Extending Class Build Up
// ------------------------
// JPanel
// > ComponentConstructorDraggable
// >> ComponentConstructorClickable
// >>> ComponentConstructorBaseComponent
// >>>> Custom Component (breaker etc)
////////////////////////////////////////////////////////////////////////////////
public class ComponentConstructorClickable extends JPanel implements MouseListener {
    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }
    
    @Override
    public void mouseClicked(MouseEvent evt) {
    }
}
