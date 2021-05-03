/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.tree;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.parboiled.common.StringUtils;

/**
 *
 * @author blubbomat
 */
public class LabeledTreeNode<E> {
    private E obj;
    protected LinkedList<LabeledTreeNode<E>> children = new LinkedList<>();
    protected LabeledTreeNode<E> parent;
    private String label;

    public LabeledTreeNode(E obj) {
        this.obj = obj;
    }
    
    public boolean addChild(String label, LabeledTreeNode<E> child){
        child.setParent(this, label);
        
        children.addLast(child);
        return true;
    }
    
    public E getObj() {
        return obj;
    }

    public void setObj(E obj) {
        this.obj = obj;
    }
    
    private void getAllLeafsRec(List<LabeledTreeNode<E>> retval){
        if(children.size() == 0){
            retval.add(this);
        }
        
        for(LabeledTreeNode<E> child : children){
            child.getAllLeafsRec(retval);
        }
    }
    
    public List<LabeledTreeNode<E>> getAllLeafs(){
        List<LabeledTreeNode<E>> retval = new LinkedList<>();
        getAllLeafsRec(retval);
        return retval;
    }
    
    public void prettyPrint(int indent, String label){        
        System.out.println(StringUtils.repeat(' ', indent) + "\\--" + label +  "--> " + obj);
        
        children.forEach((child) -> {
            child.prettyPrint(indent+1, child.getLabel());
        });
    }

    public LabeledTreeNode<E> getParent() {
        return parent;
    }

    protected void setParent(LabeledTreeNode<E> parent, String label) {
        this.parent = parent;
        this.label = label;
    }
    
    public int treeDepth(){
        if(getParent() == null){
            return 1;
        }
        
        return getParent().treeDepth() + 1;
    }
    
    public LabeledTreeNode<E> root(){
        if(getParent() == null){
            return this;
        }
        
        return getParent().root();
    }
    
    public int getWidth(){
        LabeledTreeNode<E> root = root();
        
        return root.getAllLeafs().size();
    }

    public String getLabel() {
        return label;
    }
    
    
    
    
    
    
}
