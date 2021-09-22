/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui.patterntester;

import java.awt.GridLayout;
import java.awt.event.KeyListener;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import ntnuhtwg.insecurityrefactoring.base.GlobalSettings;
import ntnuhtwg.insecurityrefactoring.base.SourceLocation;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.tree.TreeNode;
import ntnuhtwg.insecurityrefactoring.gui.astrenderer.ASTView;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 *
 * @author blubbomat
 */
public class PatternCodeViewer extends JPanel{
    
    RSyntaxTextArea pattern = new RSyntaxTextArea("", 20, 60);
    RSyntaxTextArea sourceCode = new RSyntaxTextArea("", 20, 60);
    JTabbedPane viewer = new JTabbedPane(); 
    ASTView aSTView = new ASTView();
    
    
    public PatternCodeViewer() {
        this.setLayout(new GridLayout(1, 2));
        
        pattern.setEditable(true);
        pattern.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PHP);
        RTextScrollPane originalPane = new RTextScrollPane(pattern);
        
        

        sourceCode.setEditable(false);
        sourceCode.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PHP);
        RTextScrollPane codePane = new RTextScrollPane(sourceCode);
        
        this.add(originalPane);
        this.add(viewer);   
        
        viewer.addTab("Code", codePane);
        viewer.addTab("AST", aSTView);
    }
    
    public String getPattern(){
        return pattern.getText();
    }
    
    public void setSourceCode(String sourceCode){
        this.sourceCode.setText(sourceCode);
        this.sourceCode.setCaretPosition(0);
    }
    
    public void updateAST(TreeNode<INode> ast){
        this.aSTView.updateAST(ast);
    }

    void setCode(String code) {
        this.pattern.setText(code);
    }
    
    public void addPatternKeyListeners(KeyListener l){
        pattern.addKeyListener(l);
    }
}
