/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.db.neo4j.node;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import org.neo4j.driver.Value;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.internal.value.NullValue;
import org.neo4j.driver.types.Node;
import scala.NotImplementedError;

/**
 *
 * @author blubbomat
 */
public class NodeConnected implements INode{
    private final Node n;
    private boolean isModified = false;
    private int maxlineno = -1;

    public NodeConnected(Node n) {
        this.n = n;
    }
    
    public NodeConnected(long id) {
        this.n = new InternalNode(id);
    }

    @Override
    public Long id() {
        return n.id();
    }
    
    @Override
    public Object get(String property) {
        return n.get(property).asObject();
    }
    
    @Override
    public String getString(String property) {
        return n.get(property).asString();
    }

    @Override
    public int getInt(String property) {
        if("maxlineno".equals(property)){
            return maxlineno;
        }
        
        Value value = n.get(property);
        String name = value.type().name();
        
        if("INTEGER".equals(name)){
            return value.asInt();
        }
        else if("STRING".equals(name)){
            String str = n.get(property).asString();
            return Integer.valueOf(str);
        }
        else{
            return -1;
        }
        
    }
    
    @Override
    public boolean containsKey(String type) {
        if("maxlineno".equals(type)){
            return maxlineno >= 0;
        }
        
        return n.containsKey(type);
    }

    @Override
    public Map<String, Object> asMap() {
        return n.asMap();
    }

    @Override
    public String toString() {
        return (isModified() ? "(M) " : "") +  "NodeC{" + id() + '}' + (containsKey("type")? get("type") : "");
    }

    @Override
    public boolean isNullNode() {
        return "NULL".equals(getString("type"));
    }

    @Override
    public List<String> getFlags() {
        List<String> retval = new LinkedList<>();
        Value flags = n.get("flags");
        if(flags != null && !flags.isNull()){
            for(Object flag : flags.asList()){
                retval.add((String)flag);
            }
        }
        return retval;
    }

    @Override
    public boolean isModified() {
        return isModified;
    }

    @Override
    public void markModified() {
        this.isModified = true;
    }

    @Override
    public void setProperty(String property, Object value) {
        if("maxlineno".equals(property)){
            maxlineno = (int)value;
            return;
        }
        
        throw new NotImplementedError("cannot modify a connected node " + property);
    }

    

    

    
}
