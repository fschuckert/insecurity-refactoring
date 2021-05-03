/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.patterns;

import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.exception.TimeoutException;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.cypher.DataflowDSL;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4jDB;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;

/**
 *
 * @author blubbomat
 */
public interface PassthroughPattern{
    
    public boolean isPassthrough();
    
    public DataType getDataInputType();
    public DataType getDataOutputType();
    
//    public INode findInputNode(DataflowDSL dsl, INode node);
    
    public boolean equalsPattern(INode node, Neo4jDB db) throws TimeoutException;
    
    public String getName();

    public List<INode> findInputNodes(DataflowDSL dsl, INode obj)  throws TimeoutException ;
    
}
