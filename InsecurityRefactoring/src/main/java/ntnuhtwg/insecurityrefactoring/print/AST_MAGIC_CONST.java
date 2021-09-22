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
public class AST_MAGIC_CONST {
    public static String string(INode node) {
        List<String> flags = node.getFlags();
        for (String flag : flags) {
            if(flag.startsWith("MAGIC_")){
                return flag.replace("MAGIC_", "");
            }
        }
        
        return "";
    }
}
