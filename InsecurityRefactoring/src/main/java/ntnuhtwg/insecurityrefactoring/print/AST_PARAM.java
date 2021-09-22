/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.print;

import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;

/**
 *
 * @author blubbomat
 */
public class AST_PARAM {
    public static String reference(INode node){
        List<String> flags = node.getFlags();
        
        if(flags.contains("PARAM_REF")){
            return "&";
        }
        
        return "";
    }
    
    public static String variadic(INode node){
        List<String> flags = node.getFlags();
        
        if(flags.contains("PARAM_VARIADIC")){
            return "...";
        }
        
        return "";
    }
}
