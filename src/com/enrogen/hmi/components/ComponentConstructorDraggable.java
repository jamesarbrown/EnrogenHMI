/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.enrogen.hmi.components;

import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
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

class ComponentConstructorDraggable extends ComponentConstructorClickable implements DragGestureListener, DragSourceListener {

  DragSource dragSource;

  public ComponentConstructorDraggable() {
    dragSource = new DragSource();
    dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
  }

  public void dragGestureRecognized(DragGestureEvent evt) {
    Transferable t = new StringSelection("aStricdcdcdcdcdcng");
     dragSource.startDrag(evt, DragSource.DefaultCopyDrop, t, this);

  }

  public void dragEnter(DragSourceDragEvent evt) {
      DragSourceContext context = evt.getDragSourceContext();
      //intersection of the users selected action, and the source and target actions
      int myaction = evt.getDropAction();
      if( (myaction & DnDConstants.ACTION_COPY) != 0) {
    context.setCursor(DragSource.DefaultCopyDrop);
      } else {
    context.setCursor(DragSource.DefaultCopyNoDrop);
      }
  }

  @Override
  public void dragOver(DragSourceDragEvent evt) {
    System.out.println("over");
  }

  @Override
  public void dragExit(DragSourceEvent evt) {
    System.out.println("leaves");
  }

  @Override
  public void dropActionChanged(DragSourceDragEvent evt) {
    System.out.println("changes the drag action between copy or move");
  }

  @Override
  public void dragDropEnd(DragSourceDropEvent evt) {
    System.out.println("finishes or cancels the drag operation");
  }
}
