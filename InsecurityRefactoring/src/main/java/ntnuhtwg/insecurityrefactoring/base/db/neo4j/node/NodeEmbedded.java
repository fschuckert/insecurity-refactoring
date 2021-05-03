/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.db.neo4j.node;

import java.util.List;
import java.util.Map;
import org.neo4j.driver.Value;
import org.neo4j.graphdb.Node;

/**
 *
 * @author blubbomat
 */
public class NodeEmbedded implements INode{
    
//    private final Node n;
    Map<String, Object> properties;
    long id;
    private boolean isModified = false;

    public NodeEmbedded(Node n) {
        id = n.getId();
        properties = n.getAllProperties();
    }

    @Override
    public Long id() {
        return id;
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
        String str = getString(property);
        return Integer.valueOf(str);
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
    public String toString() {
        return "NodeEmbedded{" + "id=" + id + '}';
    }

    @Override
    public boolean isNullNode() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<String> getFlags() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
    

    
    
}
