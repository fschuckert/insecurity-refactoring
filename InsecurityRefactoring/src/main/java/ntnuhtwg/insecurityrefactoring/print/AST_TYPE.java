/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.print;

import java.lang.invoke.MethodHandles;
import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;

/**
 *
 * @author blubbomat
 */
public class AST_TYPE {

    public static String string(INode node) {
        List<String> flags = node.getFlags();
        for (String flag : flags) {
                switch (flag) {
                    case "TYPE_ARRAY":
                        return "array ";
                    case "TYPE_CALLABLE":
                        return "callable ";
                    case "TYPE_VOID":
                        return "void ";
                    case "TYPE_BOOL":
                        return "bool ";
                    case "TYPE_LONG":
                        return "int ";
                    case "TYPE_DOUBLE":
                        return "float ";
                    case "TYPE_STRING":
                        return "string ";
                    case "TYPE_ITERABLE":
                        return "iterable ";
//                    case "TYPE_OBJECT":
//                        return 
//                    case "TYPE_NULL    // php 8.0 union types
//                    case "TYPE_FALSE   // php 8.0 union types
//                    case "TYPE_STATIC  // php 8.0 static return type
            }

            return "missing" + MethodHandles.lookup().lookupClass().getName() + " flags: " + flags + " for node:" + node.id();
        }
        
        return "";
    }
}
