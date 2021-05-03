/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.refactor.acid;

import ntnuhtwg.insecurityrefactoring.refactor.base.VarName;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import ntnuhtwg.insecurityrefactoring.base.ASTNodeTypes;
import ntnuhtwg.insecurityrefactoring.base.GlobalSettings;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
import ntnuhtwg.insecurityrefactoring.base.tree.LabeledTreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher.DataflowDSL;
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;
import ntnuhtwg.insecurityrefactoring.base.SourceLocation;
import ntnuhtwg.insecurityrefactoring.base.ast.AnyNode;
import ntnuhtwg.insecurityrefactoring.base.ast.BaseNode;
import ntnuhtwg.insecurityrefactoring.base.ast.ConditionNode;
import ntnuhtwg.insecurityrefactoring.base.ast.FixedNode;
import ntnuhtwg.insecurityrefactoring.base.ast.TimeoutNode;
import ntnuhtwg.insecurityrefactoring.base.exception.ResultTreeToLarge;
import ntnuhtwg.insecurityrefactoring.base.exception.TimeoutException;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4JConnector;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SourcePattern;
import org.neo4j.driver.internal.InternalNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4jDB;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.NodeConnected;
import ntnuhtwg.insecurityrefactoring.base.patterns.PassthroughPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SinkPattern;
import org.neo4j.kernel.impl.index.schema.CollectingIndexUpdater;

/**
 *
 * @author blubbomat
 */
public class ACIDTreeCreatorBackup implements Runnable {

    private Neo4jDB db;
    private PatternStorage patternReader;
    private DataflowDSL dsl;
    private INode sinkNode;
    private SinkPattern pattern;
    private boolean isPip = false;
    private int replaceIndex = -1;
    private boolean isPresearch = true;
    private final boolean controlFlowCheck;

    private DFATreeNode resultTree;

//    private boolean checkMissingCalls = true;
//    private boolean debugAddAll = true;
    private int stopOnDepth = 1000;
    private int stopOnWidth = 2000;

    private LinkedList<String> typesToSearch = new LinkedList<String>(Arrays.asList("sqli", "xss"));

    public ACIDTreeCreatorBackup(Neo4jDB db, PatternStorage patternReader, INode sinkNode, SinkPattern pattern, boolean controlFlowCheck) {
        this.db = db;
        this.patternReader = patternReader;
        this.dsl = new DataflowDSL(db);
        this.sinkNode = sinkNode;
        this.pattern = pattern;
        this.controlFlowCheck = controlFlowCheck;
    }

    public int getReplaceIndex() {
        return replaceIndex;
    }

    public void setReplaceIndex(int replaceIndex) {
        this.replaceIndex = replaceIndex;
    }

    public static List<DFATreeNode> getSourceNodes(DFATreeNode tree, PatternStorage patternStorage, Neo4jDB db) throws TimeoutException {
        List<DFATreeNode> sourceNodes = new LinkedList<>();

        for (LabeledTreeNode<INode> leafLabel : tree.getAllLeafs()) {
            DFATreeNode leaf = (DFATreeNode) leafLabel;
            for (SourcePattern sourcePattern : patternStorage.getSources()) {
                if (sourcePattern.equalsPattern(leaf.getObj(), db)) {
                    sourceNodes.add(leaf);
                }
            }
        }

        return sourceNodes;
    }

    private void resolveExpression(DFATreeNode expression, Set<SourceLocation> isInCall) throws ResultTreeToLarge, TimeoutException {
//        System.out.println("expression: " + expression.getObj() + " " + Util.codeLocation(db, expression.getObj()).toString());
        resolveConcatRec(expression);
        for (LabeledTreeNode<INode> leafLabel : expression.getAllLeafs()) {
            DFATreeNode leaf = (DFATreeNode) leafLabel;
            backtrackLeafNodeRec(leaf, isInCall);
        }
    }

