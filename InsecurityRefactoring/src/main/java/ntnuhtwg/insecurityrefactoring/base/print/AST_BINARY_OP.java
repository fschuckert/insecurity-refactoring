/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.print;

import java.lang.invoke.MethodHandles;
import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.tree.TreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;

/**
 *
 * @author blubbomat
 */
public class AST_BINARY_OP {
    public static String string(INode node){
        List<String> flags = node.getFlags();
        for(String flag : flags){
            switch(flag){
                case "BINARY_BITWISE_OR":
                    return " | ";
                case "BINARY_BITWISE_AND":
                    return " & ";
                case "BINARY_BITWISE_XOR":
                    return " ^ ";

                case "BINARY_CONCAT":
                    return " . ";

                case "BINARY_ADD":
                    return " + ";      
                case "BINARY_SUB":
                    return " - ";
                case "BINARY_MUL":
                    return " * ";
                case "BINARY_DIV":
                    return " / ";
                case "BINARY_MOD":
                    return " % ";
                case "BINARY_POW":
                    return " ** ";

                case "BINARY_SHIFT_LEFT":
                    return " << ";
                case "BINARY_SHIFT_RIGHT":
                    return " >> ";

                case "BINARY_COALESCE":
                    return " ?? ";

                case "BINARY_BOOL_AND":
                    return " && ";
                case "BINARY_BOOL_OR":
                    return " || ";
                case "BINARY_BOOL_XOR":
                    return " xor ";

                case "BINARY_IS_IDENTICAL":
                    return " === ";
                    
                case "BINARY_IS_NOT_IDENTICAL":
                    return " !== ";
                       
                case "BINARY_IS_EQUAL":
                    return " == ";
                case "BINARY_IS_NOT_EQUAL":
                    return " != ";
                case "BINARY_IS_SMALLER":
                    return " < ";
                case "BINARY_IS_SMALLER_OR_EQUAL":
                    return " <= ";
                case "BINARY_IS_GREATER":
                    return " > ";
                case "BINARY_IS_GREATER_OR_EQUAL":
                    return " >= ";
                case "BINARY_SPACESHIP":
                    return " <=> ";
                
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
