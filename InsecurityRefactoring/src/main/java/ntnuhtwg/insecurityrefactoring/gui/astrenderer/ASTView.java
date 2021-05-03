/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui.astrenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import ntnuhtwg.insecurityrefactoring.base.ast.ASTFactory;
import ntnuhtwg.insecurityrefactoring.base.ast.BaseNode;
import ntnuhtwg.insecurityrefactoring.base.tree.TreeNode;
import ntnuhtwg.insecurityrefactoring.gui.abego.StringNode;
import ntnuhtwg.insecurityrefactoring.gui.abego.StringNodeExtentProvider;
import ntnuhtwg.insecurityrefactoring.gui.abego.StringTreePanel;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import org.abego.treelayout.TreeLayout;
import org.abego.treelayout.util.DefaultConfiguration;
import org.abego.treelayout.util.DefaultTreeForTreeLayout;

/**
 *
 * @author blubbomat
 */
public class ASTView extends JPanel{
    private JScrollPane scrollPane;
    private JPanel drawPanel = new JPanel();

    public ASTView() {
        this.setLayout(new GridLayout(1, 1));        
        scrollPane = new JScrollPane(drawPanel);
        this.add(scrollPane);
        drawPanel.add(new JLabel("AST"));
    }
    
//    public void updateASTs(List<TreeNode<INode>> abstractASTs){
//        TreeNode<INode> topNode = ASTFactory.createStatementList();
//        
//    }
    
    public void updateAST(TreeNode<INode> abstractAST){
        StringNode rootNode = new StringNode(abstractAST.getObj());
        DefaultTreeForTreeLayout<StringNode> treeLayout = new DefaultTreeForTreeLayout(rootNode);    
        
        DefaultMutableTreeNode tree = getTree(abstractAST, treeLayout, rootNode);
        updateAST(rootNode, tree, treeLayout);
    }
    
    
    public void updateAST(StringNode topNode, DefaultMutableTreeNode ast, DefaultTreeForTreeLayout<StringNode> treeLayout){
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(paintTree(treeLayout, panel), BorderLayout.CENTER);
        
        JTree m_simpleTree = new JTree(ast);

        DefaultTreeCellRenderer cellRenderer = (DefaultTreeCellRenderer) m_simpleTree.getCellRenderer();

        cellRenderer.setBackgroundNonSelectionColor(new Color(155, 155, 221));
        cellRenderer.setBackgroundSelectionColor(new Color(0, 0, 128));
        cellRenderer.setBorderSelectionColor(Color.black);
        cellRenderer.setTextSelectionColor(Color.white);
        cellRenderer.setTextNonSelectionColor(Color.blue);
        
        drawPanel.removeAll();
//        astTreePane.add(m_simpleTree);
        this.validate();
        drawPanel.add(panel);
        drawPanel.validate();
        drawPanel.repaint();
    }
    
    private JScrollPane paintTree(DefaultTreeForTreeLayout tree, JPanel panel){
        StringNodeExtentProvider stringNodeExtentProvider = new StringNodeExtentProvider();
        stringNodeExtentProvider.setFontMetrics(panel.getFontMetrics(panel.getFont()));
        DefaultConfiguration<StringNode> configuration = new DefaultConfiguration<StringNode>(10, 10);

        
        TreeLayout<StringNode> treeLayout = new TreeLayout(tree, stringNodeExtentProvider, configuration);
        StringTreePanel stringTreePanel = new StringTreePanel(treeLayout);
        stringTreePanel.setBackground(Color.white);
        stringTreePanel.setBackground(Color.BLUE);
        
        JScrollPane activeScrollTreePanel = new JScrollPane(stringTreePanel);
        activeScrollTreePanel.setBackground(Color.CYAN);

        return activeScrollTreePanel;
    }
    
    private DefaultMutableTreeNode getTree(TreeNode<INode> abstractTreeNode, DefaultTreeForTreeLayout<StringNode> treeLayout, StringNode parent){
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode();
//        StringNode parentNode = new StringNode(abstractTreeNode.getObj());
        
        for(TreeNode<INode> child : abstractTreeNode.getChildren()){
            StringNode childNode = new StringNode(child.getObj());
            treeLayout.addChild(parent, childNode);
            treeNode.add(getTree(child, treeLayout, childNode));
        }
        
        return treeNode;
    }
}
