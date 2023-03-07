/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui.insecurityrefactoring;

import ntnuhtwg.insecurityrefactoring.gui.temppattern.TempPatternFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import ntnuhtwg.insecurityrefactoring.Framework;
import ntnuhtwg.insecurityrefactoring.base.ASTNodeTypes;
import ntnuhtwg.insecurityrefactoring.base.GlobalSettings;
import ntnuhtwg.insecurityrefactoring.base.SourceLocation;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.exception.TimeoutException;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher.DataflowDSL;
import ntnuhtwg.insecurityrefactoring.refactor.acid.ACIDTreeCreator;
import ntnuhtwg.insecurityrefactoring.gui.abego.StringNode;
import ntnuhtwg.insecurityrefactoring.gui.abego.StringNodeExtentProvider;
import ntnuhtwg.insecurityrefactoring.gui.abego.StringTreePanel;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4JConnector;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4jDB;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.info.ACIDTree;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SanitizePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SinkPattern;
import ntnuhtwg.insecurityrefactoring.base.info.DataflowPathInfo;
import ntnuhtwg.insecurityrefactoring.gui.DataflowSourceCode;
import ntnuhtwg.insecurityrefactoring.refactor.analyze.ACIDAnalyzer;
import ntnuhtwg.insecurityrefactoring.refactor.temppattern.ScanProgress;
import ntnuhtwg.insecurityrefactoring.refactor.temppattern.TempPattern;
import org.abego.treelayout.Configuration;
import org.abego.treelayout.TreeLayout;
import org.abego.treelayout.util.DefaultConfiguration;
import org.abego.treelayout.util.DefaultTreeForTreeLayout;
import org.javatuples.Pair;
import org.neo4j.driver.Record;
import org.neo4j.driver.Values;
import org.neo4j.driver.types.Node;

/**
 *
 * @author blubbomat
 */
public class PIPRenderer extends JPanel{
    
    private ACIDViewer acidViewer;
    private final String scanEverything = "Scan everything...";
    
    private JCheckBox skipPreScan = new JCheckBox("Skip pre scan", false);
    private JButton findPip = new JButton("Find PIPs");
    
//    private JTextField prePath = new JTextField(GlobalSettings.prePath);
//    private JTextField scanPath = new JTextField("/home/blubbomat/Development/simple");
    private JTextField scanPath = new JTextField("/path/that/will/be/scanned/");
    private JTextField specificPath = new JTextField("");
//    private JFileChooser scanPath = new JFileChooser("/home/blubbomat/Development/simple");
    private JButton chooseFile = new JButton("Choose File");
//    private JFileChooser chooseFile = new JFileChooser()
//    private JComboBox<DFATreeNode> results = new JComboBox<>();
    Framework framework;
    
    private DefaultListModel<ACIDTree> listModel = new DefaultListModel();
    private DefaultListModel<DataflowPathInfo> sourceListModel = new DefaultListModel();
    private JList<ACIDTree> results = new JList<>(listModel);
    private JList<DataflowPathInfo> sourceNodes = new JList<>(sourceListModel);
    
    
    
    
    
    private JCheckBox showPath = new JCheckBox("Show path", false);
    private JCheckBox debugAddAllResults = new JCheckBox("Debug: Add all results", false);
    private JButton insecurityRefactor = new JButton("Start Insecurity Refactoring");
    private JLabel preContext = new JLabel("pre:");
    private JLabel postContext = new JLabel("pos:");
    private RefactoringRenderer actualRefactoringPanel;
    
    private TempPatternFrame tempPatternFrame;
    private JButton showTempPatterns = new JButton("Show temp patterns");
    private JButton rescan = new JButton("Rescan with temp pattern");
    private JButton hideShowSource = new JButton("Hide/show source code");
    private JComboBox<SinkPattern> viewSpecificPattern = new JComboBox<SinkPattern>();
    private SinkPattern selectedPattern = null;
    private JComboBox<String> scanSpecific = new JComboBox<>();
    private JCheckBox requiresSan = new JCheckBox("Requires sanitize");
    private JCheckBox checkControlFunctions = new JCheckBox("Check control functions");
    private DataflowSourceCode dataflowSourceCode;
    private JProgressBar progressBar = new JProgressBar(0, 100);
    

