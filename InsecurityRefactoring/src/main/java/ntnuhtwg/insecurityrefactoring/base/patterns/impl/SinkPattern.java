/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.patterns.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import ntnuhtwg.insecurityrefactoring.base.ast.ASTFactory;
import ntnuhtwg.insecurityrefactoring.base.ast.AnyNode;
import ntnuhtwg.insecurityrefactoring.base.ast.FixedNode;
import ntnuhtwg.insecurityrefactoring.base.context.EscapeChar;
import ntnuhtwg.insecurityrefactoring.base.exception.GenerateException;
import ntnuhtwg.insecurityrefactoring.base.tree.TreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher.DataflowDSL;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.patternpersist.PatternEntry;
import ntnuhtwg.insecurityrefactoring.base.patternpersist.PatternParser;
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;
import org.neo4j.driver.types.Node;

/**
 *
 * @author blubbomat
 */
public class SinkPattern extends Pattern{
    
    private String vulnType;
    private boolean isSafe = false;
    private Set<EscapeChar> sufficientEscapeChars = new HashSet<>();
    
    List<String> generateOutputCodeLines;

    public void setGenerateOutputCodeLines(List<String> generateOutputCodeLines) {
        this.generateOutputCodeLines = generateOutputCodeLines;
    }
    
    public boolean containsOutputCodeLines(){
        return generateOutputCodeLines != null && !generateOutputCodeLines.isEmpty();
    }
    
    public TreeNode<INode> generateOutputStatementAst(PatternStorage patternStorage, TreeNode<INode> input){
        Map<String, TreeNode<INode>> subTrees = Collections.EMPTY_MAP;
        if(input != null){
            subTrees = new HashMap<>();
            subTrees.put("%input", input);
        }
        
        TreeNode<INode> statementList = ASTFactory.createStatementList();
        for (String code : generateOutputCodeLines) {
            TreeNode<PatternEntry> tree = PatternParser.parsePatternCode(code, getName());
            statementList.addChild(generateASTRec(subTrees, patternStorage, tree));
        }

        return statementList;        
    }

    public String getVulnType() {
        return vulnType;
    }

    public void setVulnType(String vulnType) {
        this.vulnType = vulnType;
    }

    public boolean isIsSafe() {
        return isSafe;
    }

    public void setIsSafe(boolean isSafe) {
        this.isSafe = isSafe;
    }

    public Set<EscapeChar> getSufficientEscapeChars() {
        return sufficientEscapeChars;
    }
    
    
    
    

    
    public void addSufficientEscapeChar(EscapeChar chr){
        this.sufficientEscapeChars.add(chr);
    }
    
   

    public boolean generateOutputContainsInput() {
        for(String line : generateOutputCodeLines){
            if(line.contains("%input")){
                return true;
            }
        }
        
        return false;
    }

    

    
}
