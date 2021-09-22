/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.ast;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;

/**
 *
 * @author blubbomat
 */
public class FixedNode implements INode{
    
    private String str;
    private boolean check = false;
    private boolean isModified = false;
    private boolean isDots = false;

    public FixedNode(String str) {
        this.str = str;
    }
    
    public FixedNode(String str, boolean check) {
        this.str = str;
        this.check = check;
    }

    public String getFixedValue() {
        return str;
    }

    public boolean isCheck() {
        return check;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.str);
        hash = 29 * hash + (this.check ? 1 : 0);
        hash = 29 * hash + (this.isModified ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
     
        if(obj instanceof INode){
            INode node = (INode)obj;
            if("here".equals(str) && "here".equals(node.get("code"))){
                return true;
            }
        }
        
        return false;        
    }

    
    
    
    

    @Override
    public Long id() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object get(String property) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getString(String property) {
        System.out.println("Trying to get a property: " + property + " for: " + toString());
        throw new UnsupportedOperationException("Trying to get a property: " + property + " for: " + toString()); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getInt(String property) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isNullNode() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean containsKey(String type) {
        if("...".equals(type)){
            return isDots;
        }
        return false;
    }
    
    public boolean isDots(){
        return isDots;
    }

    @Override
    public Map<String, Object> asMap() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<String> getFlags() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString() {
        return (isModified() ? "(M) " : "") +"FixedNode{" + "str=" + str + '}';
    }

    @Override
    public boolean isModified() {
        return isModified;
    }

    @Override
    public void markModified() {
        isModified = true;
    }

    @Override
    public void setProperty(String property, Object value) {
        if("...".equals(property)){
            isDots = true;
        }
        else{
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
    
    
}
