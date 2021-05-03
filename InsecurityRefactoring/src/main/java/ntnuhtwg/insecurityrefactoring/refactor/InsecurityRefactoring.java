/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.refactor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ntnuhtwg.insecurityrefactoring.base.ASTNodeTypes;
import ntnuhtwg.insecurityrefactoring.base.ASTType;
import ntnuhtwg.insecurityrefactoring.base.GlobalSettings;
import ntnuhtwg.insecurityrefactoring.base.RefactoredCode;
import ntnuhtwg.insecurityrefactoring.base.SourceLocation;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.ast.ASTFactory;
import ntnuhtwg.insecurityrefactoring.base.exception.TimeoutException;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
import ntnuhtwg.insecurityrefactoring.base.tree.TreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher.DataflowDSL;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.patterns.PassthroughPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.DataflowIdentifyPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.DataflowPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.FailedSanitizePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.InsecureSourcePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SanitizePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SourcePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;
import ntnuhtwg.insecurityrefactoring.base.print.PrintAST;
import org.apache.commons.lang3.StringUtils;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.javatuples.Tuple;
import scala.NotImplementedError;

/**
 *
 * @author blubbomat
 */
public class InsecurityRefactoring {
    
    DataflowDSL dsl;
    
    // prevent conflicts in modifications
    Map<Long, TreeNode<INode>> modifiedFiles = new HashMap<>();
    Map<Long, TreeNode<INode>> modifiedNodes = new HashMap<>();
//    Set<Long> modifiedNodes = new HashSet<>();
    PatternStorage patternStorage;

    public InsecurityRefactoring(DataflowDSL dsl, PatternStorage patternStorage) {
        this.dsl = dsl;
        this.patternStorage = patternStorage;
    }
    
    private long addToModifiedGetId(DFATreeNode node) throws TimeoutException{
        INode topLevel = dsl.getTopLevelOfFile(node.getObj());
            
        if(!modifiedFiles.containsKey(topLevel.id())){
            TreeNode<INode> fileAST = dsl.getSubTree(topLevel.id());
            if(fileAST != null){
                modifiedFiles.put(topLevel.id(), fileAST);
            }
        }
        
        return topLevel.id();
    } 
    
    
    
    public List<RefactoredCode> refactor(List<Triplet<DFATreeNode, SanitizePattern, SanitizePattern>> refactorTargets, List< Pair<DFATreeNode, DataflowPattern> > dataflowRefactors, Pair<DFATreeNode, InsecureSourcePattern> sourceExchange) throws TimeoutException{
        
        modifiedFiles.clear();
        
        if(sourceExchange != null){
            DFATreeNode node = sourceExchange.getValue0();
            SourcePattern secureSource = sourceExchange.getValue0().getSourcePattern();
            InsecureSourcePattern insecureSource =  sourceExchange.getValue1();          
            long topLevelId = addToModifiedGetId(node);
            
            refactorPattern(topLevelId, node, secureSource, insecureSource);  
        }
        
        // complete tree
        for(Triplet<DFATreeNode, SanitizePattern, SanitizePattern> refactorData : refactorTargets){            
            DFATreeNode node = refactorData.getValue0();
            SanitizePattern sanitize = refactorData.getValue1();
            SanitizePattern failedSan = refactorData.getValue2();            
            long topLevelId = addToModifiedGetId(node);
            
            refactorPattern(topLevelId, node, sanitize, failedSan);            
        }
        System.out.println("dataflows: " + dataflowRefactors.size());
        
        for(Pair<DFATreeNode, DataflowPattern> pair : dataflowRefactors){
            long topLevelId = addToModifiedGetId(pair.getValue0());
            DataflowPattern dataflowPattern = pair.getValue1();
            DataflowIdentifyPattern dataflowIdentifyPattern = patternStorage.getDataflowIdentify(dataflowPattern.getIdentifyPattern());
            refactorPattern(topLevelId, pair.getValue0(), dataflowIdentifyPattern, dataflowPattern);
        }
        
        return createFilesPartial();
    }
    
