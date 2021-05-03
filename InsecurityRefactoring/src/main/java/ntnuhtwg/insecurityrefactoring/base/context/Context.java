/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.context;

import java.util.HashMap;
import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.info.ContextInfo;
import ntnuhtwg.insecurityrefactoring.base.context.contextimpl.ContextApostroph;
import ntnuhtwg.insecurityrefactoring.base.context.contextimpl.ContextHTMLAttribute;
import ntnuhtwg.insecurityrefactoring.base.context.contextimpl.ContextJavascript;
import ntnuhtwg.insecurityrefactoring.base.context.contextimpl.ContextQuotes;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;

/**
 *
 * @author blubbomat
 */
public abstract class Context {
    
    public static HashMap<String, Context> knownRequirements = new HashMap<>() {{
            put("context(apostrophe)".toLowerCase(), new ContextApostroph());
            put("context(quotes)".toLowerCase(), new ContextQuotes());
            put("context(javascript)".toLowerCase(), new ContextJavascript());
            put("context(attribute)".toLowerCase(), new ContextHTMLAttribute());
    }};;
    
    
    
    public abstract boolean fullfillsRequirement(ContextInfo context); 
}
