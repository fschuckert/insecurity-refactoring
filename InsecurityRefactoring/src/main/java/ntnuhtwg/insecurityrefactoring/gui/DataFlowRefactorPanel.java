/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui;

import java.awt.Dimension;
import java.awt.Label;
import java.util.Collections;
import java.util.HashMap;
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
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.DataflowPattern;
import ntnuhtwg.insecurityrefactoring.base.context.Context;
import org.javatuples.Pair;

/**
 *
 * @author blubbomat
 */
public class DataFlowRefactorPanel extends JPanel{
    
    private DFATreeNode node;
    
    private JComboBox<DataflowPattern> selectedRefactorPattern = new JComboBox<>();
    private NoneDataFlowPattern none = new NoneDataFlowPattern();

    public DataFlowRefactorPanel(DFATreeNode node, DataflowDSL dsl) {        
        this.node = node;
        
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        
        SourceLocation location = Util.codeLocation(dsl.getDb(), node.getObj());
        this.add(new Label("Dataflow"));
        this.add(new Label(location.toString()));
//        this.add(new JLabel(node.toString()));
        
        selectedRefactorPattern.addItem(none);
        selectedRefactorPattern.setMaximumSize(new Dimension(500, 50));
        
        for(DataflowPattern dataflowPattern : node.getPossibleDataflowReplacements()){
            selectedRefactorPattern.addItem(dataflowPattern);
        }
        this.add(selectedRefactorPattern);
    }

    public Pair<DFATreeNode, DataflowPattern> getSelectedDataflowPattern() {
        DataflowPattern selectedPattern = (DataflowPattern)selectedRefactorPattern.getSelectedItem();
        
        if(selectedPattern instanceof NoneDataFlowPattern){
            return null;
        }
        
        return new Pair<>(node, selectedPattern);
    }
    
    
    
    private class NoneDataFlowPattern extends DataflowPattern{

        public NoneDataFlowPattern() {
            super(false, null, null, null, 0.0, 0.0, 0.0);
        }
        
        

        @Override
        public String toString() {
            return "No refactoring...";
        }
        
    }
}
