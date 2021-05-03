package ntnuhtwg.insecurityrefactoring.base.db.neo4j.dsl.gremlin;

///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package ntnuhtwg.insecurityrefactoring.dsl.gremlin;
//
//import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
//import org.apache.tinkerpop.gremlin.process.traversal.dsl.GremlinDsl;
//import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
//import org.apache.tinkerpop.gremlin.structure.Vertex;
//
///**
// *
// * @author blubbomat
// */
//
//@GremlinDsl
//public interface DataFlowTraversalDSL<S, E> extends GraphTraversal.Admin<S, E> {
//    public default GraphTraversal<S, Vertex> reaches(String varName){
//        return inE("REACHES").has("var", varName).outV();
//    }
//    
//    public default GraphTraversal<S, Vertex> child(Integer childNumber){
//        return out("PARENT_OF").has("childnum", childNumber);
//    }
//    
//    public default GraphTraversal<S, Vertex> children(){
//        return out("PARENT_OF");
//    }
//    
//    public default GraphTraversal<S, Vertex> concatNodes(){      
//         return  (GraphTraversal<S, Vertex>)
//                 emit(__.not(__.has("type", "AST_BINARY_OP")))
//                 .repeat((Traversal<?,E>)__.has("type", "AST_BINARY_OP").out("PARENT_OF")).times(10)
//                 ;         
//    }
//    
//    public default GraphTraversal<S, Vertex> concatNodesAddEdges(){      
////         (GraphTraversal<S, Vertex>)
//                 emit().as("a")
//                 .repeat((Traversal<?,E>)__.has("type", "AST_BINARY_OP").out("PARENT_OF")).times(10)
//                 .addE("DFO").from("a")
//                 ;     
//                 
//                 return null;
//    }
////    
////    public boolean isCall(){
////        return has
////    }
//    
////    public default GraphTraversal<S, Vertex> concatNodes(){    
////        return br
////    }
//}
