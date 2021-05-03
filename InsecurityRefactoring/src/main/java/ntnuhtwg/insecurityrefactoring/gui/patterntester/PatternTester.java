/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui.patterntester;

import java.awt.BorderLayout;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.ast.ASTFactory;
import ntnuhtwg.insecurityrefactoring.base.tree.TreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.DataflowPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;
import ntnuhtwg.insecurityrefactoring.base.print.PrintAST;
import ntnuhtwg.insecurityrefactoring.base.context.Context;

/**
 *
 * @author blubbomat
 */
public class PatternTester extends JPanel{
    
    PatternCodeViewer patternCodeViewer = new PatternCodeViewer();
    JButton generateCode = new JButton("Generate Source Code");
    JPanel west = new JPanel();
    JCheckBox isStatementList = new JCheckBox("Statementlist", true);

    PatternStorage patternStorage;
    

    public PatternTester(PatternStorage patternStorage) {
        this.patternStorage = patternStorage;
        this.setLayout(new BorderLayout());
        west.setLayout(new BoxLayout(west, BoxLayout.Y_AXIS));
        this.add(west, BorderLayout.WEST);
        this.add(patternCodeViewer, BorderLayout.CENTER);
        
        west.add(generateCode);
        west.add(isStatementList);
        
        generateCode.addActionListener((arg0) -> {
            PrintAST printAST = new PrintAST();
            
            String patternCode = patternCodeViewer.getPattern();
            
            DataflowPattern dataflowPattern = new DataflowPattern(true, DataType.String(), DataType.String(), "id", 0.0, 0.0, 0.0);
            
            LinkedList<String> codeLines = new LinkedList();
            for(String line : patternCode.split("\n")){
                line = line.trim();
                if(line.startsWith("\"")){
                    line = line.replaceFirst("\"", "").trim();
                }
                
                if(line.endsWith("\"")){
                    line = line.substring(0,line.length() - 1).trim();
                }
                if(line.endsWith("\",")){
                    line = line.substring(0,line.length() - 2).trim();
                }
                codeLines.add(line);
            }
            dataflowPattern.setCodeLines(codeLines);
            
            Map<String, TreeNode<INode>> subtrees = new HashMap<>();
            subtrees.put("%input", ASTFactory.createVar("inputVar"));
            subtrees.put("%output", ASTFactory.createVar("outputVar"));
            
            try{
                List<TreeNode<INode>> ASTs = dataflowPattern.generateAst(subtrees, patternStorage);            
            
                TreeNode<INode> finalAST = null;
                if(isStatementList.isSelected()){
                    TreeNode<INode> statementList = ASTFactory.createStatementList();
                    for(TreeNode<INode> statement : ASTs){
                        statementList.addChild(statement);
                    }
                    finalAST = statementList;
                }
                else{
                    if(ASTs.size() > 1){
                        JOptionPane.showMessageDialog(null, "INVALID: No statement list, but multiple patterns!");
                        return;
                    }
                    if(ASTs.isEmpty()){
                        JOptionPane.showMessageDialog(null, "INVALID: No patterns");
                        return;
                    }

                    finalAST = ASTs.get(0);
                }

                patternCodeViewer.setSourceCode("<?php\n\n" + printAST.prettyPrint(finalAST) +"\n\n?>");
                patternCodeViewer.updateAST(finalAST);
                
                
            } catch(Exception ex){
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                patternCodeViewer.setSourceCode("Exception occured: \n" + sw.toString());
            }
            
        });
    }
    
    
}
