/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui;

import java.awt.Dimension;
import javax.swing.JPanel;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 *
 * @author blubbomat
 */
public class DataflowSourceCode extends JPanel{
    
    RSyntaxTextArea refactored;
    RTextScrollPane refactoredPane;

    public DataflowSourceCode() {
        this.setMinimumSize(new Dimension(300, 200));
        refactored = new RSyntaxTextArea("", 20, 60);
        refactored.setMinimumSize(new Dimension(300, 200));
        refactored.setMinimumSize(new Dimension(300, 200));
        refactored.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PHP);
        RTextScrollPane refactoredPane = new RTextScrollPane(refactored);
        
        this.add(refactoredPane);
    }
    
    
    
    
    public void refreshSourceCode(String sourceCode){
        refactored.setText(sourceCode);
    }
}
