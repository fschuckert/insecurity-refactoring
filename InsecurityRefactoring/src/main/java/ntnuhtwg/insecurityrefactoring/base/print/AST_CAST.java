/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.print;

import java.lang.invoke.MethodHandles;
import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;

/**
 *
 * @author blubbomat
 */
public class AST_CAST {
        public static String string(INode node){
        List<String> flags = node.getFlags();
        for(String flag : flags){
            switch(flag){
//                case "TYPE_NULL":
//                    return ""
                case "TYPE_BOOL":
                    return "(bool)";
                case "TYPE_LONG":
                    return "(int)";                   
                case "TYPE_DOUBLE":
                    return "(double)";
                case "TYPE_STRING":
                    return "(string)";
                case "TYPE_ARRAY":
                    return "(array)";
                case "TYPE_OBJECT":
                    return "(object)";
            }
        }
        
        return "missing" + MethodHandles.lookup().lookupClass().getName() + " flags: " + flags + " for node:" + node.id();
    }
}
