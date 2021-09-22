/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.tree;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import ntnuhtwg.insecurityrefactoring.base.ASTNodeTypes;
import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.SourceLocation;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.ast.BaseNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.DataflowPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SanitizePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SinkPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SourcePattern;

/**
 *
 * @author blubbomat
 */
public class DFATreeNode extends LabeledTreeNode<INode>{
    
    
    private DataType inputType = DataType.Unknown();
    private DataType outputType = DataType.Unknown();
    
    private boolean isConcat = false;    
//    private boolean isExcluding = false; => just isExluciding = !isConcat
    
    
    private boolean isAssigned = false;
    
    private boolean isPassthrough = false;
    private boolean condition = false;
    
    private SinkPattern sinkPattern = null;
    
    private SanitizePattern sanitizePattern;
    
    private SourceLocation sourceLocation;
    
    private List<DataflowPattern> possibleDataflowReplacements = new LinkedList<>();
    
    private SourcePattern sourcePattern;
    
    private Stack<DFATreeNode> callStack = new Stack<>();
    
    // null -> not a condition node, 
    // true -> the conditions has to be true, 
    //false -> the condition has to be false
    Boolean conditionRequiresTrue = null; 
    
    public DFATreeNode(INode obj) {
        super(obj);
    }
    
    public DFATreeNode(DFATreeNode parent, String connection , INode obj) {
        super(obj);
        parent.addChild(connection, this);
        this.callStack = parent.getCallStack();        
    }
     
    public DFATreeNode getParent_(){
        return getParent() != null ? (DFATreeNode)getParent() : null;
    }

    public SinkPattern getSinkPattern() {
        return sinkPattern;
    }

    public void setSinkPattern(SinkPattern sinkPattern) {
        this.sinkPattern = sinkPattern;
    }

    protected Stack<DFATreeNode> getCallStack() {
        return callStack;
    }
    

    
        
    
    
    public void pushCallStack(DFATreeNode functionName){
        this.callStack = (Stack<DFATreeNode>)callStack.clone();
        callStack.push(functionName);
    }
    
    public DFATreeNode popCallStack(){
        this.callStack = (Stack<DFATreeNode>)callStack.clone();
        
        if(callStack.isEmpty()){
            return null;
        }
        return callStack.pop();
    }
    
    public DFATreeNode peekCall(){
        if(callStack.isEmpty()){
            return null;
        }
        return callStack.peek();
    }

    public boolean isConcat() {
        return isConcat;
    }

    public void setIsConcat(boolean isConcat) {
        this.isConcat = isConcat;
    }

    public boolean isIsExcluding() {
        return !isConcat;
    }

 
   
    
    
    
    @Override
    public String toString() {
        if(getObj() == null){
            return "" + null;
        }
        
        String retval = "";
        if(sinkPattern != null){
            retval += "(" + sinkPattern.getName() + ") ";
        }
               
        
        if(sourceLocation != null){
            retval += sourceLocation.toString();
            return retval;
        }
        else{
            return "(" + getObj().id() + ")" + getObj().get("type")  + " l:" + getObj().get("lineno");
        }
    }

    public DataType getInputType() {
        return inputType;
    }

    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    public void setSourceLocation(SourceLocation sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    public SourcePattern getSourcePattern() {
        return sourcePattern;
    }

    public void setSourcePattern(SourcePattern sourcePattern) {
        this.sourcePattern = sourcePattern;
    }

    public Boolean getConditionRequiresTrue() {
        return conditionRequiresTrue;
    }

    public void setConditionRequiresTrue(Boolean conditionRequiresTrue) {
        this.conditionRequiresTrue = conditionRequiresTrue;
        setConditionRequiresTrue(conditionRequiresTrue != null);
    }   
    
    

    public void setInputType(DataType inputType) {
        this.inputType = inputType;
    }

    public DataType getOutputType() {
        return outputType;
    }

    public void setOutputType(DataType outputType) {
        this.outputType = outputType;
    }

    public boolean isIsAssigned() {
        return isAssigned;
    }

    public void setIsAssigned(boolean isAssigned) {
        this.isAssigned = isAssigned;
    }

    public boolean isIsPassthrough() {
        return isPassthrough;
    }

    public void setIsPassthrough(boolean isPassthrough) {
        this.isPassthrough = isPassthrough;
    }

    public SanitizePattern getSanitizePattern() {
        return sanitizePattern;
    }

    public void setSanitizePattern(SanitizePattern sanitizePattern) {
        this.sanitizePattern = sanitizePattern;
    }

    public List<DataflowPattern> getPossibleDataflowReplacements() {
        return possibleDataflowReplacements;
    }
    
    
    public void addPossibleDataflowPattern(DataflowPattern dataflowPattern){
        this.possibleDataflowReplacements.add(dataflowPattern);
    }

    public void clearDataflowReplacements() {
        this.possibleDataflowReplacements.clear();
    }

    public boolean isConditionNode() {
        return condition;
    }

    public List<DFATreeNode> getAllLeafs_() {
        List<DFATreeNode> retval = new LinkedList<>();
        
        for(LabeledTreeNode<INode> leaf : getAllLeafs()){
            retval.add((DFATreeNode)leaf);
        }
        
        return retval;
    }

    public List<DFATreeNode> getChildren_() {
        List<DFATreeNode> retval = new LinkedList<>();
        
        for(LabeledTreeNode<INode> child : children){
            retval.add((DFATreeNode)child);
        }
        
        return retval;
    }
    
    public List<DFATreeNode> getChildrenBefore(DFATreeNode node){
        List<DFATreeNode> retval = new LinkedList<>();
        
        for(LabeledTreeNode<INode> child : children){
            if(node.equals(child)){
                break;
            }
            retval.add((DFATreeNode)child);
        }
        
        return retval;
    }
    
    public List<DFATreeNode> getChildrenAfter(DFATreeNode node){
        LinkedList<DFATreeNode> retval = new LinkedList<>();
        
        boolean found = false;
        
        for(LabeledTreeNode<INode> child : children){
            if(found){
                retval.add((DFATreeNode)child);
            }
            
            if(node.equals(child)){
                found = true;
            }
        }
        
        return retval;
    }
    
    public boolean isRecursiveNodeCheck(){
        if(EdgeNames.TO_VAR.equals(getLabel())){
            return false;
        }
        
        return true;
    }
    
    
    public DFATreeNode findFirstOfTypeDown(String type){
        if(getObj() != null){
            if(Util.isType(getObj(), type)){
                return this;
            }
        }
        
        for(DFATreeNode child : getChildren_()){
            DFATreeNode found = child.findFirstOfTypeDown(type);
            if(found != null){
                return found;
            }
        }
        
        return null;
    }
    
    
    public DFATreeNode findFirstOfType(String type){
        if(getObj() != null){
            if(Util.isType(getObj(), type)){
                return this;
            }
        }
        
        DFATreeNode parent = getParent_();
        if(parent != null){
            return parent.findFirstOfType(type);
        }
        
        return null;
    }

    

    public void setIsCondition(boolean b) {
        this.condition = b;
    }

  

    
    
}
