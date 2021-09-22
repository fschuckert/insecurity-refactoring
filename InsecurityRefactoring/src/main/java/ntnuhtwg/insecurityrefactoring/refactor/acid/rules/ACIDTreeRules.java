/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.refactor.acid.rules;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import ntnuhtwg.insecurityrefactoring.base.ASTNodeTypes;
import ntnuhtwg.insecurityrefactoring.base.SourceLocation;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher.DataflowDSL;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.exception.ResultTreeToLarge;
import ntnuhtwg.insecurityrefactoring.base.exception.TimeoutException;
import ntnuhtwg.insecurityrefactoring.base.patterns.PassthroughPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.ConcatPattern;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
import ntnuhtwg.insecurityrefactoring.base.tree.EdgeNames;
import ntnuhtwg.insecurityrefactoring.base.tree.LabeledTreeNode;

/**
 *
 * @author blubbomat
 */
public class ACIDTreeRules {

    private final PatternStorage patternReader;
    private final DataflowDSL dsl;
    private final boolean doConditionCheck;
    private int resolveCount = 0;
    private final int maxResolveCount = 500;

    public ACIDTreeRules(PatternStorage patternReader, DataflowDSL dsl, boolean doConditionCheck) {
        this.patternReader = patternReader;
        this.dsl = dsl;
        this.doConditionCheck = doConditionCheck;
    }

    public void resolveExpression(DFATreeNode expression, Set<SourceLocation> isInCall) throws ResultTreeToLarge, TimeoutException {

        if(resolveCount++ > maxResolveCount){
            throw new ResultTreeToLarge("To many resolves: " + resolveCount);
        }
        ConcatPattern concatPattern = null;
        List<DFATreeNode> expressions = new LinkedList<>();

        if (Util.isType(expression.getObj(), ASTNodeTypes.VARIABLE)) {
            expressions = resolveVariable(expression);
        } else if ((concatPattern = patternReader.isConcatenation(expression, dsl)) != null) {
            expressions = resolveConcat(expression, concatPattern);
        } else if (Util.isAnyCall(expression.getObj())) {
            expressions = resolveFunctionCall(expression);
        } else if (patternReader.isPassthroughType(expression.getObj(), dsl)) {
            expressions = resolveOtherPassthrough(expression);
        }

        // Rekursive call
        for (DFATreeNode subExpression : expressions) {
            if (subExpression != null && subExpression.getObj() != null && alreadyReachedNodeRec(subExpression.getParent_(), subExpression.getObj().id())) {
                new DFATreeNode(subExpression, "rekursive skip", null);
            } else {
                resolveExpression(subExpression, isInCall);
            }
        }
    }
    
      
    private List<DFATreeNode> resolveOtherPassthrough(DFATreeNode expression) throws TimeoutException{
        PassthroughPattern passthroughPattern = null;
        
        if((passthroughPattern = patternReader.isPassthrough(expression, dsl)) != null){
            return resolvePassthrough(expression, passthroughPattern);
        }
        
        return Collections.EMPTY_LIST;
    }
    
    private List<DFATreeNode> resolveVariable(DFATreeNode variable) throws TimeoutException{
        List<DFATreeNode> retval = new LinkedList<>();
        
        String variableName = dsl.getVariableName(variable.getObj());
        
        INode stmtVc1 = dsl.getStatement(variable.getObj());
        
        List<INode> backreaches = dsl.backReaches(stmtVc1, variableName);
        for(INode stmtVc2 : backreaches){
            
            List<DFATreeNode> nodesAfterCondition = conditionCheck(variable, stmtVc2, variableName);
            for(DFATreeNode conditionNode : nodesAfterCondition){
                if(Util.isType(stmtVc2, ASTNodeTypes.PARAM)){
                    DFATreeNode parameterNode = new DFATreeNode(conditionNode, "parameter", stmtVc2);
                    retval.addAll( parameter(parameterNode) );
                }
                else {
                    DFATreeNode assignedToVariable = createTreeToAssignVar(conditionNode, stmtVc2);
                    retval.addAll( resolveAssignment(assignedToVariable, stmtVc2) );
                }
            }
        }
        
        return retval;
    }

