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
import ntnuhtwg.insecurityrefactoring.base.exception.TimeoutException;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
import ntnuhtwg.insecurityrefactoring.base.tree.TreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher.DataflowDSL;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.patterns.PassthroughPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.sufficient.Sufficient;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.sufficient.GenerateParameters;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.sufficient.VulnSufficient;

/**
 *
 * @author blubbomat
 */
public class SanitizePattern extends Pattern implements PassthroughPattern, Sufficient{
    
    private boolean passthrough;
    
    private List<VulnSufficient> sufficients;
    
    private final DataType dataInput;
    private final DataType dataOutput;
    
    private List<GenerateParameters> sufficientBasedOnParams = new LinkedList<>();

    public SanitizePattern(boolean passthrough, DataType dataInput, DataType dataOutput, List<VulnSufficient> sufficients) {
        this.passthrough = passthrough;
        this.dataInput = dataInput;
        this.dataOutput = dataOutput;
        this.sufficients = sufficients;
    }
    
    @Override
    public boolean isPassthrough() {
        return passthrough;
    }

    @Override
    public DataType getDataInputType() {
        return dataInput;
    }

    @Override
    public DataType getDataOutputType() {
        return dataOutput;
    }
    
    public boolean isSanitizeNode(DataflowDSL dsl, DFATreeNode node) throws TimeoutException{
        if(this.equalsPattern(node.getObj(), dsl.getDb())){
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public List<VulnSufficient> getSufficients() {
        return sufficients;
    }

    @Override
    public void setSufficients(List<VulnSufficient> sufficients) {
        this.sufficients = sufficients;
    }


    public boolean isReplaceableWith(SanitizePattern sanitizePattern){
        return sanitizePattern.getPatternType() == getPatternType() 
                && sanitizePattern.getDataInputType().equalsAny(getDataInputType())
                && sanitizePattern.getDataOutputType().equalsAny(getDataOutputType())
                && sanitizePattern.getOutputType() == getOutputType()
                && sanitizePattern.getInputType() == getInputType();
    }
    
    public boolean isReplaceableWithIgnoreDatatype(SanitizePattern sanitizePattern){
        return sanitizePattern.getPatternType() == getPatternType() 
                && sanitizePattern.getOutputType() == getOutputType()
                && sanitizePattern.getInputType() == getInputType();
    }   

    public boolean isCheckMethod() {
        return DataType.Boolean().equals(getDataOutputType());
    }
    
}
