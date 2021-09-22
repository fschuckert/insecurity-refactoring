/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui.astrenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Label;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import ntnuhtwg.insecurityrefactoring.Framework;
import ntnuhtwg.insecurityrefactoring.base.SwingUtil;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.exception.TimeoutException;
import ntnuhtwg.insecurityrefactoring.gui.abego.StringNode;
import ntnuhtwg.insecurityrefactoring.gui.abego.StringNodeExtentProvider;
import ntnuhtwg.insecurityrefactoring.gui.abego.StringTreePanel;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4JConnector;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4jDB;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher.DataflowDSL;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import org.abego.treelayout.TreeLayout;
import org.abego.treelayout.util.DefaultConfiguration;
import org.abego.treelayout.util.DefaultTreeForTreeLayout;
import org.neo4j.driver.Record;
import org.neo4j.driver.Values;
import org.neo4j.driver.types.Node;

/**
 *
 * @author blubbomat
 */
public class ASTRenderer extends JPanel {
//    private  Neo4jDB connector;
    
    private Framework framework;

    private JTextField topNodeId = new JTextField();
    private JComboBox<FileId> files = new JComboBox<FileId>();
    private JButton refreshFiles = new JButton("Search files");
    private JButton printJson = new JButton("Print JSON (copy to clipboard)");
//    private JPanel drawPanel = new JPanel();
//    JScrollPane astTreePane;
//    private GUI gui;
    private ASTView astView = new ASTView();

    public ASTRenderer(Framework framework) {
        this.framework = framework;
        this.setLayout(new BorderLayout());

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.X_AXIS));
        northPanel.add(new JLabel("Id:"));
        northPanel.add(topNodeId);
        northPanel.add(files);
        northPanel.add(refreshFiles);

        refreshFiles.addActionListener((arg0) -> {
            refreshFiles();
        });

        files.addActionListener((arg0) -> {
            FileId fileId = (FileId) files.getSelectedItem();
            if (fileId == null) {
                return;
            }

            renderNode(fileId.getId());
        });

        this.add(astView, BorderLayout.CENTER);
        this.add(northPanel, BorderLayout.NORTH);
        this.add(printJson, BorderLayout.SOUTH);

        printJson.addActionListener((arg0) -> {
            String idText = topNodeId.getText();
//            Integer id = Integer.valueOf(idText);
            try {
                Neo4jDB conne = connectToDB().getDb();
                INode node = conne.findNode(Long.valueOf(idText));
                String json = Util.getASTasJSONRec(new DataflowDSL(conne), node);
                System.out.println("#####################################################################################");
                System.out.println("###########                             START                           #############");
                System.out.println("#####################################################################################");
                System.out.println(json);
                System.out.println("###################################      END       ##################################");

                SwingUtil.copyToClipboard(json);
                
                JOptionPane.showMessageDialog(null, "Copied AST into clipboard.");
            } catch (TimeoutException ex) {
                ex.printStackTrace();

            }
        });

        topNodeId.addActionListener((arg0) -> {
            String idText = topNodeId.getText();
            Long id = Long.valueOf(idText);
            renderNode(id);
        });
    }
    
    private DataflowDSL connectToDB(){
        if(!framework.isDBRunning()){
            framework.connectDB();
        }
        
        Neo4jDB connector = framework.getDb();
        DataflowDSL dsl = new DataflowDSL(connector);
        
        return dsl;
    }

    private void refreshFiles() {
        files.removeAllItems();

        DataflowDSL dsl = connectToDB();

        try {
            List<INode> topLevels = dsl.findAllTopLevel();
            for (INode topLevel : topLevels) {
                files.addItem(new FileId(topLevel.id(), topLevel.getString("name")));
            }

        } catch (TimeoutException ex) {
            Logger.getLogger(ASTRenderer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void renderNode(Long id) {
        Neo4jDB connector = connectToDB().getDb();
//        connector.
        INode node = null;
        try {
            node = connector.findNode((long) id);
        } catch (TimeoutException ex) {
            Logger.getLogger(ASTRenderer.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (node == null) {
            return;
        }

        StringNode rootNode = new StringNode(node);
        DefaultTreeForTreeLayout<StringNode> treeLayout = new DefaultTreeForTreeLayout(rootNode);
        DefaultMutableTreeNode tree = getTree(node, treeLayout, rootNode, connector);

        astView.updateAST(rootNode, tree, treeLayout);

        try {
//            connector.close();
        } catch (Exception ex) {
            Logger.getLogger(ASTRenderer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private DefaultMutableTreeNode getTree(INode parent, DefaultTreeForTreeLayout<StringNode> treeLayout, StringNode parentStringNode, Neo4jDB connector) {
//        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(parent.asMap());       //TODO: check if required!
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode();

        Long id = parent.id();
        List<INode> result = List.of();
        try {
            result = connector.findAll(
                    "MATCH (ast1)-[:PARENT_OF]->(ast2) WHERE id(ast1)=$id return ast2",
                    "id", id
            );
        } catch (TimeoutException ex) {
            Logger.getLogger(ASTRenderer.class.getName()).log(Level.SEVERE, null, ex);
        }
        Collections.reverse(result);

        for (INode node : result) {
            StringNode childStringNode = new StringNode(node);
            treeLayout.addChild(parentStringNode, childStringNode);
            treeNode.add(getTree(node, treeLayout, childStringNode, connector));
        }

        return treeNode;
    }

}
