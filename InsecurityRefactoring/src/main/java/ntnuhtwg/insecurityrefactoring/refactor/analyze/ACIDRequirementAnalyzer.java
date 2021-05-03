///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package ntnuhtwg.insecurityrefactoring.refactor.analyze;
//
//import ntnuhtwg.insecurityrefactoring.base.info.ContextInfo;
//import ntnuhtwg.insecurityrefactoring.base.context.Context;
//import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
//
///**
// *
// * @author blubbomat
// */
//public class ACIDRequirementAnalyzer {
//    public static void analyzeRequirements(DFATreeNode node, ContextInfo context){
//        if(node == null){
//            return;
//        }
//        
//        node.clearContexts();
//        
//        for(Context req : Context.knownRequirements.values()){
//            boolean fullFills = req.fullfillsRequirement(context);
//            if(fullFills){
//                node.addContext(req);
//            }
//        }
//        
//        analyzeRequirements(node.getParent_(), context);
//    }
//}
