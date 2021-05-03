/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.jshell.spi.ExecutionControl;
import ntnuhtwg.insecurityrefactoring.base.exception.TimeoutException;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4JConnector;
import org.neo4j.driver.internal.value.NodeValue;
import org.neo4j.driver.Record;
import org.neo4j.driver.Values;
import org.neo4j.driver.types.Node;

/**
 *
 * @author blubbomat
 */
public class InteractiveNode {
    private final Node node;
    private final Neo4JConnector connector;

    public InteractiveNode(Node node, Neo4JConnector connector) {
        this.node = node;
        this.connector = connector;
    }

    public Node getNode() {
        return node;
    }
    
    
    public InteractiveNode getParent(){
        List<Record> result;
        try {
            result = connector.runRead("MATCH (parent)-[:PARENT_OF]->(child) WHERE id(child)=$id return parent", Values.parameters("id", node.id()));
        } catch (TimeoutException ex) {
            Logger.getLogger(InteractiveNode.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        if(result.size() > 1){
            System.err.println("A node has multiple parents Child id: " + node.id() + " parents numbers: " + result.size() + " We are using the first result, but the data might be corrupt.");
        }
        
        if(result.size() == 0){
            return null;
        }
        
        Node parentNode = ((NodeValue)result.get(0)).asNode();
        
        return new InteractiveNode(parentNode, connector);
    }
    
    public List<InteractiveNode> children(){
        return null;
    }
}
