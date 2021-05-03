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
public class RETURNS_REF {
    public static String string(INode node) {
        List<String> flags = node.getFlags();
        for (String flag : flags) {
                switch (flag) {
                    case "RETURNS_REF":
                        return "&";
                    case "FUNC_GENERATOR":
                        return "<<check this>> func_gen";
            }

//            return "missing" + MethodHandles.lookup().lookupClass().getName() + " flags: " + flags + " for node:" + node.id();
        }
        
        return "";
    }
}
