/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.ast;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;

/**
 *
 * @author blubbomat
 */
public class BaseNode implements INode{
    
    protected Map<String, Object> properties = new HashMap<>();
    protected List<String> flags = new LinkedList<>();
    private boolean isModified = false;

    @Override
    public Long id() {
        return -1l;
    }

    @Override
    public Object get(String property) {
        return properties.get(property);
    }

    @Override
    public String getString(String property) {
        return String.valueOf(get(property));
    }

    @Override
    public int getInt(String property) {
        return Integer.valueOf(getString(property));
    }

    @Override
    public boolean isNullNode() {
        if(properties.containsKey("type")){
            return "NULL".equals(properties.get("type"));
        }
        return false;
    }

    @Override
    public boolean containsKey(String type) {
        return properties.containsKey(type);
    }

    @Override
    public Map<String, Object> asMap() {
        return properties;
    }

    @Override
    public List<String> getFlags() {
        return flags;
    }
    
    public void addFlag(String flag){
        flags.add(flag);
    }
    
    public BaseNode addProperty(String property, Object value){
        properties.put(property, value);
        return this;
    }

    @Override
    public String toString() {
        return (isModified() ? "(M) " : "") + "BaseNode{" + "properties=" + properties + ", flags=" + flags + '}';
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
        addProperty(property, value);
    }
    
    
}
