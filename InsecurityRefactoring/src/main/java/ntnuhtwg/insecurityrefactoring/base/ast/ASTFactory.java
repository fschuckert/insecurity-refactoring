/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.ast;

import ntnuhtwg.insecurityrefactoring.base.ast.impl.AstDim;
import ntnuhtwg.insecurityrefactoring.base.ast.impl.AstStmtList;
import ntnuhtwg.insecurityrefactoring.base.ast.impl.AstString;
import ntnuhtwg.insecurityrefactoring.base.ast.impl.AstVar;
import ntnuhtwg.insecurityrefactoring.base.ast.impl.If;
import ntnuhtwg.insecurityrefactoring.base.tree.TreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;

/**
 *
 * @author blubbomat
 */
public class ASTFactory {
    
//    public static TreeNode<INode> createArrayAccess(TreeNode<INode> input, TreeNode<INode> attribute){
//        AstDim dim = new AstDim();
//        dim.
//    }
    
    private static TreeNode<INode> createBasic(String type){
        BaseNode node = new BaseNode().addProperty("type", type);
        TreeNode<INode> treeNode = new TreeNode<>(node);
        
        return treeNode;
    }
    
    public static TreeNode<INode> createVar(String varName){
        TreeNode<INode> astVar  = new TreeNode<>(new AstVar());
        
        TreeNode<INode> string = new TreeNode<>(new AstString(varName));
        astVar.addChild(string);
        
        return astVar;
    }
    
    public static TreeNode<INode> createString(String varName){
        TreeNode<INode> string = new TreeNode<>(new AstString(varName));
        return string;
    }
    
    public static TreeNode<INode> createStatementList(){
        TreeNode<INode> statementList = new TreeNode<>(new AstStmtList());
        return statementList;
    }

    /**
     * creates a if statement, returns the statementlist of true condition
     * @param condition
     * @param ret_stmtList returns a referenz to the stmtlist to add statements to the if statement
     * @return 
     */
    public static TreeNode<INode> createIfStatement(TreeNode<INode> condition, TreeNode<INode> ret_stmtList) {
        TreeNode<INode> ifNode = new TreeNode<>(new If());
        TreeNode<INode> if_elem = createBasic("AST_IF_ELEM");        
        ifNode.addChild(if_elem);
        
        if_elem.addChild(condition);      
        if_elem.addChild(ret_stmtList);
        
        return ifNode;
    }
}
