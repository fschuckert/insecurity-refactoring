/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui.abego;


import java.awt.FontMetrics;
import org.abego.treelayout.NodeExtentProvider;

/**
 * For showing the tree using abego requires to implement a NodeExtendProvider
 * to get the right size of the nodes.
 * @author Felix Schuckert
 */
public class StringNodeExtentProvider implements NodeExtentProvider<StringNode>{
    
    private FontMetrics fontMetrics;

    public void setFontMetrics(FontMetrics fontMetrics) {
        this.fontMetrics = fontMetrics;
    }

    
    /**
     * this method will return the width for a showing node. Caluclated using
     * the string size and the font size
     * @param tn
     * @return 
     */
    @Override
    public double getWidth(StringNode tn) {
        return tn.getMaxCharacterLength(fontMetrics);
    }

    /**
     * this will return the height of the AST node.
     * @param tn
     * @return 
     */
    @Override
    public double getHeight(StringNode tn) {
        return tn.getNumberOfLines() * 20f;
    }
    
}
