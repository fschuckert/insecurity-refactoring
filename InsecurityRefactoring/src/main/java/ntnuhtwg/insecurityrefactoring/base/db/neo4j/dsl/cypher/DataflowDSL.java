/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.ASTNodeTypes;
import ntnuhtwg.insecurityrefactoring.base.ASTType;
import ntnuhtwg.insecurityrefactoring.base.SourceLocation;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.exception.TimeoutException;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
import ntnuhtwg.insecurityrefactoring.base.tree.TreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4jDB;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SinkPattern;

/**
 *
 * @author blubbomat
 */
public class DataflowDSL {
    private Neo4jDB db;

    public DataflowDSL(Neo4jDB neo4JConnector) {
        this.db = neo4JConnector;
    }
    
    public Neo4jDB getDb(){
        return db;
    }
    
//    public  List<INode> findPatterns(SinkPattern pattern) {
//         return db.findAll(
//                 "MATCH (n) WHERE " + pattern.cypherWhere("n") + " RETURN n"
//         );
//    }
    
    public List<INode> children(INode node) throws TimeoutException{
        return db.findAll(
                "MATCH (p)-[:PARENT_OF]-> (c) WHERE id(p)=$id RETURN c ORDER BY c.childnum ASC", 
                "id", node.id()
        );
    }
    
    public INode child(INode parent, int index) throws TimeoutException{
        return db.findFirstNode(
                "MATCH (p)-[:PARENT_OF]->(c) WHERE id(p)=$id AND c.childnum=$ind RETURN c", 
                "id", parent.id(),
                "ind", index
        );
    }
    
    public List<INode> getConcatChildren(INode node) throws TimeoutException{
        if(Util.isType(node, ASTNodeTypes.BINARY_OP) || Util.isType(node, ASTNodeTypes.ENCAPS_LIST) ||  Util.isType(node, ASTNodeTypes.CAST)){
            return db.findAll(
                    "MATCH (parent) -[:PARENT_OF]-> (child) WHERE id(parent)=$parentId RETURN child",
                    "parentId", node.id()
            );
        }
        else if( Util.isType(node, ASTNodeTypes.CALL) ){
            int i =10;
//            if( "sprintf".equals(getCallName(node, false).toLowerCase()) ||
//                "vsprintf".equals(getCallName(node, false).toLowerCase()) ){
//                return getCallParameters(node);
//            }
            
            
        }
        else if(Util.isType(node, ASTNodeTypes.MINI_IF) ){
            List<INode> results = new LinkedList<>();
            results.add(child(node, 1));
            results.add(child(node, 2));
            return results;
        }
        else if(Util.isType(node, ASTNodeTypes.ARRAY)){
            return db.findAll("MATCH (arr)-[:PARENT_OF]->(elem)-[:PARENT_OF]->(child) WHERE id(arr)=$arrId AND child.childnum=0 return child", 
                    "arrId", node.id());
        }
        
        if(!Util.isAnyOf(node, new String[]{ASTNodeTypes.CALL, ASTNodeTypes.METHOD_CALL, ASTNodeTypes.STATIC_CALL, ASTNodeTypes.STRING, ASTNodeTypes.VARIABLE, ASTNodeTypes.DIM, ASTNodeTypes.PROP, ASTNodeTypes.CONST, ASTNodeTypes.INTEGER})){
            System.out.println("Concat unknown: " + node);
            SourceLocation loc = Util.codeLocation(db, node);
            System.out.println("" + loc.codeSnippet(true));
        }
        return Collections.EMPTY_LIST;
    }

    public String getVariableName(INode node) throws TimeoutException {
        INode stringNode = db.findFirstNode(
                "MATCH (var)-[:PARENT_OF]->(str) WHERE id(var)=$id RETURN str", 
                "id", node.id()
        );
        
        return stringNode.getString("code");
    }
    
    public INode getTopLevelOfFile(INode node) throws TimeoutException{
//        "MATCH (parent)-[:FILE_OF|:PARENT_OF*]->(child) WHERE id(child)=$id AND parent.type='File' return parent", 
//                              "MATCH p=(parent)-[*]->(child) WHERE id(child)=$id AND parent.type='File' AND ALL(rs IN relationships(p) WHERE type(rs) in['FILE_OF', 'PARENT_OF']) return parent", 
        return db.findFirstNode("MATCH p=(parent)-[:PARENT_OF*]->(child) WHERE id(child)=$id AND parent.type='AST_TOPLEVEL' AND 'TOPLEVEL_FILE' in parent.flags return parent",
                "id", node.id());
    }
    