    private List<DFATreeNode> conditionCheck(DFATreeNode variable, INode stmtVc2, String variableName) throws TimeoutException {
        List<DFATreeNode> nodesAfterCondition = new LinkedList(Arrays.asList(variable));
        if(this.doConditionCheck){
            System.out.println("Checking for condition");
            nodesAfterCondition = ACIDTreeConditionsRules.resolveControlInBetween(variable, stmtVc2, variableName, dsl);
        }
        return nodesAfterCondition;
    }
    
    private List<DFATreeNode> resolveAssignment(DFATreeNode assignedToVariable, INode assignmentStmtVc2) throws TimeoutException{
        List<DFATreeNode> retval = new LinkedList<>();
        if (Util.isType(assignmentStmtVc2, ASTNodeTypes.ASSIGN) || Util.isType(assignmentStmtVc2, ASTNodeTypes.ASSIGN_OP)) {
            DFATreeNode assignment = new DFATreeNode(assignedToVariable, "is assigned", assignmentStmtVc2);
            
            if(Util.isType(assignmentStmtVc2, ASTNodeTypes.ASSIGN_OP)){
                DFATreeNode variable = new DFATreeNode(assignment, "assignment op", assignedToVariable.getObj());
                retval.add(variable);
            } 
            
            DFATreeNode expression = new DFATreeNode(assignment, "from expression",  dsl.getExpressionFromAssignment(assignmentStmtVc2));
            retval.add(expression);
            
                       
        }
        return retval;
    }
    
    private List<DFATreeNode> parameter(DFATreeNode parameter) throws TimeoutException{
        if(parameter.peekCall() == null){
            return findCalls(parameter);
        }
        else {
            DFATreeNode functionCall = parameter.popCallStack();
            return backToCall(parameter, functionCall);
        }
    }
    
    private List<DFATreeNode> findCalls(DFATreeNode parameter) throws TimeoutException{
        List<DFATreeNode> retval = new LinkedList<>();
        
        List<INode> callNodes = dsl.getFunctionCallsFromParam(parameter.getObj());
        for (INode callNode : callNodes) {
            DFATreeNode callDFA = new DFATreeNode(parameter, "call", callNode);
            
            INode expression = dsl.getExpressionFromCall(callNode, parameter.getObj().getInt("childnum"));
            DFATreeNode expressionDFA = new DFATreeNode(callDFA, "param", expression);
            retval.add(expressionDFA);
        }
        
        return retval;
    }
    
    private List<DFATreeNode> backToCall(DFATreeNode parameter, DFATreeNode functionCall) throws TimeoutException{
        List<DFATreeNode> retval = new LinkedList<>();
        
        int parameterIndex = parameter.getObj().getInt("childnum");
        INode callParameterExpNode = dsl.getExpressionFromCall(functionCall.getObj(), parameterIndex);
        DFATreeNode callParameterExp = new DFATreeNode(parameter, "param to exp", callParameterExpNode);
        
        retval.add(callParameterExp);
        
        return retval;
    }
    
    private List<DFATreeNode> resolveConcat(DFATreeNode concatenation, ConcatPattern concatPattern) throws TimeoutException{
        List<DFATreeNode> retval = new LinkedList<>();
        concatenation.setIsConcat(true);
        
        List<INode> inputNodes = concatPattern.findInputNodes(dsl, concatenation.getObj());
        
        for(INode inputNode : inputNodes){
            DFATreeNode input = new DFATreeNode(concatenation, "concat ", inputNode);
            retval.add(input);
        }
        return retval;
    }
    
