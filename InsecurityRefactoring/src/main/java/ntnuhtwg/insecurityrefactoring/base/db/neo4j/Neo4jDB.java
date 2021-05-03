/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.db.neo4j;

import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.SourceLocation;
import ntnuhtwg.insecurityrefactoring.base.exception.TimeoutException;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import org.neo4j.driver.types.Node;

/**
 *
 * @author blubbomat
 */
public interface Neo4jDB {
    
    public INode findFirstNode(String query, Object... keysAndValues) throws TimeoutException;
    public List<INode> findAll(String query, Object... keysAndValues) throws TimeoutException;
    
    public INode findNode(Long id) throws TimeoutException;
    

    public void close() throws Exception;
    
}