    private void resolveConcatRec(DFATreeNode treeNode) throws TimeoutException {
//        System.out.println("resolve concat: " + treeNode.getObj() + " " + Util.codeLocation(db, treeNode.getObj()).toString());
        List<INode> concatChildren = dsl.getConcatChildren(treeNode.getObj());
        int i = 0;
        for (INode childNode : concatChildren) {
            DFATreeNode childTreeNode = new DFATreeNode(treeNode, "concat" + i++, childNode);
            resolveConcatRec(childTreeNode);
        }
    }

    private void backtrackLeafNodeRec(DFATreeNode treeNode, Set<SourceLocation> isInCall) throws ResultTreeToLarge, TimeoutException {
//        System.out.println("Backtrack: " + treeNode.getObj() + " " + Util.codeLocation(db, treeNode.getObj()).toString());
//        Util.debugPrintLoc(db, treeNode.getObj());
        int depth = treeNode.treeDepth();
        int width = treeNode.getWidth();
//        System.out.println("Depth: " + depth + " Width: " + width);
        if (stopOnDepth > 0 && depth > stopOnDepth) {
            System.out.println("Stopped because tree is too deep!");
            return;
        }

        if (stopOnWidth > 0 && width > stopOnWidth) {
            throw new ResultTreeToLarge("tree width to high");
//            System.out.println("Stopped because tree max width exceeded");
//            return;
        }
        if (Util.isType(treeNode.getObj(), ASTNodeTypes.VARIABLE)) {
            INode varNode = treeNode.getObj();
            String variableName = dsl.getVariableName(varNode);
            INode stmt = dsl.getStatement(varNode);
            int i = 0;
            int x = 0;
            List<INode> backreaches = dsl.backReaches(stmt, variableName);

            if (!backreaches.isEmpty()) {
//                DFATreeNode dfa = new DFATreeNode(treeNode, "reaches from", new AnyNode());
                for (INode backreachNode : dsl.backReaches(stmt, variableName)) {
                    if (backreachNode.getInt("lineno") >= stmt.getInt("lineno")) {
//                        System.out.println("removed backreach because of it comes from a line higher line number");
                        continue;
                    }
                    x++;
                    if (!alreadyReachedNodeRec(treeNode, backreachNode.id())) {
                        resolveBackreach(treeNode, backreachNode, new VarName(variableName, i++), isInCall, x);
                    } else {
                        new DFATreeNode(treeNode, "skip :" + backreachNode.id(), new NodeConnected(backreachNode.id()));
                    }
                }
            }
        } else if (Util.isAnyCall(treeNode.getObj()) || Util.isType(treeNode.getObj(), ASTNodeTypes.CAST)) {
            INode callNode = treeNode.getObj();
            int y = 0;
            boolean alreadyPassthrough = false;
            for (PassthroughPattern passthroughPattern : patternReader.getPassthroughs()) {
                if(alreadyPassthrough){
                    break;
                }
                if (passthroughPattern.equalsPattern(callNode, db)) {
                    treeNode.setIsPassthrough(true);
                    Pattern pattern = (Pattern) passthroughPattern;
                    List<INode> inputNodes = pattern.findNode(dsl, callNode, "%input");
                    for(INode inputNode : inputNodes){
                        DFATreeNode input = new DFATreeNode(treeNode, "passthrough " + y++ + " " + pattern.getName(), inputNode);
                        resolveExpression(input, isInCall);
                        alreadyPassthrough = true;
                    }
                }
            }

            INode functionDef = dsl.getFunctionFromCall(callNode);

            if (functionDef != null) {
                SourceLocation functionDefLocation = Util.codeLocation(db, functionDef);
                if (!isInCall.contains(functionDefLocation)) {
                    int i = 0;
                    for (INode returnNode : dsl.getAllReturnNodes(functionDef)) {
                        // no connection -> create subtree independent!
                        DFATreeNode returnDFA = new DFATreeNode(returnNode);
                        INode expressionNode = dsl.getExpressionFromReturn(returnNode);
                        if (expressionNode != null) {
                            DFATreeNode expression = new DFATreeNode(returnDFA, "return-to-exp", expressionNode);

                            // prevent recursive function calls to analyze in loop
                            isInCall.add(functionDefLocation);
                            resolveExpression(expression, isInCall);
                            isInCall.remove(functionDefLocation);
                        }

                        // sub tree is constructed -> connect parameter leafs to the relevant expressions
                        treeNode.addChild("function return " + i++, returnDFA);
                        for (LabeledTreeNode<INode> leafLabel : returnDFA.getAllLeafs()) {
                            DFATreeNode leaf = (DFATreeNode) leafLabel;
                            if (Util.isType(leaf.getObj(), ASTNodeTypes.PARAM)) {
                                int parameterIndex = leaf.getObj().getInt("childnum");
                                INode callParameterExp = dsl.getExpressionFromCall(callNode, parameterIndex);
                                DFATreeNode paramToExp = new DFATreeNode(leaf, "param to exp", callParameterExp);
                                resolveExpression(paramToExp, isInCall);
                            } else if (Util.isType(leaf.getObj(), ASTNodeTypes.CALL)) {
                                String callName = dsl.getCallName(leaf.getObj(), false);
                                if (callName != null && "func_get_arg".equals(callName.toLowerCase())) {
                                    //TODO: hardcoded for PHP change it to use json
                                    INode parameter = dsl.getCallParameter(leaf.getObj(), 0);
                                    if (Util.isType(parameter, ASTNodeTypes.INTEGER)) {
                                        int parameterIndex = parameter.getInt("code");
                                        if (parameterIndex >= 0) {
                                            INode callParameterExp = dsl.getExpressionFromCall(callNode, parameterIndex);
                                            DFATreeNode paramToExp = new DFATreeNode(leaf, "param to exp", callParameterExp);
                                            resolveExpression(paramToExp, isInCall);
                                        }
                                    }
                                }
                            }

                        }

                    }
                } else {
                    new DFATreeNode(treeNode, "SKIPPED", new NodeConnected(callNode.id()));
                }

            }
        } else if (Util.isType(treeNode.getObj(), ASTNodeTypes.DIM)) {
            INode varNode = dsl.child(treeNode.getObj(), 0);
            DFATreeNode dimNode = new DFATreeNode(treeNode, "dim", varNode);
            backtrackLeafNodeRec(dimNode, isInCall);
        }
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

            if (node == null) {
                message += "object is null";
            } else {
                message += node + " id:" + node.id() + " type: " + node.get("type");
            }
            throw new UnsupportedOperationException(message);
        }
    }

    private List<DFATreeNode> resolveControlInBetween(DFATreeNode fromNode, INode toNode, VarName variableName) throws TimeoutException {
        if(isPresearch){
//            return fromNode.getAllLeafs_();
            List<DFATreeNode> retval = new LinkedList<>();
            retval.add(fromNode);
            return retval;
        }
        INode fromNodeStatement = dsl.getStatement(fromNode.getObj());
        INode toNodeStatement = dsl.getStatement(toNode);
        List<INode> controlStatements = List.of();
        
        try {
            controlStatements = dsl.getControlStatements(toNodeStatement, fromNodeStatement, variableName.getVarName()); // parameters switched because we are doing a backwards analyzis
//            JUST FOR DEBUGGING/TESTING timeout
//            if(true){
//                throw new TimeoutException();
//            }
        } catch (TimeoutException ex) {
            SourceLocation fromLoc = Util.codeLocation(db, fromNodeStatement);
            SourceLocation toLoc = Util.codeLocation(db, toNodeStatement);
            
            System.out.println("TIMEOUT in control from: ");
            System.out.println("FROM: " + fromLoc.toString());
            System.out.println("TO: " + toLoc.toString());
            
            fromNode.addChild("TIMEOUT", new DFATreeNode(new TimeoutNode(fromLoc, toLoc)));
            return fromNode.getAllLeafs_();
        }

        DFATreeNode condition = new DFATreeNode(new ConditionNode(true));
        boolean hasConditions = false;
        for (INode controlStatement : controlStatements) {
            if (dsl.isIfStatementExpression(controlStatement) && dsl.ifStatementContainsStatement(controlStatement, fromNode)) {
                boolean hasConditionToBeTrue = dsl.ifElementContainsStatement(controlStatement, fromNode);
                hasConditions = true;
                conditionOnEachLeaf(condition, controlStatement, variableName, hasConditionToBeTrue);
            }
        }

        if (hasConditions) {
            fromNode.addChild("condition " + variableName.getVarNameWithIndex(), condition);
            return condition.getAllLeafs_();
        } else {
            // nothing changed
            List<DFATreeNode> retval = new LinkedList<>();
            retval.add(fromNode);
            return retval;
        }
    }

    private void conditionOnEachLeaf(DFATreeNode tree, INode condition, VarName varName, boolean conditionToBeTrue) throws TimeoutException {
        List<DFATreeNode> leafs = tree.getAllLeafs_();
        int i = 0;
        for (DFATreeNode leaf : leafs) {
            DFATreeNode conditionNode = new DFATreeNode(condition);
            leaf.addChild("condition " + i, conditionNode);
            conditionTreeRec(conditionNode, varName, conditionToBeTrue);
        }
    }

    private void conditionTreeRec(DFATreeNode node, VarName varName, boolean conditionToBeTrue) throws TimeoutException {
        if (Util.isType(node.getObj(), ASTNodeTypes.UNARY_OP)) {
            INode child = dsl.child(node.getObj(), 0);
            DFATreeNode childNode = new DFATreeNode(node, "unary_op", child);
            childNode.setConditionRequiresTrue(conditionToBeTrue);
            conditionTreeRec(childNode, varName, conditionToBeTrue);
        } else if (Util.isType(node.getObj(), ASTNodeTypes.BINARY_OP)) {
            INode child0 = dsl.child(node.getObj(), 0);
            INode child1 = dsl.child(node.getObj(), 1);
            if (node.getObj().getFlags().contains("BINARY_BITWISE_OR") || node.getObj().getFlags().contains("BINARY_BOOL_OR")) {
                if (dsl.containsVariable(child0, varName.getVarName())) {
                    DFATreeNode childNode0 = new DFATreeNode(node, "<OR_0>", child0);
                    childNode0.setConditionRequiresTrue(conditionToBeTrue);
                    conditionTreeRec(childNode0, varName, conditionToBeTrue);
                }
                if (dsl.containsVariable(child1, varName.getVarName())) {
                    DFATreeNode childNode1 = new DFATreeNode(node, "<OR_1>", child0);
                    childNode1.setConditionRequiresTrue(conditionToBeTrue);
                    conditionTreeRec(childNode1, varName, conditionToBeTrue);
                }
            } else if (node.getObj().getFlags().contains("BINARY_BITWISE_AND") || node.getObj().getFlags().contains("BINARY_BOOL_AND")) {
                boolean recFurther = false;
                if (dsl.containsVariable(child0, varName.getVarName())) {
                    node = new DFATreeNode(node, "<AND_0>", child0);
                    node.setConditionRequiresTrue(conditionToBeTrue);
                    recFurther = true;
                }
                if (dsl.containsVariable(child1, varName.getVarName())) {
                    node = new DFATreeNode(node, "<AND_1>", child0);
                    node.setConditionRequiresTrue(conditionToBeTrue);
                    recFurther = true;
                }

                if (recFurther) {
                    conditionTreeRec(node, varName, conditionToBeTrue);
                }
            }
        }
    }

    private void resolveBackreach(DFATreeNode fromNode, INode statement, VarName variableName, Set<SourceLocation> isInCall, int backNodeIndex) throws ResultTreeToLarge, TimeoutException {
//        System.out.println("Backreach: var(" + variableName + ") " + fromNode.getObj() + " " + Util.codeLocation(db, fromNode.getObj()).toString());

        if (Util.isType(statement, ASTNodeTypes.ASSIGN) || Util.isType(statement, ASTNodeTypes.ASSIGN_OP)) {
            LinkedList<INode> listToVar = getNodesToVar(statement);
            int x = 0;
            for (INode varNode : listToVar) {
                x++;

                for (DFATreeNode node : resolveControlInBetween(fromNode, varNode, variableName)) {
                    DFATreeNode varTreeNode = new DFATreeNode(varNode);
                    if (!node.addChild("assigns to" + backNodeIndex + ":" + x, varTreeNode)) {
                        continue;
                    }

                    DFATreeNode tempNode = varTreeNode;
                    tempNode.setIsAssigned(true);
                    for (int i = 1; i < listToVar.size(); i++) {
                        tempNode = new DFATreeNode(tempNode, "getToVar" + i, listToVar.get(i));
                        tempNode.setIsAssigned(true);
                    }

                    DFATreeNode assignment = new DFATreeNode(tempNode, "assign:" + variableName.getVarNameWithIndex(), statement);

                    INode expression = dsl.getExpressionFromAssignment(statement);
                    DFATreeNode expressionTreeNode = new DFATreeNode(assignment, variableName.getVarNameWithIndex(), expression);

                    resolveExpression(expressionTreeNode, isInCall);

                    if (Util.isType(statement, ASTNodeTypes.ASSIGN_OP)) {
                        DFATreeNode varOpNode = new DFATreeNode(assignment, "assing_op", varNode);
                        resolveExpression(varOpNode, isInCall);
                    }
                }
            }

        } else if (Util.isType(statement, ASTNodeTypes.PARAM)) {
            INode parameterNode = statement;
            DFATreeNode paramaterDFA = new DFATreeNode(fromNode, "param", parameterNode);

            if (isInCall.isEmpty()) {
                // we have to check all calls to get all sources
                List<INode> callNodes = dsl.getFunctionCallsFromParam(parameterNode);
//                System.out.println("Reaching out to: " + callNodes.size());
                int i = 0;
                for (INode callNode : callNodes) {
                    // param ----> call ----> expression(parameter)
                    DFATreeNode callDFA = new DFATreeNode(paramaterDFA, "call" + i++, callNode);

                    INode expression = dsl.getExpressionFromCall(callNode, parameterNode.getInt("childnum"));
                    DFATreeNode expressionDFA = new DFATreeNode(callDFA, "param", expression);
                    resolveExpression(expressionDFA, isInCall);
                }
            }

        }
    }

    private boolean alreadyReachedNodeRec(LabeledTreeNode<INode> node, long id) {
        if (node.getObj().id() == id) {
            return true;
        }
        if (node.getParent() == null) {
            return false;
        }

        return alreadyReachedNodeRec(node.getParent(), id);
    }

    public boolean isPip() {
        return isPip;
    }

    public DFATreeNode getResultTree() {
        return resultTree;
    }
    
    private void startDataflowAnalysis(boolean preSearch){
//        this.isPresearch = true;
        this.isPresearch = preSearch;
        
        resultTree = new DFATreeNode(sinkNode);
        resultTree.setSinkPattern(pattern);
        
        try {
            List<INode> inputNodes = pattern.findNode(dsl, sinkNode, "%input");
            int i = 0;
            for(INode inputNode : inputNodes){
                DFATreeNode child = new DFATreeNode(inputNode);
                resultTree.addChild("sink" + i++, child);
                resolveExpression(child, new HashSet<>());                
            }
            if (!getSourceNodes(resultTree, patternReader, db).isEmpty()) {
                System.out.println("Found a pip!");
                isPip = true;
            }
//                        System.out.println("Finished.");
        } catch (ResultTreeToLarge ex) {
            System.out.println("Results too large...");
        } catch (TimeoutException ex) {
            ex.printErrorMessage("Unexpected timeout exception");
        }
    }

    @Override
    public void run() {
        startDataflowAnalysis(true);
        
        if(isPip && controlFlowCheck){
            System.out.println("Do a more specific search!");
            startDataflowAnalysis(false);
        }
    }
}
