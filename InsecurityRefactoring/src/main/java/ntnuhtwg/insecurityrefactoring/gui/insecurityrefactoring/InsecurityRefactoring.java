/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui.insecurityrefactoring;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import ntnuhtwg.insecurityrefactoring.Framework;

/**
 *
 * @author blubbomat
 */
public class InsecurityRefactoring extends JPanel{
    
    JTabbedPane tabPane = new JTabbedPane();
    
    PIPRenderer pipFinder;

    public InsecurityRefactoring(Framework framework, JFrame frame) {
        this.setLayout(new BorderLayout());
        
        this.add(tabPane);
        
        pipFinder = new PIPRenderer(framework, frame);
        
        pipFinder.addStartInsecurityRefactoringActionListener((arg0) -> {
            RefactoringRenderer refactoringRenderer = pipFinder.getActualRefactoringPanel();
            if(refactoringRenderer != null){
                tabPane.add(refactoringRenderer.getTitle(), refactoringRenderer);
            }
        });
        
        tabPane.add("Possible Injection Point finder", pipFinder);
    }
    
}
