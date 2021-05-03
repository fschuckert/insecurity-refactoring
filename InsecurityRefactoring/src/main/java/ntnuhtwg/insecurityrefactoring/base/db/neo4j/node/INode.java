/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.db.neo4j.node;

import java.util.List;
import java.util.Map;
import org.neo4j.driver.Value;

/**
 *
 * @author blubbomat
 */
public interface INode {

    public Long id();
    
    public Object get(String property);
    public String getString(String property);
    public int getInt(String property);
    
    public boolean isNullNode();

    public boolean containsKey(String type);

    public Map<String, Object> asMap();
    
    public List<String> getFlags();

    public boolean isModified();
    public void markModified();
    
    public void setProperty(String property, Object value);
    
    
}
