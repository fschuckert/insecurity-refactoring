/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui;

import ntnuhtwg.insecurityrefactoring.gui.insecurityrefactoring.PIPRenderer;
import ntnuhtwg.insecurityrefactoring.gui.insecurityrefactoring.RefactoringRenderer;
import ntnuhtwg.insecurityrefactoring.gui.astrenderer.ASTRenderer;
import java.awt.Container;
import java.awt.Label;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import ntnuhtwg.insecurityrefactoring.Framework;
import ntnuhtwg.insecurityrefactoring.gui.patterntester.PatternCodeEditor;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4JConnector;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4jDB;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4jEmbedded;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher.DataflowDSL;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;
import ntnuhtwg.insecurityrefactoring.gui.editor.PatternEditor;
import ntnuhtwg.insecurityrefactoring.refactor.InsecurityRefactoring;

/**
 *
 * @author blubbomat
 */
public class GUI {
    JFrame frame;
    
    public void init(Framework framework){
        frame = new JFrame();
        frame.setName("Insecurity Refactoring");
        frame.setTitle("Insecurity Refactoring");
        

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();        
        frame.setVisible(true);
        frame.setSize(1024, 756);
        frame.setBounds(10, 10, 1024, 756);
        JTabbedPane mainTab = new JTabbedPane();        
        getContent().add(mainTab);
        
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e){
                framework.stop();
            }
        });
        
//        Refactoring refactoring = new Refactoring("/home/blubbomat/Development/FindPOI/sca_patterns-master/dataflow/simple_xss_vuln", new DataflowDSL(connector), patternStorage);
//        InsecurityRefactoring refactoring = new InsecurityRefactoring(new DataflowDSL(connector), patternStorage);
//        
        PIPRenderer pIPRenderer = new PIPRenderer(framework, frame);
        
        JTabbedPane insecurityRefactoringTab = new JTabbedPane();
        
        insecurityRefactoringTab.addTab("Pip finder", pIPRenderer);
//        tabbedPane.addTab("Pip Source Code", dataflowSourceCode);
        
        mainTab.addTab("Insecurity Refactoring", insecurityRefactoringTab);
        ASTRenderer astRenderer = new ASTRenderer(framework);        
        mainTab.addTab("AST", astRenderer);   
        mainTab.addTab("Pattern tester", new PatternCodeEditor(framework.getPatternStorage(), null));
        mainTab.addTab("Pattern editor", new PatternEditor());
        
    }
    
    public Container getContent(){
        return frame.getContentPane();
    }
}
