package ntnuhtwg.insecurityrefactoring.base.db.neo4j;

///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package ntnuhtwg.insecurityrefactoring.neo4j;
//
//import com.steelbridgelabs.oss.neo4j.structure.Neo4JElementIdProvider;
//import com.steelbridgelabs.oss.neo4j.structure.Neo4JGraph;
//import com.steelbridgelabs.oss.neo4j.structure.providers.Neo4JNativeElementIdProvider;
//import ntnuhtwg.insecurityrefactoring.dsl.gremlin.DataFlowTraversalSource;
//import org.apache.tinkerpop.gremlin.process.traversal.dsl.GremlinDsl;
//import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
//import org.apache.tinkerpop.gremlin.structure.Direction;
//import org.apache.tinkerpop.gremlin.structure.Graph;
//import org.apache.tinkerpop.gremlin.structure.Vertex;
//import org.neo4j.driver.v1.AuthTokens;
//import org.neo4j.driver.v1.Driver;
//import org.neo4j.driver.v1.GraphDatabase;
//
//
///**
// *
// * @author blubbomat
// */
//public class GremlinConnection {
//    
//    private Graph graph;
//    private Driver driver;
//    private DataFlowTraversalSource dataflow;
//    
//    public void connect(){
//        
//        System.out.println("driver");
//        driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "admin"));
//       
//        System.out.println("Element id provider");
//        Neo4JElementIdProvider<?> provider = new Neo4JNativeElementIdProvider();
//        
//        
//        
//        System.out.println("Graph");
//        try{
//            graph = new Neo4JGraph(driver, provider, provider);
//            dataflow = graph.traversal(DataFlowTraversalSource.class);
////            graph.
//        }
//        catch(Exception ex){
//            ex.printStackTrace();
//        }
//        
//    }
//    
//    
//
////    public Driver getDriver() {
////        return driver;
////    }
//
//    public DataFlowTraversalSource dataflow() {
//        return dataflow;
//    }
//
//    public void disconnect() {
//        driver.close();
//    }
//
//    public Driver getDriver() {
//        return driver;
//    }
//
//    public Graph getGraph() {
//        return graph;
//    }
//
//    
//    
//    
//    
//    
//    
//    
//    
//}
