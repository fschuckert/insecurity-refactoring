/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 *
 * @author blubbomat
 */
public class TreeNode<E> {
    private ArrayList<TreeNode<E>> children = new ArrayList<>();
    
    private TreeNode<E> parent = null;
    
    private E obj;
    
    private String special;

    public String getSpecial() {
        return special;
    }

    public void setSpecial(String special) {
        this.special = special;
    }
    
    

    public TreeNode(E obj) {
        this.obj = obj;
    }
    
    public boolean replaceChild(TreeNode<E> newChild, int childNum){
        if(childNum >= 0 && childNum < children.size()){
            TreeNode<E> child = children.get(childNum);            
            child.setParent(null);
            
            children.set(childNum, newChild);
            newChild.setParent(this);
            return true;
        }
        return false;
    }
    
    public boolean replaceChildWithList(TreeNode<E> toReplace, List<TreeNode<E>> replacements){
        boolean foundChild = false;
        ArrayList<TreeNode<E>> childrenOld = children;
        children = new ArrayList<>();
        
        for(TreeNode<E> child : childrenOld){
            if(child.equals(toReplace)){
                toReplace.setParent(null);
                foundChild = true;
                children.addAll(replacements);
                for(TreeNode<E> replacement : replacements){
                    replacement.setParent(this);
                }
            }
            else {
                children.add(child);
            }
        }
        
        return foundChild;
    }
    
    public boolean removeChild(int childNum){
        if(childNum >= 0 && childNum < children.size()){
            TreeNode<E> child = children.get(childNum);
            children.remove(childNum);
            child.setParent(null);
            return true;
        }
        return false;
    }
    
    
    public void addChild(TreeNode<E> child){
        if(children.add(child)){
            child.setParent(this);
        }
    }     
    
    public void addAll(Collection<TreeNode<E>> children){
        for(TreeNode<E> child: children){
            addChild(child);
        }
    }
    
    private void setParent(TreeNode<E> parent){
        this.parent = parent;
    }
    
    public ArrayList<TreeNode<E>> getChildren(){
        return children;
    }
    
    public TreeNode<E> getChild(int i){
        return children.get(i);
    }

    public TreeNode<E> getParent() {
        return parent;
    }

    public E getObj() {
        return obj;
    }

    public void setObj(E obj) {
        this.obj = obj;
    }

    @Override
    public String toString() {
        return "T{"  + obj + "} c:" + children.size();
    }
    
    private boolean findPathToRec(LinkedList<Integer> path, int index, E toFind){
        if(toFind.equals(obj)){
            path.add(index);
            return true;
        }
        
        int i = 0;
        for(TreeNode<E> child : getChildren()){
            if(child.findPathToRec(path, i, toFind)){
                path.add(index);
                return true;
            }            
            i ++;            
        }
        
        return false;
    }
    
    public List<Integer> getPathToObj(E toFind){
        if(toFind == null){
            return null;
        }
        
        LinkedList<Integer> path = new LinkedList<>();
        findPathToRec(path, -1, toFind);
        if(path.isEmpty()){
            return null;
        }
        
        path.removeLast();
        Collections.reverse(path);
        
        return path;
    }
    
    
    
}
