/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.refactor.acid.rules;

import java.util.LinkedList;
import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.ASTNodeTypes;
import ntnuhtwg.insecurityrefactoring.base.SourceLocation;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.ast.ConditionNode;
import ntnuhtwg.insecurityrefactoring.base.ast.TimeoutNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher.DataflowDSL;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.exception.TimeoutException;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
import ntnuhtwg.insecurityrefactoring.refactor.base.VarName;

/**
 *
 * @author blubbomat
 */
public class ACIDTreeConditionsRules {
    
    public static List<DFATreeNode> resolveControlInBetween(DFATreeNode fromNode, INode toNode, String variableName, DataflowDSL dsl) throws TimeoutException {
        INode fromNodeStatement = dsl.getStatement(fromNode.getObj());
        INode toNodeStatement = dsl.getStatement(toNode);
        List<INode> controlStatements = List.of();
        
        try {
            controlStatements = dsl.getControlStatements(toNodeStatement, fromNodeStatement, variableName); // parameters switched because we are doing a backwards analyzis
//            JUST FOR DEBUGGING/TESTING timeout
//            if(true){
//                throw new TimeoutException();
//            }
        } catch (TimeoutException ex) {
            SourceLocation fromLoc = Util.codeLocation(dsl.getDb(), fromNodeStatement);
            SourceLocation toLoc = Util.codeLocation(dsl.getDb(), toNodeStatement);
            
            System.out.println("TIMEOUT in control from: ");
            System.out.println("FROM: " + fromLoc.toString());
            System.out.println("TO: " + toLoc.toString());
            
            fromNode.addChild("TIMEOUT", new DFATreeNode(new TimeoutNode(fromLoc, toLoc)));
            return fromNode.getAllLeafs_();
        }

        DFATreeNode condition = new DFATreeNode(new ConditionNode(true));
        condition.setIsCondition(true);
        boolean hasConditions = false;
        for (INode controlStatement : controlStatements) {
            if (dsl.isIfStatementExpression(controlStatement) && dsl.ifStatementContainsStatement(controlStatement, fromNode)) {
                boolean hasConditionToBeTrue = dsl.ifElementContainsStatement(controlStatement, fromNode);
                hasConditions = true;
                conditionOnEachLeaf(condition, controlStatement, variableName, hasConditionToBeTrue, dsl);
            }
        }

        if (hasConditions) {
            fromNode.addChild("condition " + variableName, condition);
            return condition.getAllLeafs_();
        } else {
            // nothing changed
            List<DFATreeNode> retval = new LinkedList<>();
            retval.add(fromNode);
            return retval;
        }
    }

    private static void conditionOnEachLeaf(DFATreeNode tree, INode condition, String varName, boolean conditionToBeTrue, DataflowDSL dsl) throws TimeoutException {
        List<DFATreeNode> leafs = tree.getAllLeafs_();
        int i = 0;
        for (DFATreeNode leaf : leafs) {
            DFATreeNode conditionNode = new DFATreeNode(condition);
            leaf.addChild("condition " + i, conditionNode);
            conditionTreeRec(conditionNode, varName, conditionToBeTrue, dsl);
        }
    }

    private static void conditionTreeRec(DFATreeNode node, String varName, boolean conditionToBeTrue, DataflowDSL dsl) throws TimeoutException {
        if (Util.isType(node.getObj(), ASTNodeTypes.UNARY_OP)) {
            INode child = dsl.child(node.getObj(), 0);
            DFATreeNode childNode = new DFATreeNode(node, "unary_op", child);
            childNode.setConditionRequiresTrue(conditionToBeTrue);
            conditionTreeRec(childNode, varName, conditionToBeTrue, dsl);
        } else if (Util.isType(node.getObj(), ASTNodeTypes.BINARY_OP)) {
            INode child0 = dsl.child(node.getObj(), 0);
            INode child1 = dsl.child(node.getObj(), 1);
            if (node.getObj().getFlags().contains("BINARY_BITWISE_OR") || node.getObj().getFlags().contains("BINARY_BOOL_OR")) {
                if (dsl.containsVariable(child0, varName)) {
                    DFATreeNode childNode0 = new DFATreeNode(node, "<OR_0>", child0);
                    childNode0.setConditionRequiresTrue(conditionToBeTrue);
                    conditionTreeRec(childNode0, varName, conditionToBeTrue, dsl);
                }
                if (dsl.containsVariable(child1, varName)) {
                    DFATreeNode childNode1 = new DFATreeNode(node, "<OR_1>", child0);
                    childNode1.setConditionRequiresTrue(conditionToBeTrue);
                    conditionTreeRec(childNode1, varName, conditionToBeTrue, dsl);
                }
            } else if (node.getObj().getFlags().contains("BINARY_BITWISE_AND") || node.getObj().getFlags().contains("BINARY_BOOL_AND")) {
                boolean recFurther = false;
                if (dsl.containsVariable(child0, varName)) {
                    node = new DFATreeNode(node, "<AND_0>", child0);
                    node.setConditionRequiresTrue(conditionToBeTrue);
                    recFurther = true;
                }
                if (dsl.containsVariable(child1, varName)) {
                    node = new DFATreeNode(node, "<AND_1>", child0);
                    node.setConditionRequiresTrue(conditionToBeTrue);
                    recFurther = true;
                }

                if (recFurther) {
                    conditionTreeRec(node, varName, conditionToBeTrue, dsl);
                }
            }
        }
    }
}
