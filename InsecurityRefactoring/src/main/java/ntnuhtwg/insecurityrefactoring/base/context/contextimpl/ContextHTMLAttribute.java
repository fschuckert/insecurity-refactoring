/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.context.contextimpl;

import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.info.ContextInfo;
import ntnuhtwg.insecurityrefactoring.base.context.Context;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;

/**
 *
 * @author blubbomat
 */
public class ContextHTMLAttribute extends Context{

    @Override
    public boolean fullfillsRequirement(ContextInfo context) {
        String pre = context.getPre().trim();
        
        if(!pre.endsWith("'") && !pre.endsWith("\"")){
            return false;
        }
        
        String subPre = pre.substring(0, context.getPre().length()-1).trim();
        
        if(!subPre.endsWith("=")){
            return false;
        }
        
        return true;
    }
    
}
