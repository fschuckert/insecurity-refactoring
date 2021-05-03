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
public class AST_INCLUDE_OR_EVAL {
    public static String string(INode node) {
        List<String> flags = node.getFlags();
        for (String flag : flags) {
                switch (flag) {
                    case "EXEC_EVAL":
                        return "eval";
                    case "EXEC_INCLUDE":
                        return "include";
                    case "EXEC_INCLUDE_ONCE":
                        return "include_once";
                    case "EXEC_REQUIRE":
                        return "require";
                    case "EXEC_REQUIRE_ONCE":
                        return "require_once";
            }

            return "missing" + MethodHandles.lookup().lookupClass().getName() + " flags: " + flags + " for node:" + node.id();
        }
        
        return "";
    }
}