    private List<RefactoredCode> createFilesPartial(){
        List<RefactoredCode> refactoredCode = new LinkedList<>();
        PrintAST printAST = new PrintAST();
        markStatementsToGenerate();
        
        for(TreeNode<INode> modifiedFile : modifiedFiles.values()){
            SourceLocation sourceLocation = Util.codeLocation(dsl.getDb(), modifiedFile.getObj());
            System.out.println("Writing code...");
            HashSet<Integer> modifiedLines = new HashSet<>();
            String code = modifiedCode(modifiedFile, sourceLocation, modifiedLines);
           
            
            refactoredCode.add(new RefactoredCode(sourceLocation, code, modifiedLines));
            System.out.println("Writing code finished");
        }
        
        return refactoredCode;
    }
    
    private String modifiedCode(TreeNode<INode> statementList, SourceLocation sourceLocation , Set<Integer> modifiedLines){
        Pair<String, Integer> codeToLine = modifiedCodeRec(statementList, sourceLocation, -1, modifiedLines);
        
        Integer writeFrom = codeToLine.getValue1()+1;
        String code = codeToLine.getValue0();
        code += sourceLocation.codeSnippet(writeFrom, -1);
        System.out.println("Writing: " + writeFrom + "-" +  (-1));
        
        return code;
    }
    
     private Pair<String, Integer> modifiedCodeRec(TreeNode<INode> statementList, SourceLocation sourceLocation, int writtenToLine, Set<Integer> modifiedLines){        
        // recursion
        String code = "";        
      
        for(TreeNode<INode> statement : statementList.getChildren()){                
            if(statement.getObj().isModified() && Util.isType(statementList.getObj(), ASTNodeTypes.STMT_LIST)){
                if(writtenToLine < lineNo(statement)){
                    code += sourceLocation.codeSnippet(writtenToLine+1, lineNo(statement) -1);       
                    System.out.println("Writing: " + writtenToLine + "-" + (lineNo(statement) -1));
                    writtenToLine = lineNo(statement) -1;

                }
                PrintAST printAST = new PrintAST();
                TreeNode<INode> stmtList = ASTFactory.createStatementList();
                stmtList.addChild(statement);
                String modifiedCodePart =  printAST.prettyPrint(stmtList);
                
                markModifiedLines(modifiedCodePart, code, modifiedLines);
                code += modifiedCodePart;                
                writtenToLine = getMaxLineNoValue(statement);
            }
            else{                        
                Pair<String, Integer> recResult = modifiedCodeRec(statement, sourceLocation, writtenToLine, modifiedLines);
                code += recResult.getValue0();
                writtenToLine = recResult.getValue1();
            }
        }
        
        return new Pair<>(code, writtenToLine);
    }

    private void markModifiedLines(String modifiedCodePart, String code, Set<Integer> modifiedLines) {
        // mark modified lines -> for manual reviewing in GUI
        int startLine = lines(code);
        if(startsWithNewLine(modifiedCodePart)){
            startLine++;
        }
        
        int modifiedLineAmount = lines(modifiedCodePart);
        if(endsWithNewLine(modifiedCodePart)){
            modifiedLineAmount--;
        }
        
        for(int i = startLine; i<startLine + modifiedLineAmount; i++){
            modifiedLines.add(i);
        }
    }
     
    private static int lines(String str){
        return StringUtils.countMatches(str, "\n") + 1;
    }
    
    private static boolean startsWithNewLine(String str){
        return str.replace(" ", "").replace("\t", "").startsWith("\n");
    }
    
    private static boolean endsWithNewLine(String str){
        return str.replace(" ", "").replace("\t", "").endsWith("\n");
    }
   
    
    private void markStatementsToGenerate(){
        for(TreeNode<INode> modifiedNode : modifiedNodes.values()){
            markToGenerate(modifiedNode);
        }
    }
    
