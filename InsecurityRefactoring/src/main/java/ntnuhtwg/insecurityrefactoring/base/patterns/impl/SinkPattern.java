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
    
    
    public String findQuery(){
        // match (n)-[:PARENT_OF]->(var)-[:PARENT_OF]->(str), (n)-[:PARENT_OF]->(str2) WHERE str.code="blubb" AND str2.code="add" return n
        
        // match (n)-[:PARENT_OF]->(var)-[:PARENT_OF]->(str), (n)-[:PARENT_OF]->(str2) WHERE str.code="blubb" AND str2.code="add" return n
               
        List<TreeNode<INode>> astTree = generateAst(new HashMap<String, TreeNode<INode>>(), patternStorage);
        List<String> relations = new LinkedList<>();
        List<String> wheres = new LinkedList<>();
        
        createQueryRec("n", astTree.get(0), relations, wheres);
        
        StringJoiner andJoiner = new StringJoiner(" AND ");
        StringJoiner commaJoiner = new StringJoiner(", ");
        
        String query = "MATCH (n)";
        
        commaJoiner.add(query);
        for(String relation : relations){
            commaJoiner.add(relation);
        }
        query = commaJoiner.toString();
        
        for(String where : wheres){
            andJoiner.add(where);
        }
        
        query += " WHERE " + andJoiner.toString() + " RETURN (n)";
        
//        System.out.println("" + query);
        return query;        
    }
    
    private void createQueryRec(String parentName, TreeNode<INode> treeNode, List<String> relations, List<String> wheres){
        if(treeNode.getObj().containsKey("type")){
                String where = parentName + ".type=\"" + treeNode.getObj().get("type") + "\"";
                wheres.add(where);
        }
        
        if(treeNode.getObj().containsKey("code")){
                String where = parentName + ".code=\"" + treeNode.getObj().get("code") + "\"";
                wheres.add(where);
        }
        if(!treeNode.getObj().getFlags().isEmpty()){
            for(String flag : treeNode.getObj().getFlags()){
                String whereFlag = "\"" + flag + "\" in " + parentName + ".flags";
                wheres.add(whereFlag);
            }
        }
        
        int i=0;
        for(TreeNode<INode> child : treeNode.getChildren()){
            if(child.getObj() instanceof AnyNode ||
                    child.getObj() instanceof FixedNode ||
                    (child.getObj().containsKey("type") && "any".equals(child.getObj().get("type")))){
                continue;
            }
            
            String childName = parentName + "_" + i++;            
            String relation = "(" + parentName + ")-[:PARENT_OF]->(" + childName + ")";
            relations.add(relation);
            
            createQueryRec(childName, child, relations, wheres);
        }
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
