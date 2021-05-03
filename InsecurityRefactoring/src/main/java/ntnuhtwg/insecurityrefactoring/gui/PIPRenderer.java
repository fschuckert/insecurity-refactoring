/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui;

import ntnuhtwg.insecurityrefactoring.gui.temppattern.TempPatternFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
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
import ntnuhtwg.insecurityrefactoring.base.info.PipInformation;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SanitizePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SinkPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;
import ntnuhtwg.insecurityrefactoring.base.context.Context;
import ntnuhtwg.insecurityrefactoring.refactor.analyze.ACIDAnalyzer;
import ntnuhtwg.insecurityrefactoring.refactor.base.ScanProgress;
import ntnuhtwg.insecurityrefactoring.refactor.base.TempPattern;
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
    
    
    private final String scanEverything = "Scan everything...";
    
    private JCheckBox skipPreScan = new JCheckBox("Skip pre scan", true);
    private JButton findPip = new JButton("Find PIPs");
    private JPanel drawPanel = new JPanel();
//    private JTextField prePath = new JTextField(GlobalSettings.prePath);
    private JTextField scanPath = new JTextField("/home/blubbomat/Development/simple");
    private JTextField specificPath = new JTextField("");
//    private JFileChooser scanPath = new JFileChooser("/home/blubbomat/Development/simple");
    private JButton chooseFile = new JButton("Choose File");
