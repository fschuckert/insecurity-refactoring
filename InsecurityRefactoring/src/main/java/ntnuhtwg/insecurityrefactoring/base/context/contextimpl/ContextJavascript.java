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
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author blubbomat
 */
public class ContextJavascript extends Context{

    @Override
    public boolean fullfillsRequirement(ContextInfo context) {
        return false;
    }

    @Override
    public String toString() {
        return "Context Javascript";
    }
    
    
    
}
