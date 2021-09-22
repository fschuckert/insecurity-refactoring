/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui.insecurityrefactoring;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import ntnuhtwg.insecurityrefactoring.Framework;
import ntnuhtwg.insecurityrefactoring.base.ASTNodeTypes;
import ntnuhtwg.insecurityrefactoring.base.SourceLocation;
import ntnuhtwg.insecurityrefactoring.base.SwingUtil;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher.DataflowDSL;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.exception.TimeoutException;
import ntnuhtwg.insecurityrefactoring.base.info.ACIDTree;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
import ntnuhtwg.insecurityrefactoring.gui.DataflowSourceCode;
import ntnuhtwg.insecurityrefactoring.gui.abego.StringNode;
import ntnuhtwg.insecurityrefactoring.gui.abego.StringNodeExtentProvider;
import ntnuhtwg.insecurityrefactoring.gui.abego.StringTreePanel;
import org.abego.treelayout.Configuration;
import org.abego.treelayout.TreeLayout;
import org.abego.treelayout.util.DefaultConfiguration;
import org.abego.treelayout.util.DefaultTreeForTreeLayout;

/**
 *
 * @author blubbomat
 */
public class ACIDViewer extends JPanel {
    
    private DataflowSourceCode dataflowSourceCode;

    private Framework framework;
    private ACIDTree pipInformation;

    private JScrollPane astTreePane;
    private JPanel drawPanel = new JPanel();

    // south
    private JComboBox<Configuration.Location> treeLoc = new JComboBox<>(Configuration.Location.values());
    private JCheckBox renderCode = new JCheckBox("Show source code");
    private JCheckBox showProperties = new JCheckBox("Show properties");
    private JCheckBox flowtype = new JCheckBox("Show Flowtype", false);

    public ACIDViewer(Framework framework) {
        this.framework = framework;
        this.setLayout(new BorderLayout());

        astTreePane = new JScrollPane(drawPanel);

        this.add(astTreePane, BorderLayout.CENTER);

        JPanel south = SwingUtil.layoutBoxX(
                treeLoc,
                renderCode,
                showProperties,
                flowtype
        );
        this.add(south, BorderLayout.SOUTH);

        treeLoc.addActionListener((arg0) -> {
            refreshTree(pipInformation);
        });

        renderCode.addActionListener((arg0) -> {
            refreshTree(pipInformation);
        });

        flowtype.addActionListener((arg0) -> {
            refreshTree(pipInformation);
        });

        showProperties.addActionListener((arg0) -> {
            refreshTree(pipInformation);
        });
    }

    public void refreshTree(ACIDTree dfaRootNode) {
        this.pipInformation = dfaRootNode;
        if (dfaRootNode == null) {
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
        drawPanel.add(panel);
        drawPanel.validate();
        astTreePane.validate();
        drawPanel.repaint();

    }

    private DefaultMutableTreeNode getTree(DFATreeNode parent, DefaultTreeForTreeLayout<StringNode> treeLayout, StringNode parentStringNode) {
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(parent);

        for (DFATreeNode entry : parent.getChildren_()) {
            INode child = entry.getObj();
            SourceLocation sourceLocation = Util.codeLocation(framework.getDb(), child);
            String optional = "";
            String sourceCode = renderCode.isSelected() ? Util.codeSnippet(framework.getDb(), child) : "";
            String sourceLocationStr = sourceLocation != null ? sourceLocation.toString() : "Location null";
            if (Util.isType(child, ASTNodeTypes.VARIABLE)) {
                DataflowDSL dsl = new DataflowDSL(framework.getDb());
                try {
                    optional = dsl.getVariableName(child);
                } catch (TimeoutException ex) {
                    Logger.getLogger(PIPRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (Util.isType(child, ASTNodeTypes.STRING)) {
                optional = "\"" + child.getString("code") + "\"";
            } else if (Util.isAnyCall(child)) {
                try {
                    optional = new DataflowDSL(framework.getDb()).getCallName(child, false);
                } catch (TimeoutException ex) {
                    Logger.getLogger(PIPRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (entry.isConditionNode()) {
                optional += " Req. cond.: " + entry.getConditionRequiresTrue() + "";
            }

            StringNode childStringNode = new StringNode(entry, entry.getLabel(), sourceLocationStr, sourceCode, showProperties.isSelected(), optional, flowtype.isSelected());
            treeLayout.addChild(parentStringNode, childStringNode);
            treeNode.add(getTree(entry, treeLayout, childStringNode));
        }

        return treeNode;
    }

    private JScrollPane paintTree(DefaultTreeForTreeLayout tree, JPanel panel) {
        StringNodeExtentProvider stringNodeExtentProvider = new StringNodeExtentProvider();
        stringNodeExtentProvider.setFontMetrics(panel.getFontMetrics(panel.getFont()));
        DefaultConfiguration<StringNode> configuration = new DefaultConfiguration<StringNode>(10, 10, (Configuration.Location) treeLoc.getSelectedItem());

        TreeLayout<StringNode> treeLayout = new TreeLayout(tree, stringNodeExtentProvider, configuration);
        StringTreePanel stringTreePanel = new StringTreePanel(treeLayout);
        stringTreePanel.setBackground(Color.white);
        stringTreePanel.setBackground(Color.BLUE);

        JScrollPane activeScrollTreePanel = new JScrollPane(stringTreePanel);
        activeScrollTreePanel.setBackground(Color.CYAN);

        return activeScrollTreePanel;
    }
}
