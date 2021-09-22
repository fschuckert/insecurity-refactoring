/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.db.neo4j;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.NodeEmbedded;
import org.neo4j.configuration.GraphDatabaseSettings;
import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;
import org.neo4j.configuration.connectors.BoltConnector;
import org.neo4j.cypher.internal.v4_0.ast.NodeByParameter;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.impl.core.NodeEntity;
/**
 *
 * @author blubbomat
 */
public class Neo4jEmbedded implements Neo4jDB{
//    String dbPath = "/home/blubbomat/Development/FindPOI/neo4j-community-3.5.13/data/databases/graph.db/";
//    private static final File databaseDirectory = new File( "/home/blubbomat/Development/FindPOI/neo4j-community-3.5.13/data/databases/graph.db/databases" ); // + neo4j works without exception
    private static final File databaseDirectory = new File( "/home/blubbomat/Development/FindPOI/neo4j-community-4.0.3/" );
    private DatabaseManagementService managementService;
    private GraphDatabaseService graphDb;
    
    
    public void openDB(){
        System.out.println("Starting build");
        managementService = new DatabaseManagementServiceBuilder( databaseDirectory ).build();
        System.out.println("Starting graph db");
        graphDb  = managementService.database( DEFAULT_DATABASE_NAME );
        
//        System.out.println("Starting transaction");
//        Label label = Label.label("AST");
//        int id = 1;
//        try(Transaction tx = graphDb.beginTx()){
////            Node node = tx.findNode(label, "id", id);
//                tx.
//
////            for(Node node : tx.getAllNodes()){
////                System.out.println("nodes: " + node);
////            }
////
////            for(Label labe : tx.getAllLabels()){
////                System.out.println("label: " + labe);
////            }
//            ResourceIterator<Node> nodes = tx.findNodes(label, "type", "AST_ECHO");
//            
//            while(nodes.hasNext()){
//                Node node = nodes.next();
//                System.out.println("Tesult" + node);
//            }
////            
////            Node node = tx.getNodeById(1).;
////            System.out.println("with id " + node);
//
////              tx.findNodes(label, "type", "AST_ECHO").forEachRemaining((arg0) -> {
////                  arg0
////              });
//
//
////            tx.
//        }
//        
//        
//        
//        
//        
//        
//        
//        managementService.shutdown();
    }
    
    
    
    
     private List<INode> runRead(String query,  Object... keysAndValues){  
        Map<String, Object> parameters = Values.parameters(keysAndValues).asMap();
        List<INode> result = new LinkedList<>();
         try(Transaction tx = graphDb.beginTx()){
             Result  resultTx = tx.execute(query, parameters);

             while(resultTx.hasNext()){
                Map<String, Object> res = resultTx.next();
                Node node = (Node)res.values().iterator().next();
                result.add( new NodeEmbedded( node ));
             }
         }
        
        return result;
    }
    
    
    public static void main(String[] args){
        Neo4jEmbedded embedded = new Neo4jEmbedded();
        embedded.openDB();
        embedded.runRead("match (n) return n", new HashMap<>());
    }

    @Override
    public INode findFirstNode(String query, Object... keysAndValues) {
        List<INode> results = runRead(query, keysAndValues);
        
        if(results.size() > 1){
            System.err.println("A query has multiple results qry:" + query + " parameters:" + keysAndValues + " result size:" + results.size() + " We are using the first result, but the data might be corrupt.");
        }
         
        if(results.isEmpty()){
            return null;
        }
         
         return results.get(0);
    }

    @Override
    public List<INode> findAll(String query, Object... keysAndValues) {
        return runRead(query, keysAndValues);
    }

    @Override
    public INode findNode(Long id) {
        return findFirstNode(
                "MATCH (n) WHERE id(n)=$id return n", 
                "id", id);
    }

    @Override
    public void close() throws Exception {
        this.managementService.shutdown();
    }

    @Override
    public boolean checkConnection() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    
}