    public PIPRenderer(Framework framework, JFrame frame){
        this.framework = framework;
        
        this.acidViewer = new ACIDViewer(framework);
        results.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sourceNodes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.tempPatternFrame = new TempPatternFrame(frame);
        tempPatternFrame.setVisible(false);
        this.dataflowSourceCode = new DataflowSourceCode();
        progressBar.setStringPainted(true);
        
        specificPath.setMaximumSize(new Dimension(1000, 20));
        
        this.setLayout(new BorderLayout());
        
        JPanel west = new JPanel();
        viewSpecificPattern.setMaximumSize(new Dimension(200, 20));
        west.setLayout(new BoxLayout(west, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(results);
        west.add(new JLabel("Specific sink location (path:lineno):"));
        west.add(specificPath);
        west.add(viewSpecificPattern);
        west.add(requiresSan);
        west.add(checkControlFunctions);
        west.add(debugAddAllResults);
        west.add(scrollPane);
        west.add(sourceNodes);
        west.add(preContext);
        west.add(postContext);
        west.add(showTempPatterns);
        west.add(rescan);
       
        
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.X_AXIS));
        northPanel.add(skipPreScan);
        northPanel.add(scanPath);
        northPanel.add(scanSpecific);
        northPanel.add(findPip);
        northPanel.add(hideShowSource);
        
        this.add(northPanel, BorderLayout.NORTH);
        this.add(west, BorderLayout.WEST);
        
        this.add(dataflowSourceCode, BorderLayout.EAST);
        
        JPanel south = new JPanel();
        south.setBackground(Color.red);
        south.setLayout(new GridLayout(1, 4));
        this.add(south, BorderLayout.SOUTH);
        
        this.add(acidViewer, BorderLayout.CENTER);
        
        insecurityRefactor.setVisible(false);
        south.add(insecurityRefactor);
        south.add(progressBar);
//        south.add(prePath);
        
        hideShowSource.addActionListener((arg0) -> {
            dataflowSourceCode.setVisible(!dataflowSourceCode.isVisible());
        });
        
        
        sourceNodes.addListSelectionListener((arg0) -> {
            ACIDTree acidTree = (ACIDTree)results.getSelectedValue();
            DataflowPathInfo dataflowPath = (DataflowPathInfo)sourceNodes.getSelectedValue();
            if(acidTree != null && dataflowPath != null){
                framework.analyze(acidTree, dataflowPath);
                List<Pair<SanitizePattern, DFATreeNode>> sanitizeNodes = dataflowPath.getSanitizeNodes();
                preContext.setText("pre: " + dataflowPath.getContextInfo().getPre());
                postContext.setText("pos: " + dataflowPath.getContextInfo().getPost());
                actualRefactoringPanel = new RefactoringRenderer(framework);
                actualRefactoringPanel.refresh(dataflowPath, sanitizeNodes, dataflowPath.getContextInfo());
                refreshSourceCode(dataflowPath);
                acidViewer.refreshTree(acidTree);
                insecurityRefactor.setVisible(true);
            }
            else{
                insecurityRefactor.setVisible(false);
            }
        });
        
        findPip.addActionListener((arg0) -> {
            this.findPip.setEnabled(false);
            String specific = (String)scanSpecific.getSelectedItem();
            SourceLocation specificLoc = specificPath.getText().isBlank() ? null : new SourceLocation(specificPath.getText());
            ScanTask scanTask = new ScanTask(framework, scanPath.getText(), scanEverything.equals(specific) ? null : specific, skipPreScan.isSelected(), this, this.checkControlFunctions.isSelected(), specificLoc);
            progressBar.setValue(0);
            scanTask.addPropertyChangeListener((PropertyChangeEvent arg1) -> {
                PropertyChangeEvent prop = arg1;
                if(prop.getNewValue() instanceof Integer){
                    progressBar.setValue((int)prop.getNewValue());
                }
//                scanProgress.set
//                System.out.println("Property changed: " + arg1);
            });
            scanTask.execute();
            
        });
        
        rescan.addActionListener((arg0) -> {
            List<TempPattern> tempPattern = tempPatternFrame.getTempPatterns();
            ScanProgress progress = new ScanProgress();
            framework.rescan(tempPattern, progress, checkControlFunctions.isSelected());
            refreshPips();
        });
        
        requiresSan.addActionListener((arg0) -> {
            refreshPips();
        });
        
       
        
        results.addListSelectionListener((arg0) -> {
//            renderTree();
            refreshSourceList();
            if(sourceNodes.isSelectionEmpty() && sourceNodes.getSelectedIndex() != 0){
                sourceNodes.setSelectedIndex(0);
            }
        });
        
        
        
      
        
        insecurityRefactor.addActionListener((arg0) -> {
            // TODO:
            
        });
        
        showTempPatterns.addActionListener((arg0) -> {
            tempPatternFrame.setVisible(!tempPatternFrame.isVisible());
        });
        
        viewSpecificPattern.addActionListener((arg0) -> {
            SinkPattern sinkPattern = (SinkPattern)viewSpecificPattern.getSelectedItem();
            if(sinkPattern != this.selectedPattern){   
                if(sinkPattern instanceof RealPips){
                    this.selectedPattern = null;
                }
                else{
                    this.selectedPattern = sinkPattern;
                }
                refreshPips();
            }
        });
        
        refreshSpecificPatterns();
        refreshPips();
    }
    