//    private JFileChooser chooseFile = new JFileChooser()
//    private JComboBox<DFATreeNode> results = new JComboBox<>();
    Framework framework;
    
    private DefaultListModel<PipInformation> listModel = new DefaultListModel();
    private DefaultListModel<DFATreeNode> sourceListModel = new DefaultListModel();
    private JList<PipInformation> results = new JList<>(listModel);
    private JList<DFATreeNode> sourceNodes = new JList<>(sourceListModel);
    
    private JScrollPane astTreePane;
    
    private JCheckBox renderCode = new JCheckBox("Show source code");
    private JCheckBox showProperties = new JCheckBox("Show properties");
    private JCheckBox flowtype = new JCheckBox("Show Flowtype", false);
    private JCheckBox requirement = new JCheckBox("Show Requirement", false);
    private JCheckBox showPath = new JCheckBox("Show path", false);
    private JCheckBox debugAddAllResults = new JCheckBox("Debug: Add all results", false);
    private JCheckBox onlyRenderContext = new JCheckBox("Show only context nodes", false);
    private JLabel preContext = new JLabel("pre:");
    private JLabel postContext = new JLabel("pos:");
    private RefactoringRenderer refactoring;
    
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
//    private GUI gui;
    private JComboBox<Configuration.Location> treeLoc = new JComboBox<>(Configuration.Location.values());

    public PIPRenderer(Framework framework, RefactoringRenderer refactoringRenderer, JFrame frame,  DataflowSourceCode dataflowSourceCode){
        this.framework = framework;
        results.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sourceNodes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        refactoring = refactoringRenderer;
        this.tempPatternFrame = new TempPatternFrame(frame);
        tempPatternFrame.setVisible(false);
        this.dataflowSourceCode = dataflowSourceCode;
        progressBar.setStringPainted(true);
        
        specificPath.setMaximumSize(new Dimension(1000, 20));
        
        this.setLayout(new BorderLayout());
        
        astTreePane = new JScrollPane(drawPanel);
        
        this.add(astTreePane, BorderLayout.CENTER);
        
        drawPanel.add(new JLabel("Results..."));
        
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
        west.add(onlyRenderContext);
        
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
        
        south.add(this.treeLoc);
        south.add(renderCode);
        south.add(showProperties);
        south.add(flowtype);
        south.add(requirement);
        south.add(progressBar);
//        south.add(prePath);
        
        hideShowSource.addActionListener((arg0) -> {
            dataflowSourceCode.setVisible(!dataflowSourceCode.isVisible());
        });
        
        
        sourceNodes.addListSelectionListener((arg0) -> {
            PipInformation dfaRootNode = (PipInformation)results.getSelectedValue();
            DFATreeNode dfaSourceNode = (DFATreeNode)sourceNodes.getSelectedValue();
            if(dfaRootNode != null && dfaSourceNode != null){
                List<Pair<SanitizePattern, DFATreeNode>> sanitizeNodes = framework.analyze(dfaRootNode, dfaSourceNode);
                preContext.setText("pre: " + dfaRootNode.getContextInfo().getPre());
                postContext.setText("pos: " + dfaRootNode.getContextInfo().getPost());
                refactoring.refresh(dfaSourceNode, sanitizeNodes, dfaRootNode.getContextInfo());
                refreshSourceCode(dfaSourceNode);
                renderTree();
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
        
        treeLoc.addActionListener((arg0) -> {
            renderTree();
        });
        
        results.addListSelectionListener((arg0) -> {
            renderTree();
            refreshSourceList();
            if(sourceNodes.isSelectionEmpty() && sourceNodes.getSelectedIndex() != 0){
                sourceNodes.setSelectedIndex(0);
            }
        });
        
        renderCode.addActionListener((arg0) -> {
            renderTree();
        });
        
        flowtype.addActionListener((arg0) -> {
            renderTree();
        });
        
        requirement.addActionListener((arg0) -> {
            renderTree();
        });
        
        showProperties.addActionListener((arg0) -> {
            renderTree();
        });
        
        showPath.addActionListener((arg0) -> {
            renderTree();
        });
        
        onlyRenderContext.addActionListener((arg0) -> {
            renderTree();
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
    
    private void refreshPips(){
        boolean reqSan = requiresSan.isSelected();
        if(selectedPattern == null){
            List<PipInformation> resultTrees = framework.getPipInformation();
            listModel.removeAllElements();
            for(PipInformation result : resultTrees){
                result.getSink().setSourceLocation(Util.codeLocation(framework.getDb(), result.getSink().getObj()));
                listModel.addElement(result);
            }
            refreshSourceList();
        }
        else{
            List<PipInformation> resultTrees = framework.getPipInformation();
            listModel.removeAllElements();
            for(PipInformation result : resultTrees){
                //TODO: check if it is required anymore
                result.getSink().setSourceLocation(Util.codeLocation(framework.getDb(), result.getSink().getObj()));
                listModel.addElement(result);
            }
            refreshSourceList();
        }
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
        PipInformation dfaRootNode = (PipInformation)results.getSelectedValue();
        if(dfaRootNode != null){
            List<DFATreeNode> sourceNodes = framework.getSourceNodes(dfaRootNode.getSink());         
            System.out.println("Got sources: " + sourceNodes);
            sourceListModel.addAll(sourceNodes);
        }
    }

    
    private void renderTree(){
        PipInformation dfaRootNode = (PipInformation)results.getSelectedValue();
        if(dfaRootNode == null){
            drawPanel.removeAll();
            return;
        }
        
        StringNode rootNode = new StringNode(dfaRootNode.getSink(), "", "", "", showProperties.isSelected(), "root", flowtype.isSelected());
        DefaultTreeForTreeLayout<StringNode> treeLayout = new DefaultTreeForTreeLayout(rootNode);
        
//        TreeLayout<StringNode> treeStringNode = new TreeLayout<>();
        
        DefaultMutableTreeNode tree = getTree(dfaRootNode.getSink(), treeLayout, rootNode);
        
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(paintTree(treeLayout, panel), BorderLayout.CENTER);
        
        JTree m_simpleTree = new JTree(tree);

        DefaultTreeCellRenderer cellRenderer = (DefaultTreeCellRenderer) m_simpleTree.getCellRenderer();

        cellRenderer.setBackgroundNonSelectionColor(new Color(155, 155, 221));
        cellRenderer.setBackgroundSelectionColor(new Color(0, 0, 128));
        cellRenderer.setBorderSelectionColor(Color.black);
        cellRenderer.setTextSelectionColor(Color.white);
        cellRenderer.setTextNonSelectionColor(Color.blue);
        
        drawPanel.removeAll();
//        astTreePane.add(m_simpleTree);
        drawPanel.add(panel);
        drawPanel.validate();
        astTreePane.validate();
        drawPanel.repaint();
        
    }
    
    private DefaultMutableTreeNode getTree(DFATreeNode parent, DefaultTreeForTreeLayout<StringNode> treeLayout, StringNode parentStringNode){
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(parent);       
     
//        Long id = parent.id();
//        List<Node> result = connector.findAll("MATCH (ast1)-[:PARENT_OF]->(ast2) WHERE id(ast1)=$id return ast2", Values.parameters("id", id));
//        Collections.reverse(result);
        
        for(DFATreeNode entry : parent.getChildren_()){
            INode child = entry.getObj();
//            SourceLocation sourceLocation = connector.codeLocation(child);
            SourceLocation sourceLocation = Util.codeLocation(framework.getDb(), child);
            String optional = "";
//            String sourceCode = renderCode.isSelected() ? connector.codeLocation(child).codeSnippet(prePath.getText()) : "";
            ;
            String sourceCode = renderCode.isSelected() ? Util.codeSnippet(framework.getDb(), child) : "";
            String sourceLocationStr = sourceLocation != null ? sourceLocation.toString() : "Location null";
            if(Util.isType(child, ASTNodeTypes.VARIABLE)){
                DataflowDSL dsl = new DataflowDSL(framework.getDb());
                try {
                    optional = dsl.getVariableName(child);
                } catch (TimeoutException ex) {
                    Logger.getLogger(PIPRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else if(Util.isType(child, ASTNodeTypes.STRING)){
                optional = "\""+ child.getString("code") +"\"";
            }
            else if(Util.isAnyCall(child)){
                try {
                    optional = new DataflowDSL(framework.getDb()).getCallName(child, false);
                } catch (TimeoutException ex) {
                    Logger.getLogger(PIPRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            if(entry.isConditionNode()){
                optional +=  " Req. cond.: " + entry.getConditionRequiresTrue() +"";
            }           
            
            StringNode childStringNode = new StringNode(entry, entry.getLabel(), sourceLocationStr, sourceCode, showProperties.isSelected(), optional, flowtype.isSelected());
            treeLayout.addChild(parentStringNode, childStringNode);
            treeNode.add(getTree(entry, treeLayout, childStringNode));
        }
        
        return treeNode;
    }
    
    private JScrollPane paintTree(DefaultTreeForTreeLayout tree, JPanel panel){
        StringNodeExtentProvider stringNodeExtentProvider = new StringNodeExtentProvider();
        stringNodeExtentProvider.setFontMetrics(panel.getFontMetrics(panel.getFont()));
        DefaultConfiguration<StringNode> configuration = new DefaultConfiguration<StringNode>(10, 10, (Configuration.Location)treeLoc.getSelectedItem());
//        configuration.

        
        TreeLayout<StringNode> treeLayout = new TreeLayout(tree, stringNodeExtentProvider, configuration);
        StringTreePanel stringTreePanel = new StringTreePanel(treeLayout);
        stringTreePanel.setBackground(Color.white);
        stringTreePanel.setBackground(Color.BLUE);
//        stringTreePanel.set
        
        JScrollPane activeScrollTreePanel = new JScrollPane(stringTreePanel);
        activeScrollTreePanel.setBackground(Color.CYAN);

        return activeScrollTreePanel;
    }
    

    private void refreshSpecificPatterns() {
        this.scanSpecific.removeAllItems();
        this.scanSpecific.addItem(scanEverything);
        
        for(SinkPattern sinkPattern : framework.getPatternStorage().getSinks()){
            scanSpecific.addItem(sinkPattern.getName());
        }
    }

    private void refreshSourceCode(DFATreeNode dfaSourceNode) {
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