    public INode getStatement(INode node) throws TimeoutException{
//        System.out.println("get statement for: " + node.id());
        INode parent;
        INode child = node;
        
        do{
            parent = parent(child);
            
            if(Util.isType(parent, ASTNodeTypes.STMT_LIST)){
                return child;
            }
            
            child = parent;
        } while(parent != null);
    
        System.out.println("cannot find statement");
        return null;
        
//        return db.findFirstNode(
//                "MATCH (stmt_list{type:'AST_STMT_LIST'})-[:PARENT_OF]->(stmt), (n), p=shortestPath((stmt)-[:PARENT_OF*]->(n)) WHERE id(n)=$id AND NONE(c in nodes(p) WHERE c.type='AST_STMT_LIST') return stmt", 
//                "id", node.id()
//        );
    }
    
    public List<INode> backReaches(INode node, String varName) throws TimeoutException{
        return db.findAll(
                "MATCH (stmtA)-[:REACHES{var:$varName}]->(stmtB) WHERE id(stmtB)=$id RETURN stmtA", 
                "id", node.id(),
                "varName", varName
        );
    }
    
    public INode getExpressionFromAssignment(INode assignment) throws TimeoutException{
        return db.findFirstNode(
                "MATCH (assign)-[:PARENT_OF]->(exp) "
                        + "WHERE id(assign)=$id "
                        + "AND (assign.type='AST_ASSIGN' OR assign.type='AST_ASSIGN_OP') "
                        + "AND exp.childnum=1 "
                        + "RETURN exp", 
                "id", assignment.id()
        );
    }
    
    public INode getVarFromAssignment(INode assignment) throws TimeoutException{
        return db.findFirstNode(
                "MATCH (assign)-[:PARENT_OF]->(var) WHERE id(assign)=$id AND var.childnum=0 RETURN var",
                "id", assignment.id()
        );
    }
    
    public INode getVarFromDim(INode dim) throws TimeoutException{
        return db.findFirstNode(
                "MATCH (dim)-[:PARENT_OF]->(var) WHERE id(dim)=$id AND var.childnum=0 RETURN var",
                "id", dim.id()
        );
    }
    
    public INode getFunctionFromCall(INode call) throws TimeoutException{
        return db.findFirstNode(
                "MATCH (call)-[:CALLS]->(func) WHERE id(call)=$id RETURN func", 
                "id", call.id()
        );
    }
    
    public List<INode> getAllReturnNodes(INode funcDeclaration) throws TimeoutException{
        return db.findAll(
                "MATCH (func) -[:PARENT_OF*]-> (ret {type: 'AST_RETURN'}) WHERE id(func)=$id RETURN ret", 
                "id", funcDeclaration.id()
        );
    }
    
    public INode getExpressionFromReturn(INode returnNode) throws TimeoutException{
        return db.findFirstNode(
                "MATCH (ret {type:'AST_RETURN'})-[:PARENT_OF]->(exp {childnum:0}) WHERE id(ret)=$id RETURN exp", 
                "id", returnNode.id()
        );
    }
    
    public INode getExpressionFromCall(INode call, int parameterIndex) throws TimeoutException{
        return db.findFirstNode("MATCH (call)-[:PARENT_OF]->(ARG_LIST {type:'AST_ARG_LIST'})-[:PARENT_OF]->(exp) WHERE exp.childnum=$paramIndex AND id(call)=$id  return exp", 
                "id", call.id(),
                "paramIndex", parameterIndex);
    }
    
    public List<INode> getFunctionCallsFromParam(INode param) throws TimeoutException{
        return db.findAll(
                "MATCH (call)-[:CALLS]->(func)-[:PARENT_OF]->(paramList)-[:PARENT_OF]->(param) WHERE id(param)=$id return call", 
                "id", param.id()
        );
    }
    
    public boolean isFunctionCall(INode call, String name) throws TimeoutException{
        return db.findAll(
                "MATCH (cal) -[:PARENT_OF]->(name)-[:PARENT_OF]->(str) WHERE id(cal)=$callId AND str.code=$funcName return cal",
                "callId", call.id(),
                "funcName", name
        ).size() != 0;
    }

    public INode parent(INode child) throws TimeoutException {
        return db.findFirstNode("MATCH (p)-[:PARENT_OF]->(c) WHERE id(c)=$childId RETURN p", 
                "childId", child.id()
                );
    }
    
    public TreeNode<INode> getSubTree(Long rootNodeId) throws TimeoutException{
        INode node = db.findNode(rootNodeId);
        TreeNode<INode> retval = new TreeNode<>(node);
        List<INode> children = children(node);
        for(INode child : children){
            TreeNode<INode> childTree = getSubTree(child.id());
            if(childTree != null){
                retval.addChild(childTree);
            }
        }
        
        return retval;
    }

