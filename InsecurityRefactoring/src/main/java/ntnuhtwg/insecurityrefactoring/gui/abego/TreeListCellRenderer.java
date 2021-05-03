/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui.abego;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import org.abego.treelayout.util.DefaultTreeForTreeLayout;

/**
 *
 * @author Felix Schuckert
 */
public class TreeListCellRenderer extends DefaultListCellRenderer{
    public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus ) {
        Object item = value;

        // if the item to be rendered is Proveedores then display it's Name
        if( item instanceof DefaultTreeForTreeLayout ) {
            DefaultTreeForTreeLayout tree = (DefaultTreeForTreeLayout)item;
            item = tree.getRoot().toString();
        }
        return super.getListCellRendererComponent( list, item, index, isSelected, cellHasFocus);
    }
    
}
