/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.patterns.impl;

import java.util.LinkedList;
import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.ast.FixedNode;
import ntnuhtwg.insecurityrefactoring.base.exception.GenerateException;
import ntnuhtwg.insecurityrefactoring.base.tree.TreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher.DataflowDSL;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4JConnector;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4jDB;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.sufficient.Sufficient;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.sufficient.GenerateParameters;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.sufficient.VulnSufficient;
import org.neo4j.driver.types.Node;

/**
 *
 * @author blubbomat
 */
public class SourcePattern extends Pattern implements Sufficient{
    private List<VulnSufficient> sufficients;
    
    DataType dataOutput;
    
    private GenerateParameters sufficientParamaters;
    
    private boolean secure;

//    public boolean isNode(INode obj, Neo4jDB db) {
//        
//        
//        INode node = astTree.get(0).getObj();
//    }
    
    
//    public abstract boolean isNode(INode node, Neo4jDB db);
//    
//    public abstract List<INode> outputNodes(INode patternNode, DataflowDSL dsl);
//    
//    public abstract boolean requiresSourceExpr();
//    
//    public abstract DataType outputType();
//    
//    public abstract TreeNode<INode> generateAST(TreeNode<INode> sourceExprOptional, TreeNode<INode> paramExp)  throws GenerateException;

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public SourcePattern(DataType dataOutput) {
        this.dataOutput = dataOutput;
    }

    public DataType getDataOutput() {
        return dataOutput;
    }

    @Override
    public List<VulnSufficient> getSufficients() {
        return sufficients;        
    }

    @Override
    public void setSufficients(List<VulnSufficient> sufficients) {
        this.sufficients = sufficients;
    }




    
    
    
}
