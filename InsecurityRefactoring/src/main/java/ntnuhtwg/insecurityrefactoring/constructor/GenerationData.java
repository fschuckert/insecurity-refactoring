/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.constructor;

import java.util.LinkedList;
import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.tree.TreeNode;

/**
 *
 * @author blubbomat
 */
public class GenerationData {
    final List<TreeNode<INode>> ast;
    
    final DataType outputDatatype;
    final TreeNode<INode> outputNode;

    public GenerationData(List<TreeNode<INode>> ast, DataType outputDatatype, TreeNode<INode> outputNode) {
        this.ast = ast;
        this.outputDatatype = outputDatatype;
        this.outputNode = outputNode;
    }
    
}
