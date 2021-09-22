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
import ntnuhtwg.insecurityrefactoring.base.context.CharsAllowed;
import ntnuhtwg.insecurityrefactoring.base.context.enclosure.Apostrophe;
import ntnuhtwg.insecurityrefactoring.base.context.enclosure.Enclosure;
import ntnuhtwg.insecurityrefactoring.base.exception.TimeoutException;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
import ntnuhtwg.insecurityrefactoring.base.tree.TreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher.DataflowDSL;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.patterns.PassthroughPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;

/**
 *
 * @author blubbomat
 */
public class SanitizePattern extends Pattern implements PassthroughPattern{
    
    private boolean passthrough;
    
    private final DataType dataInput;
    private final DataType dataOutput;
    private final boolean noDetection;
    
    private final CharsAllowed charsAllowed;
    private Enclosure addsEnclosure;

    public SanitizePattern(boolean passthrough, DataType dataInput, DataType dataOutput, CharsAllowed charsAllowed, boolean noDetection) {
        this.passthrough = passthrough;
        this.dataInput = dataInput;
        this.dataOutput = dataOutput;
        this.charsAllowed = charsAllowed;
        this.noDetection = noDetection;
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

    public CharsAllowed getCharsAllowed() {
        return charsAllowed;
    }
    
    
    
    public boolean isSanitizeNode(DataflowDSL dsl, DFATreeNode node) throws TimeoutException{
        if(this.equalsPattern(node.getObj(), dsl.getDb())){
            return true;
        }
        return false;
    }

    public boolean isNoDetection() {
        return noDetection;
    }
    
    

    @Override
    public String toString() {
        return getName();
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

    public void setAddsEnclosing(Enclosure enclosure) {
        this.addsEnclosure = enclosure;
    }

    public Enclosure getAddsEnclosure() {
        return addsEnclosure;
    }
    
    
    
}
