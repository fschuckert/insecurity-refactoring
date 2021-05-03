/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui;

import java.awt.Dimension;
import java.awt.Label;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.SourceLocation;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher.DataflowDSL;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.FailedSanitizePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SanitizePattern;
import ntnuhtwg.insecurityrefactoring.base.context.Context;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.sufficient.VulnSufficient;
import org.javatuples.Pair;
import org.javatuples.Triplet;

/**
 *
 * @author blubbomat
 */
public class SanitizeRefactorPanel extends JPanel{
    
    private SanitizePattern sanitize;
    private DFATreeNode node;
    private JComboBox<SanitizePattern> failedSanPatterns = new JComboBox<>();

    public SanitizeRefactorPanel() {
        
        
    }

    SanitizeRefactorPanel(Pair<SanitizePattern, DFATreeNode> sanitizeNodePair, List<SanitizePattern> possiblePatterns, DataflowDSL dsl) {
        this.sanitize = sanitizeNodePair.getValue0();
        this.node = sanitizeNodePair.getValue1();
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        
        SourceLocation location = Util.codeLocation(dsl.getDb(), node.getObj());
        this.add(new Label(location.toString()));

        
        for(SanitizePattern pattern : possiblePatterns){
            failedSanPatterns.addItem(pattern);
        }
        
        failedSanPatterns.addItem(new NoChangePattern());
        
        failedSanPatterns.setMaximumSize(new Dimension(500, 50));

        this.add(failedSanPatterns);
    }

    Triplet<DFATreeNode, SanitizePattern, SanitizePattern> getRefactorData() {
        if(failedSanPatterns.getSelectedItem() instanceof NoChangePattern){
            return null;
        }
        
        return new Triplet<>(node, sanitize, (SanitizePattern)failedSanPatterns.getSelectedItem());
    }
    
    
    private class NoChangePattern extends SanitizePattern{

        public NoChangePattern() {
            super(false, null, null, new LinkedList<VulnSufficient>());
        }
        
        
        
        @Override
        public String toString() {
            return "No changes...";
        }
        
        
    }
}