    public RefactoringRenderer getActualRefactoringPanel(){
        return this.actualRefactoringPanel;
    }
    
    private void refreshPips(){
        boolean reqSan = requiresSan.isSelected();
        if(selectedPattern == null){
            List<ACIDTree> resultTrees = framework.getPipInformation();
            listModel.removeAllElements();
            for(ACIDTree result : resultTrees){
                result.getSink().setSourceLocation(Util.codeLocation(framework.getDb(), result.getSink().getObj()));
                listModel.addElement(result);
            }
            refreshSourceList();
        }
        else{
            List<ACIDTree> resultTrees = framework.getPipInformation();
            listModel.removeAllElements();
            for(ACIDTree result : resultTrees){
                //TODO: check if it is required anymore
                result.getSink().setSourceLocation(Util.codeLocation(framework.getDb(), result.getSink().getObj()));
                listModel.addElement(result);
            }
            refreshSourceList();
        }
    }
    
    public void addStartInsecurityRefactoringActionListener(ActionListener l){
        this.insecurityRefactor.addActionListener(l);
    }
    
    
    private void refreshSinks(){
        viewSpecificPattern.removeAllItems();
        viewSpecificPattern.addItem(new RealPips());
        for(Entry<SinkPattern, Integer> entry : framework.getSinkCount()){
            if(entry.getValue() > 0){
                viewSpecificPattern.addItem(entry.getKey());
            }
        }
        
    }
    
    private void refreshSourceList(){
        sourceListModel.removeAllElements();
        ACIDTree dfaRootNode = (ACIDTree)results.getSelectedValue();
        if(dfaRootNode != null){
            List<DataflowPathInfo> sourceNodes = framework.getSourceNodes(dfaRootNode.getSink());         
            System.out.println("Got sources: " + sourceNodes);
            sourceListModel.addAll(sourceNodes);
        }
    }

    
    
    
  
    

    private void refreshSpecificPatterns() {
        this.scanSpecific.removeAllItems();
        this.scanSpecific.addItem(scanEverything);
        
        for(SinkPattern sinkPattern : framework.getPatternStorage().getSinks()){
            scanSpecific.addItem(sinkPattern.getName());
        }
    }

    private void refreshSourceCode(DataflowPathInfo dataflowPathInfo) {
        DFATreeNode dfaSourceNode = dataflowPathInfo.getSource();
        List<SourceLocation> locations = getSourceCodeSnippetsRec(dfaSourceNode);
        Collections.reverse(locations);
        String code = "";
//        StringJoiner stringJoiner = new StringJoiner("...\n");
        for(SourceLocation loc : locations){
            code += loc.codeSnippet() + "\n...\n";
        }
        
        System.out.println("CODE" + code);
        dataflowSourceCode.refreshSourceCode(code);
    }
    
    private LinkedList<SourceLocation> getSourceCodeSnippetsRec(DFATreeNode dfanode){
        SourceLocation loc = Util.codeLocation(framework.getDb(), dfanode.getObj());
        
        if(dfanode.getParent() == null){            
            LinkedList<SourceLocation> locList = new LinkedList<>();
            locList.add(loc);
            return locList;
        }
        
        LinkedList<SourceLocation> listBefore = getSourceCodeSnippetsRec(dfanode.getParent_());
        
        if(loc != null && !listBefore.isEmpty() && !listBefore.getLast().equals(loc)){
            listBefore.add(loc);
        }
        return listBefore;        
    }

    void finishedScan() {
        tempPatternFrame.refresh(framework.getMissingCalls());
        refreshPips();            
        refreshSinks();
        this.findPip.setEnabled(true);
    }
    
    
    
    
    private class RealPips extends SinkPattern{

        @Override
        public String toString() {
            return "show pips...";
        }
        
    }
}