    private List<DFATreeNode> resolveFunctionCall(DFATreeNode call) throws TimeoutException{
        PassthroughPattern passthroughPattern = null;
        INode functionDef = null;
        
        // call is Passthrough
        if((passthroughPattern = patternReader.isPassthrough(call, dsl)) != null){
            return resolvePassthrough(call, passthroughPattern);
        }
        // call has call edge
        else if((functionDef = dsl.getFunctionFromCall(call.getObj())) != null){
            call.pushCallStack(call);
            return returnStatements(call, functionDef);            
        }
        
        return Collections.EMPTY_LIST;
    }
    
    private List<DFATreeNode> resolvePassthrough(DFATreeNode call, PassthroughPattern passthroughPattern) throws TimeoutException{
        call.setIsPassthrough(true);
        List<DFATreeNode> retval = new LinkedList<>();
        List<INode> inputNodes = passthroughPattern.findInputNodes(dsl, call.getObj());
        for(INode inputNode : inputNodes){
            DFATreeNode input = new DFATreeNode(call, "passthrough", inputNode);
            retval.add(input);
        }
        
        return retval;        
    }
    
    private List<DFATreeNode> returnStatements(DFATreeNode call,  INode functionDef) throws TimeoutException{
        List<DFATreeNode> retval = new LinkedList<>();
        for (INode node : dsl.getAllReturnNodes(functionDef)) {
            DFATreeNode returnNode = new DFATreeNode(call, "returnStatement", node);
            DFATreeNode returnExpr = new DFATreeNode(returnNode, "return to exp", dsl.getExpressionFromReturn(returnNode.getObj()));
            retval.add(returnExpr);
        }
        
        return retval;
    }

    
    private DFATreeNode createTreeToAssignVar(DFATreeNode from, INode assignmentNode) throws TimeoutException{
        LinkedList<INode> nodesToVar = getNodesToVar(assignmentNode);
        for(INode nodeToVar : nodesToVar){
            from = new DFATreeNode(from, EdgeNames.TO_VAR, nodeToVar);
        }
        
        return from;
    }

    private LinkedList<INode> getNodesToVar(INode node) throws TimeoutException {
        if (Util.isType(node, ASTNodeTypes.VARIABLE)) {
            LinkedList<INode> retval = new LinkedList<>();
            retval.add(node);
            return retval;
        } else if (Util.isType(node, ASTNodeTypes.ASSIGN) || Util.isType(node, ASTNodeTypes.ASSIGN_OP)) {
            INode var = dsl.getVarFromAssignment(node);
            LinkedList<INode> retval = getNodesToVar(var);
            return retval;
        } else if (Util.isType(node, ASTNodeTypes.DIM)) {
            INode var = dsl.getVarFromDim(node);
            LinkedList<INode> retval = getNodesToVar(var);
            retval.add(node);
            return retval;
        } else if (Util.isType(node, ASTNodeTypes.PROP)) {
//            System.out.println("AST_PROP not supported yet") ;
            //todo
            return new LinkedList<>();
        } else if (Util.isType(node, ASTNodeTypes.ARRAY)) {

//            System.out.println("AST_ARRAY not supported yet") ;
            //todo
            return new LinkedList<>();
        } else if (Util.isType(node, ASTNodeTypes.CONST)) {
            //todo
            return new LinkedList<>();
        } else {
            String message = "Following node type is not supported yet: ";
            SourceLocation loc = Util.codeLocation(dsl.getDb(), node);
            message += loc.codeSnippet();

            if (node == null) {
                message += "object is null";
            } else {
                message += node + " id:" + node.id() + " type: " + node.get("type");
            }
            throw new UnsupportedOperationException(message);
        }
    }
    
    private boolean alreadyReachedNodeRec(DFATreeNode node, long id) {
        if (node.isRecursiveNodeCheck() && node.getObj().id() == id) {
            return true;
        }
        if (node.getParent() == null) {
            return false;
        }

        return alreadyReachedNodeRec(node.getParent_(), id);
    }
}
