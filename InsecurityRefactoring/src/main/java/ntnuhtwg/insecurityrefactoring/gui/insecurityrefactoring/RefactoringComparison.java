/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui.insecurityrefactoring;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import ntnuhtwg.insecurityrefactoring.base.GlobalSettings;
import ntnuhtwg.insecurityrefactoring.base.SourceLocation;
import ntnuhtwg.insecurityrefactoring.base.Util;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 *
 * @author blubbomat
 */
public class RefactoringComparison extends JPanel {
    
    private final ScrollPos pos = new ScrollPos();

    public RefactoringComparison(SourceLocation location, String refactoredCode, Set<Integer> modifiedLines) {
        this.setLayout(new GridLayout(1, 2));

        String originalCode = location.codeSnippet(0, -1);

        RSyntaxTextArea original = new RSyntaxTextArea(originalCode, 20, 60);
        original.setEditable(false);
        original.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PHP);
        RTextScrollPane originalPane = new RTextScrollPane(original);

        RSyntaxTextArea refactored = new RSyntaxTextArea(refactoredCode, 20, 60);
        refactored.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PHP);
        RTextScrollPane refactoredPane = new RTextScrollPane(refactored);
        pos.setPos(0);
        boolean scrolled = false;
        for (Integer modifiedLine : modifiedLines) {
            try {
                if (scrolled != true) {
                    pos.setPos(modifiedLine);
                    scrolled = true;
                }
                refactored.addLineHighlight(modifiedLine - 1, Color.GREEN);
            } catch (BadLocationException ex) {
                Logger.getLogger(RefactoringComparison.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    int y = original.yForLine(pos.getPos());
                    originalPane.getVerticalScrollBar().setValue(y);
                    System.out.println("Set to: " + y);
                } catch (BadLocationException ex) {
                    Logger.getLogger(RefactoringComparison.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                try {
                    int y = refactored.yForLine(pos.getPos());
                    refactoredPane.getVerticalScrollBar().setValue(y);
                } catch (BadLocationException ex) {
                    Logger.getLogger(RefactoringComparison.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        

        this.add(originalPane);
        this.add(refactoredPane);

    }
    
    
    private class ScrollPos{
        private int pos = 0;

        public int getPos() {
            return pos;
        }

        public void setPos(int pos) {
            this.pos = pos;
        }
        
        
    }

}
