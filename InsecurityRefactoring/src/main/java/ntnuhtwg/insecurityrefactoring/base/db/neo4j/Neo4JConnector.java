/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.db.neo4j;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import ntnuhtwg.insecurityrefactoring.base.SourceLocation;
import ntnuhtwg.insecurityrefactoring.base.exception.TimeoutException;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.NodeConnected;
import org.neo4j.driver.internal.value.NodeValue;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Logging;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.driver.types.Node;

/**
 *
 * @author blubbomat
 */
public class Neo4JConnector implements AutoCloseable, Neo4jDB{
    private final Driver driver;
    
    public Neo4JConnector( String uri, String user, String password )
    {
        driver = GraphDatabase.driver( uri, AuthTokens.basic( user, password ), Config.builder().withLogging(Logging.none()).build());
    }
    
    public boolean checkConnection(){
        try {
            findNode(0L);
            return true;
//        driver.verifyConnectivity();
        } catch (TimeoutException ex) {
            return false;
        }
    }
    
    @Override
    public void close() throws Exception
    {
        driver.close();
//        driver.
    }
    
//    /**
//     * runs a query on the neo4j database.
//     * Example: "CREATE (a:Greeting) SET a.message = $message RETURN a.message + ', from node ' + id(a)"
//     * 
//     */
//    public List<Record> runWrite(String query, Map<String, Object> parameters){     
//        List<Record> result = null;
//        try ( Session session = driver.session() )
//        {
//            session.beginTransaction();
//            
//           
//            
//            List<Record> result = session.writeTransaction( new TransactionWork<List<Record>>()
//            {
//                @Override
//                public List<Record> execute( Transaction tx )
//                {
//                    Result r = tx.run(query, parameters);
//                    return r.list();
//                }
//            } );
//        }
//        
//        return result;
//    }
    
    /**
     * runs a query on the neo4j database.
     * Example: "CREATE (a:Greeting) SET a.message = $message RETURN a.message + ', from node ' + id(a)"
     * 
     */
    public List<Record> runRead(String query, Value parameters ) throws TimeoutException{     
//        System.out.println("Running: " + query);
        List<Record> result = List.of();
        try{
        try ( Session session = driver.session() )
        {
            result = session.readTransaction( new TransactionWork<List<Record>>()
            {
                @Override
                public List<Record> execute( Transaction tx )
                {
                    Result r = tx.run(query, parameters);
 
                    return r.list();
                }
            } );
        }
        } catch(ClientException ex){
            throw new TimeoutException(ex);
        }
        
//        System.out.println("finish");
        
        return result;
    }
    
    @Override
    public INode findFirstNode(String query, Object... keysAndValues) throws TimeoutException{
        return findFirstNode(query, Values.parameters(keysAndValues));
    }
    
    public INode findFirstNode(String query, Value parameters) throws TimeoutException{
        List<Record> result = runRead(query, parameters);
        
        if(result.size() > 1){
            System.err.println("A query has multiple results qry:" + query + " parameters:" + parameters + " result size:" + result.size() + " We are using the first result, but the data might be corrupt.");
        }
        
        if(result.size() == 0){
            return null;
        }
        
        Node node = (Node)((NodeValue)result.get(0).get(0)).asNode();
        
        return new NodeConnected(node);
    }
    
    @Override
    public INode findNode(Long id) throws TimeoutException{
        List<Record> result = this.runRead("MATCH (n) WHERE id(n)=$id return n", Values.parameters("id", id));
        if(result.size() == 1){
            Node node = ((NodeValue)result.get(0).get(0)).asNode();
            return new NodeConnected(node);
        }
        
        return null;
        
    }
    
   

    public List<INode> findAll(String query, Value parameters)  throws TimeoutException{
        List<Record> result = runRead(query, parameters);

        if(result.isEmpty()){
            return Collections.emptyList();
        }
        
        List<INode> retval = new LinkedList<>();
        
        for(Record record : result){
            Node n = ((NodeValue)record.get(0)).asNode();
            retval.add(new NodeConnected(n));
        }
        
        return retval;
    }
    
    
    @Override        
    public List<INode> findAll(String query, Object... keysAndValues)  throws TimeoutException{
        return findAll(query, Values.parameters(keysAndValues));
    }
}
