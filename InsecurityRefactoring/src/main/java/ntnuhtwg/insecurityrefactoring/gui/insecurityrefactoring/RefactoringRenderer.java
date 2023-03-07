/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui.insecurityrefactoring;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import ntnuhtwg.insecurityrefactoring.Framework;
import ntnuhtwg.insecurityrefactoring.base.RefactoredCode;
import ntnuhtwg.insecurityrefactoring.base.SwingUtil;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.exception.TimeoutException;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher.DataflowDSL;
import ntnuhtwg.insecurityrefactoring.base.info.ContextInfo;
import ntnuhtwg.insecurityrefactoring.base.info.DataflowPathInfo;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.DataflowPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SanitizePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SourcePattern;
import ntnuhtwg.insecurityrefactoring.base.tools.CodeFormatter;
import ntnuhtwg.insecurityrefactoring.gui.DataFlowRefactorPanel;
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
    
    private String title = "";
    
    private List<SanitizeRefactorPanel> failedSanitizeSelections = new LinkedList<>();
    private List<DataFlowRefactorPanel> dataflowSelections = new LinkedList<>();
    private RefactorPanel secureSources = null;
    private Dimension dimension = new Dimension(150, 20);
//    private Dimension nameDimension = new Dimension(200, 20);
    
    JTabbedPane sourceCodePreview = new JTabbedPane();    
    

    public RefactoringRenderer(Framework framework) {
//        this.setBackground(Color.GREEN);
        this.setLayout(new BorderLayout());
        this.framework = framework;
        BoxLayout refactoringPanelLayout = new BoxLayout(west, BoxLayout.Y_AXIS);
        west.setLayout(refactoringPanelLayout);
        west.add(new JLabel("Test"));
        this.add(west, BorderLayout.WEST);
        
        this.add(sourceCodePreview, BorderLayout.CENTER);
        Dimension westDimension = new Dimension(550,2000);
        west.setMaximumSize(westDimension);
        west.setPreferredSize(westDimension);
//        west.setBackground(Color.red);
    }
    
    public String getTitle(){
        return title;
    }
    
    public void refresh(DataflowPathInfo source, List<Pair<SanitizePattern, DFATreeNode>> sanitizeNodes, ContextInfo contextInfo){
//        west.setLayout(new FlowLayout());
        
        west.removeAll();
        failedSanitizeSelections.clear();
        dataflowSelections.clear();
        secureSources = null;
        
        
        title = source.toString();
        
        DFATreeNode node = source.getSource();
        JLabel secureSourcesLabel = new JLabel("Secure sources");
        west.add(leftJustify(secureSourcesLabel));
        if(source.getSource().getSourcePattern().isSourceSufficient(contextInfo)){            
            List<SourcePattern> insecurePattern = framework.getPatternStorage().getInsecureSources(source.getSource().getSourcePattern(), node, contextInfo);            
            RefactorPanel refactorPanel = new RefactorPanel(node, framework.getDSL(), insecurePattern, dimension, source.getSource().getSourcePattern().getName());

            west.add(leftJustify(refactorPanel));
            
            secureSources = refactorPanel;
        }
        
        west.add(leftJustify(new JLabel("Dataflow patterns")));
        while(node != null){
            if(!node.getPossibleDataflowReplacements().isEmpty()){
                JLabel replacementName = new JLabel(node.getLabel());
                DataFlowRefactorPanel dataFlowRefactorPanel = new DataFlowRefactorPanel(node, framework.getDSL(), dimension);
                west.add(leftJustify(dataFlowRefactorPanel));
                dataflowSelections.add(dataFlowRefactorPanel);
            }
            
            node = node.getParent_();
        }
        
        west.add(leftJustify(new JLabel("Sanitize patterns")));
        
        
        for(Pair<SanitizePattern, DFATreeNode> sanitizeNodePair : sanitizeNodes){
            String sanitizeName = Util.codeLocation(framework.getDb(), sanitizeNodePair.getValue1().getObj()).shortName()+ " " + sanitizeNodePair.getValue0().getName();
//            JLabel sanitizeLabel = new JLabel(sanitizeName);
            
            List<SanitizePattern> possiblePatterns = framework.getPatternStorage().getPossibleFailedSanitizePatterns(sanitizeNodePair.getValue0(), sanitizeNodePair.getValue1(), contextInfo);
            SanitizeRefactorPanel sanitizeRefactorPanel = new SanitizeRefactorPanel(sanitizeNodePair, possiblePatterns, framework.getDSL(), dimension);            
            
            failedSanitizeSelections.add(sanitizeRefactorPanel);   
//            sanitizeRefactorPanel.setPreferredSize(dimension);
//            sanitizeRefactorPanel.setMaximumSize(dimension);
            west.add(leftJustify(sanitizeRefactorPanel));
        }
        
        JButton refactor = new JButton("1: refactoring");
        refactor.addActionListener((arg0) -> {
            refactor();            
        });
        west.add(leftJustify(refactor));
        
        JButton writeFiles = new JButton("2: Write to disk");
        writeFiles.addActionListener((arg0) -> {
            boolean backupFiles = false;
            framework.writeToDisk(backupFiles);
        });
        west.add(leftJustify(writeFiles));
        
        JButton formatCode = new JButton("3: Format source code");
        formatCode.addActionListener((arg0) -> {
            int dialogResult = JOptionPane.showConfirmDialog (null, "Do you want to format the code? It will require new scanning for further refactoring.");
            if(dialogResult == JOptionPane.YES_OPTION){
              // Saving code here
                framework.formatCode();
            }
        });
        west.add(leftJustify(formatCode));
        
        JButton push = new JButton("Push to git");
        push.addActionListener((arg0) -> {
            String msg = JOptionPane.showInputDialog(null);
            System.out.println("Msg: " + msg);
            if(msg != null){
                framework.commitAndPush(msg);
            }
        });
//        west.add(push);
        
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
        Pair<DFATreeNode, SourcePattern> secureSourcePattern = null;
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
    
    public static Component leftJustify( JComponent panel )  {
    Box  b = Box.createHorizontalBox();
    b.add( panel );
    b.add( Box.createHorizontalGlue() );
    // (Note that you could throw a lot more components
    // and struts and glue in here.)
    return b;
}
    
}
