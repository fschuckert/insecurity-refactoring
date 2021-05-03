/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import ntnuhtwg.insecurityrefactoring.Framework;
import ntnuhtwg.insecurityrefactoring.base.RefactoredCode;
import ntnuhtwg.insecurityrefactoring.base.exception.TimeoutException;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher.DataflowDSL;
import ntnuhtwg.insecurityrefactoring.base.info.ContextInfo;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.DataflowPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.FailedSanitizePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.InsecureSourcePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SanitizePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;
import ntnuhtwg.insecurityrefactoring.base.tools.CodeFormatter;
import ntnuhtwg.insecurityrefactoring.refactor.InsecurityRefactoring;
import org.javatuples.Pair;
import org.javatuples.Triplet;

/**
 *
 * @author blubbomat
 */
public class RefactoringRenderer extends JPanel{
    
    JPanel west = new JPanel();
    
//    PatternStorage patternStorage;
////    Refactoring refactoring;
//    InsecurityRefactoring refactoring;
//    DataflowDSL dsl;
    
    Framework framework;
    
    
    
    private List<SanitizeRefactorPanel> failedSanitizeSelections = new LinkedList<>();
    private List<DataFlowRefactorPanel> dataflowSelections = new LinkedList<>();
    private RefactorPanel secureSources = null;
    
    JTabbedPane sourceCodePreview = new JTabbedPane();    
    

    public RefactoringRenderer(Framework framework) {
        this.setLayout(new BorderLayout());
        this.framework = framework;
        west.setLayout(new BoxLayout(west, BoxLayout.Y_AXIS));
        west.add(new JLabel("Test"));
        this.add(west, BorderLayout.WEST);
        
        this.add(sourceCodePreview, BorderLayout.CENTER);
    }
    
    public void refresh(DFATreeNode source, List<Pair<SanitizePattern, DFATreeNode>> sanitizeNodes, ContextInfo contextInfo){
//        west.setLayout(new FlowLayout());
        
        west.removeAll();
        failedSanitizeSelections.clear();
        dataflowSelections.clear();
        secureSources = null;
        
        DFATreeNode node = source;
        west.add(new JLabel("Secure sources"));
        if(source.getSourcePattern().isSecure()){            
            List<InsecureSourcePattern> insecurePattern = framework.getPatternStorage().getPossibleInsecureSource(source.getSourcePattern());
            RefactorPanel refactorPanel = new RefactorPanel(node, framework.getDSL(), insecurePattern);
            west.add(refactorPanel);
            secureSources = refactorPanel;
        }
        
        west.add(new JLabel("Dataflow patterns"));
        while(node != null){
            if(!node.getPossibleDataflowReplacements().isEmpty()){
                DataFlowRefactorPanel dataFlowRefactorPanel = new DataFlowRefactorPanel(node, framework.getDSL());
                west.add(dataFlowRefactorPanel);
                dataflowSelections.add(dataFlowRefactorPanel);
            }
            
            node = node.getParent_();
        }
        
        west.add(new JLabel("Sanitize patterns"));
        
        
        for(Pair<SanitizePattern, DFATreeNode> sanitizeNodePair : sanitizeNodes){
            List<SanitizePattern> possiblePatterns = framework.getPatternStorage().getPossibleFailedSanitizePatterns(sanitizeNodePair.getValue0(), sanitizeNodePair.getValue1(), contextInfo);
            SanitizeRefactorPanel sanitizeRefactorPanel = new SanitizeRefactorPanel(sanitizeNodePair, possiblePatterns, framework.getDSL());
            failedSanitizeSelections.add(sanitizeRefactorPanel);
            
            west.add(sanitizeRefactorPanel);
        }
        
        JButton refactor = new JButton("1: refactoring");
        refactor.addActionListener((arg0) -> {
            refactor();            
        });
        west.add(refactor);
        
        JButton writeFiles = new JButton("2: Write to disk");
        writeFiles.addActionListener((arg0) -> {
            boolean backupFiles = false;
            framework.writeToDisk(backupFiles);
        });
        west.add(writeFiles);
        
        JButton formatCode = new JButton("3: Format source code");
        formatCode.addActionListener((arg0) -> {
            int dialogResult = JOptionPane.showConfirmDialog (null, "Do you want to format the code? It will require new scanning for further refactoring.");
            if(dialogResult == JOptionPane.YES_OPTION){
              // Saving code here
                framework.formatCode();
            }
        });
        west.add(formatCode);
        
        JButton push = new JButton("Push to git");
        push.addActionListener((arg0) -> {
            String msg = JOptionPane.showInputDialog(null);
            System.out.println("Msg: " + msg);
            if(msg != null){
                framework.commitAndPush(msg);
            }
        });
        west.add(push);
        
    }
    
    
    private void refactor(){
        List<Triplet<DFATreeNode, SanitizePattern, SanitizePattern>> refactoringData = new LinkedList<>();
        for(SanitizeRefactorPanel sanitizePanel : failedSanitizeSelections){
            if(sanitizePanel.getRefactorData() != null){
                refactoringData.add(sanitizePanel.getRefactorData());
            }
        }

        List<Pair<DFATreeNode, DataflowPattern>> dataflowRefactoring = new LinkedList<>();

        for(DataFlowRefactorPanel dataflowPanel : dataflowSelections){
            Pair<DFATreeNode, DataflowPattern> dataflowPattern = dataflowPanel.getSelectedDataflowPattern();
            if(dataflowPattern != null){
                dataflowRefactoring.add(dataflowPattern);
            }
        }

        System.out.println("Start refactoring");
        Pair<DFATreeNode, InsecureSourcePattern> secureSourcePattern = null;
        System.out.println("secure sources " + secureSourcePattern);
        if(secureSources != null){
            
            secureSourcePattern = secureSources.getSelectedDataflowPattern();
        }
        List<RefactoredCode> refactoredCodes;
        try {
            framework.selectedRefactoring(refactoringData, dataflowRefactoring, secureSourcePattern);
            refactoredCodes = framework.getRefactoredCode();
        } catch (TimeoutException ex) {
            Logger.getLogger(RefactoringRenderer.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        
        
        sourceCodePreview.removeAll();
        
        for(RefactoredCode refactoredCode : refactoredCodes){
            RefactoringComparison refactoringComparison = new RefactoringComparison(refactoredCode.getSourceLocation(), refactoredCode.getCode(), refactoredCode.getModifiedLines());
            sourceCodePreview.addTab(refactoredCode.getSourceLocation().getPath(), refactoringComparison);
        }
    }
    
}
