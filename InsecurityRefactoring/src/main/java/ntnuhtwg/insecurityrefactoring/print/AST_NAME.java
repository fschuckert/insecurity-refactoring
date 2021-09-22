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
public class AST_NAME {
    public static String string(INode node) {
        List<String> flags = node.getFlags();
        for (String flag : flags) {
                switch (flag) {
                    case "NAME_FQ":
                        return "\\";
                    case "NAME_NOT_FQ":
                        return "";
                    case "NAME_RELATIVE":
                        return "namespace\\";
            }

            return "missing" + MethodHandles.lookup().lookupClass().getName() + " flags: " + flags + " for node:" + node.id();
        }
        
        return "";
    }
}