    private void markToGenerate(TreeNode<INode> toGenerate){
        if(toGenerate.getParent() == null){
            toGenerate.getObj().markModified();
            return;
        }
        
        if(!Util.isType(toGenerate.getParent().getObj(), ASTNodeTypes.STMT_LIST)){
            throw new NotImplementedError("This should not happen at generating!");
        }
        
        toGenerate.getObj().markModified();
        
        TreeNode<INode> statementList = toGenerate.getParent();
        int myIndex = statementList.getChildren().indexOf(toGenerate);
        
        TreeNode<INode> nextNode = null;       
        int nextNodeIndex = myIndex + 1;
        if(nextNodeIndex < statementList.getChildren().size()){
            nextNode = statementList.getChild(nextNodeIndex);
        }
        
        TreeNode<INode> prevNode = null;
        int prevNodeIndex = myIndex-1;
        if(myIndex > 0){
            prevNode = statementList.getChild(prevNodeIndex);
        }
        
        while(prevNode != null && lineNo(prevNode) >= lineNo(toGenerate)){
            markNode(prevNode);
            setMaxLineNo(prevNode, getMaxLineNoRec(prevNode));
            prevNodeIndex--;
            if(prevNodeIndex >= 0) {prevNode=statementList.getChild(prevNodeIndex);} else{prevNode=null;}
        }
        
        while(nextNode != null && lineNo(nextNode) <= maxLineNo(toGenerate)){
            markNode(nextNode);
            setMaxLineNo(nextNode, getMaxLineNoRec(nextNode));
            nextNodeIndex++;
            if(nextNodeIndex < statementList.getChildren().size()) { nextNode = statementList.getChild(nextNodeIndex); } else { nextNode = null; }
        }
        
        if(lineNo(statementList) == lineNo(toGenerate)){
            TreeNode<INode> nextStatement = getFirstStatement(statementList);
            if(nextStatement != null){
                setMaxLineNo(nextStatement, getMaxLineNoRec(nextStatement));
                markToGenerate(nextStatement);
            }
        }
    }
    
    private void markNode(TreeNode<INode> node){
        node.getObj().markModified();
    }
    
    private int maxLineNo(TreeNode<INode> node){
        return node.getObj().getInt("maxlineno");
    }
    
    private int lineNo(TreeNode<INode> node){
        return node.getObj().getInt("lineno");
    }
  
//    private List<RefactoredCode> createFilesFull(){
//        List<RefactoredCode> refactoredCode = new LinkedList<>();
//        PrintAST printAST = new PrintAST();
//        for(TreeNode<INode> modifiedFile : modifiedFiles.values()){
//            SourceLocation sourceLocation = Util.codeLocation(dsl.getDb(), modifiedFile.getObj());
//            System.out.println("Writing code...");
//            String code = "<?php\n";
//            code += printAST.prettyPrint(modifiedFile);
//            code += "\n?>";
//           
//            
//            refactoredCode.add(new RefactoredCode(sourceLocation, code));
//            System.out.println("Writing code finished");
//        }
//        
//        return refactoredCode;
//    }
    
    private TreeNode<INode> getFirstStatement(TreeNode<INode> node){
        if(node == null){
            throw new NotImplementedError("Cannot generate a modified pattern out of a statement list");
        }
        
        if(Util.isType(node.getParent().getObj(), ASTNodeTypes.STMT_LIST)){
            return node;
        }
        
        return getFirstStatement(node.getParent());
    }
    
