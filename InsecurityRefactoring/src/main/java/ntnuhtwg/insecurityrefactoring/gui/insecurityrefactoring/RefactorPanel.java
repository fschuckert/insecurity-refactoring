/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui.insecurityrefactoring;

import java.awt.Dimension;
import java.awt.Label;
import java.util.Collections;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.SourceLocation;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher.DataflowDSL;
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.DataflowPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SourcePattern;
import org.javatuples.Pair;

/**
 *
 * @author blubbomat
 */
public class RefactorPanel extends JPanel{
    
    private DFATreeNode node;
    
    private JComboBox<SourcePattern> refactorSource = new JComboBox<>();
    private NoRefactoring none = new NoRefactoring();

    public RefactorPanel(DFATreeNode node, DataflowDSL dsl, List<SourcePattern> replacements) {        
        this.node = node;
        
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        
        SourceLocation location = Util.codeLocation(dsl.getDb(), node.getObj());
        
        this.add(new Label("Insecure source"));
        this.add(new Label(location.toString()));
//        this.add(new JLabel(node.toString()));
        
        refactorSource.addItem(none);
        refactorSource.setMaximumSize(new Dimension(500, 50));
        
        for(SourcePattern dataflowPattern : replacements){
            refactorSource.addItem(dataflowPattern);
        }
        this.add(refactorSource);
    }

    public Pair<DFATreeNode, SourcePattern> getSelectedDataflowPattern() {
        SourcePattern selectedPattern = (SourcePattern)refactorSource.getSelectedItem();
        
        if(selectedPattern instanceof NoRefactoring){
            return null;
        }
        
        return new Pair<>(node, selectedPattern);
    }
    
    
    
    private class NoRefactoring extends SourcePattern{

        public NoRefactoring() {
            super(null);
        }
        
        

        @Override
        public String toString() {
            return "No refactoring...";
        }
        
    }
}
