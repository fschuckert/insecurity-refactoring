/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui;

import ntnuhtwg.insecurityrefactoring.gui.astrenderer.ASTRenderer;
import java.awt.Container;
import java.awt.Label;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import ntnuhtwg.insecurityrefactoring.Framework;
import ntnuhtwg.insecurityrefactoring.gui.patterntester.PatternTester;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4JConnector;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4jDB;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4jEmbedded;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher.DataflowDSL;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;
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
        
//        Refactoring refactoring = new Refactoring("/home/blubbomat/Development/FindPOI/sca_patterns-master/dataflow/simple_xss_vuln", new DataflowDSL(connector), patternStorage);
//        InsecurityRefactoring refactoring = new InsecurityRefactoring(new DataflowDSL(connector), patternStorage);
//        
        RefactoringRenderer refactoringRender = new RefactoringRenderer(framework);
        DataflowSourceCode dataflowSourceCode = new DataflowSourceCode();
        PIPRenderer pIPRenderer = new PIPRenderer(framework, refactoringRender, frame, dataflowSourceCode);
        
        JTabbedPane insecurityRefactoringTab = new JTabbedPane();
        
        insecurityRefactoringTab.addTab("Pip finder", pIPRenderer);
        insecurityRefactoringTab.addTab("Refactoring", refactoringRender);
//        tabbedPane.addTab("Pip Source Code", dataflowSourceCode);
        
        mainTab.addTab("Insecurity Refactoring", insecurityRefactoringTab);
        ASTRenderer astRenderer = new ASTRenderer(framework.getDb());        
        mainTab.addTab("AST", astRenderer);   
        mainTab.addTab("Pattern tester", new PatternTester(framework.getPatternStorage()));
        
    }
    
    public Container getContent(){
        return frame.getContentPane();
    }
}