    public String getCallName(INode call, boolean addCall)  throws TimeoutException{
        if(Util.isType(call, ASTNodeTypes.CALL)){
            INode name = child(call, 0);
            INode str = child(name, 0);            
            return addCall ? "CALL: " + str.getString("code") : str.getString("code");
        }
        else if(Util.isType(call, ASTNodeTypes.STATIC_CALL)){
            INode method = child(call, 1);
            return addCall ? "STATIC: " + method.getString("code") : method.getString("code");
        }
        else if(Util.isType(call, ASTNodeTypes.METHOD_CALL)){
            INode method = child(call, 1);
            return addCall ? "METHOD: " + method.getString("code") : method.getString("code");
        }
        
        return "Unsupported call: " + call;
    }

    public INode getCallParameter(INode call, int paramIndex)  throws TimeoutException{
        return db.findFirstNode("MATCH (call)-[:PARENT_OF]->(arg_list)-[:PARENT_OF]->(arg) WHERE arg_list.type=\"AST_ARG_LIST\" AND arg.childnum=$paramNum AND id(call)=$callId return arg", 
                "callId", call.id(),
                "paramNum", paramIndex
                );
    }
    
    public List<INode> getCallParameters(INode call)  throws TimeoutException{
        return db.findAll("MATCH (call)-[:PARENT_OF]->(arg_list)-[:PARENT_OF]->(arg) WHERE arg_list.type=\"AST_ARG_LIST\" AND id(call)=$callId return arg", 
                "callId", call.id()
                );
    }

    public List<INode> getControlStatements(INode fromNode, INode toNode, String variableName)  throws TimeoutException{
//        System.out.println("from " + fromNode.id());
//        System.out.println("to " + toNode.id());
//        System.out.println("Var " + variableName);
//        List<INode> retval = new LinkedList<>();
//        List<INode> reachesTo = db.findAll("match (a)-[:REACHES{var:$varName}]->(between)  WHERE id(a)=$from RETURN between", 
//                "from", fromNode.id(), 
//                "varName", variableName);
//        
//        for(INode reach : reachesTo){
//            
//        }
        
        return db.findAll("match (a)-[:FLOWS_TO*]->(between)-[:FLOWS_TO*]->(b), (a)-[:REACHES{var:$varName}]->(between) WHERE id(a)=$fromNodeId AND id(b)=$toNodeId RETURN between", 
                "fromNodeId", fromNode.id(),
                "toNodeId", toNode.id(),
                "varName", variableName
                );
    }

    public boolean isIfStatementExpression(INode controlStatement)  throws TimeoutException{
        INode parent = parent(controlStatement);
        return parent != null && Util.isType(parent, ASTNodeTypes.IF_ELEM);
    }

    public boolean ifStatementContainsStatement(INode condition, DFATreeNode expression)  throws TimeoutException{
//        INode ifNode = db.findFirstNode("match (if)-[:PARENT_OF*2]->(cond), (if)-[:PARENT_OF*]->(expr) WHERE id(cond)=$conditionId AND id(expr)=$expressionId RETURN if", 
        INode ifNode = db.findFirstNode("match (if)-[:PARENT_OF*2]->(cond)WHERE id(cond)=$conditionId RETURN if", 
                "conditionId", condition.id()
                );
        
        return ifNode != null && db.findFirstNode("match (if)-[:PARENT_OF*]->(expr) WHERE id(if)=$ifId AND id(expr)=$expressionId RETURN if", 
                "ifId", ifNode.id(),
                "expressionId", expression.getObj().id()
                ) != null;
    }

    public boolean ifElementContainsStatement(INode condition, DFATreeNode expression) throws TimeoutException {        
        INode ifElementNode = db.findFirstNode("match (if)-[:PARENT_OF]->(cond)WHERE id(cond)=$conditionId RETURN if", 
                "conditionId", condition.id()
                );
        
        return ifElementNode != null && db.findFirstNode("match (if)-[:PARENT_OF*]->(expr) WHERE id(if)=$ifId AND id(expr)=$expressionId RETURN if", 
                "ifId", ifElementNode.id(),
                "expressionId", expression.getObj().id()
                ) != null;
    }

    public boolean containsVariable(INode parent, String varName)  throws TimeoutException{
        return !db.findAll(
                "match (p)-[:PARENT_OF*]->(var)-[:PARENT_OF]->(str) WHERE var.type=\"AST_VAR\" AND id(p)=$parentId AND str.code=$varName return var",
                "parentId", parent.id(),
                "varName", varName
        ).isEmpty();
    }

    public List<INode> getSiblingsAfter(INode node) throws TimeoutException{       
        return db.findAll( "MATCH (l)-[:PARENT_OF]->(a), (l)-[:PARENT_OF]->(sib) WHERE id(a)=$id AND sib.childnum > a.childnum return sib", 
                "id", node.id()
                );
    }

    public List<INode> findAllTopLevel()  throws TimeoutException{
        return db.findAll( "MATCH (n) WHERE n.type='AST_TOPLEVEL' RETURN n");
    }

    
}
