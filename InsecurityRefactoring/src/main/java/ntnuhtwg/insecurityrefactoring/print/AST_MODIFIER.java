/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.print;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.StringJoiner;
import ntnuhtwg.insecurityrefactoring.base.tree.TreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;

/**
 *
 * @author blubbomat
 */
public class AST_MODIFIER {
    public static String string(INode node){
        String retval = "";
        List<String> flags = node.getFlags();
        for(String flag : flags){
            switch(flag){
                case "MODIFIER_PUBLIC":
                    retval += "public "; 
                    break;
                case "MODIFIER_PROTECTED":
                    retval += "protected ";
                    break;
                case "MODIFIER_PRIVATE":
                    retval += "private ";  
                    break;
                case "MODIFIER_STATIC":
                    retval += "static ";
                    break;
                case "MODIFIER_ABSTRACT":
                    retval += "abstract ";
                    break;
                case "MODIFIER_FINAL":
                    retval += "final";     
                    break;
            }
        }
        
        return retval;
    }
        
}
