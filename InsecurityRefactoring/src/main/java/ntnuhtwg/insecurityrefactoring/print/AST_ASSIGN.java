/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.print;

import java.lang.invoke.MethodHandles;
import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.tree.TreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;

/**
 *
 * @author blubbomat
 */
public class AST_ASSIGN {
    public static String string(INode node){
        List<String> flags = node.getFlags();
        for(String flag : flags){
            switch(flag){
                case "BINARY_ADD":
                    return "+=";
                
            }
        }
        
        return "missing" + MethodHandles.lookup().lookupClass().getName() + " flags: " + flags + " for node:" + node.id();
    }
    
    public static boolean brackets(INode node){
        List<String> flags = node.getFlags();
        for(String flag : flags){
            switch(flag){
                case "BINARY_BOOL_AND":
                case "BINARY_BOOL_OR":
                    return true;
                
            }
        }
        return false;
    }
        
}