    private int getMaxLinenoOfStatement(TreeNode<INode> expInsideStatement){
        TreeNode<INode> statement = getFirstStatement(expInsideStatement);
        TreeNode<INode> stmtList = statement.getParent();
        
        // check if another child exists after the modified node
        if(stmtList.getChildren().size() > statement.getObj().getInt("childnum") + 1){
            TreeNode<INode> statementAfter = stmtList.getChild(statement.getObj().getInt("childnum") + 1);
            return statementAfter.getObj().getInt("lineno") - 1;
        }
        
        // we are the last child. We have to check how long it goes
        return getMaxLineNoRec(statement);
    }
    
    
    private void refactorPattern(Long fileId, DFATreeNode node, Pattern identifyPattern, Pattern intoPattern){
        System.out.println("Refactor pattern " + identifyPattern.getName() + " into " + intoPattern.getName());
        try{
//            PassthroughPattern identifyPassthrough = (PassthroughPattern)identifyPattern;
//            PassthroughPattern intoPatternPassthrough = (PassthroughPattern)intoPattern;


            TreeNode<INode> fileAST = modifiedFiles.get(fileId);

            TreeNode<INode> toModify = findSubnode(fileAST, node.getObj().id());
            Map<String, TreeNode<INode>> allNodes = identifyPattern.findAllRefactoringNodes(toModify);


            if(identifyPattern.isReturnOutput() || identifyPattern.isExpression()){
                TreeNode<INode> parent = toModify.getParent();
                List<TreeNode<INode>> expressions = intoPattern.generateAst(allNodes, patternStorage);
                if(expressions.size() != 1){
                    throw new NotImplementedError("Pattern is probably invalid. Should only return one expression!");
                }
                else{
                    TreeNode<INode> expression = expressions.get(0);
                    int maxLineNo = getMaxLinenoOfStatement(toModify);
                    parent.replaceChild(expression, toModify.getObj().getInt("childnum"));                    
                    TreeNode<INode> statement = getFirstStatement(expression);
                    markModifiedNodes(statement, fileId);
                    setMaxLineNo(statement, maxLineNo);
                   
                    this.modifiedNodes.put(fileId, statement);
                }
            }
            else{
                // replace statement            
                TreeNode<INode> statementList = toModify.getParent();
                int lineNo = toModify.getObj().getInt("lineno");
                if(!Util.isType(statementList.getObj(), ASTNodeTypes.STMT_LIST)){
                    throw new NotImplementedError("Refactoring statement which is not inside a statement list. Not supported yet.");
                }

                List<TreeNode<INode>> intoStatements = intoPattern.generateAst(allNodes, patternStorage);
                int maxLineNo = getMaxLineNoRec(toModify);
                statementList.replaceChildWithList(toModify, intoStatements);     
                markModifiedNodes(intoStatements, lineNo, fileId);
                setMaxLineNo(intoStatements, maxLineNo);
            }
        }
        catch(Exception ex){
            System.out.println("failed to apply refactoring from " + identifyPattern.getName() + " to: " + intoPattern.getName());
            ex.printStackTrace();
        }
        
        System.err.println("Refactor pattern finished");
    }
    
    private void setMaxLineNo(List<TreeNode<INode>> nodes, int maxLineNo){
        for(TreeNode<INode> statement : nodes){
            setMaxLineNo(statement, maxLineNo);
        }
    }
    
    private void setMaxLineNo(TreeNode<INode> node, int maxLineNo){
        node.getObj().setProperty("maxlineno", maxLineNo);
    }
    
    private int getMaxLineNoValue(TreeNode<INode> node){
        return node.getObj().getInt("maxlineno");
    }
    
    private int getMaxLineNoRec(TreeNode<INode> node){
        int maxLineNo = node.getObj().containsKey("lineno") ? lineNo(node) : -1;
        if(node.getObj().containsKey("maxlineno")){
            maxLineNo = Math.max(node.getObj().getInt("maxlineno"), maxLineNo);
        }
        
        for(TreeNode<INode> child : node.getChildren()){
            int childLineNo = getMaxLineNoRec(child);
            maxLineNo = Math.max(maxLineNo, childLineNo);
        }
        
        System.out.println("Max: " + maxLineNo);
        return maxLineNo;
    }
    
    
   private void markModifiedNodes(List<TreeNode<INode>> nodes, int lineNo, Long fileId){
       for(TreeNode<INode> node : nodes){
           node.getObj().setProperty("lineno", lineNo);
           this.modifiedNodes.put(fileId, node);
           markModifiedNodes(node, fileId);
       }
   }
    
    private void markModifiedNodes(TreeNode<INode> node, Long fileId){
        node.getObj().markModified();
        
        for(TreeNode<INode> child : node.getChildren()){
            markModifiedNodes(child, fileId);            
        }
    }
    
    
    private TreeNode<INode> findSubnode(TreeNode<INode> node, long id){
        if(node == null){
            return null;
        }
        if(node.getObj().id() == id){
            return node;
        }
        
        for(TreeNode<INode> child : node.getChildren()){
            
            TreeNode<INode> subNode = findSubnode(child, id);
            if(subNode != null){
                return subNode;
            }
        }
        
        return null;
    }


}
