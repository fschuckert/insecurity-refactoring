/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui.patterntester;

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import ntnuhtwg.insecurityrefactoring.base.SwingUtil;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;

/**
 *
 * @author blubbomat
 */
public class PatternTesterPanel extends JPanel{
    
    PatternCodeEditor codeEditor;
    JButton copyToClipboard = new JButton("Copy to clipboard");

    public PatternTesterPanel(PatternStorage patternStorage, String editField) {
        this.setLayout(new BorderLayout());
        
        codeEditor = new PatternCodeEditor(patternStorage, editField);
        
        this.add(codeEditor, BorderLayout.CENTER);
        this.add(copyToClipboard, BorderLayout.SOUTH);
        
        copyToClipboard.addActionListener((arg0) -> {
            List<String> codeLines = codeEditor.getCodeLines();
            String codeLinesStr = "\"" + Util.joinStr(codeLines, "\",\n\"") + "\"";
            SwingUtil.copyToClipboard(codeLinesStr);
        });
    }
    
}
