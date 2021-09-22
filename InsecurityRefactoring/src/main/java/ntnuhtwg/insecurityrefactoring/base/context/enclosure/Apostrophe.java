/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.context.enclosure;

import ntnuhtwg.insecurityrefactoring.base.context.CharsAllowed;
import ntnuhtwg.insecurityrefactoring.base.context.Plain;
import ntnuhtwg.insecurityrefactoring.base.context.VulnerabilityDescription;
import ntnuhtwg.insecurityrefactoring.base.info.ContextInfo;

/**
 *
 * @author blubbomat
 */
public class Apostrophe extends Enclosure{

    @Override
    public boolean isExploitable(ContextInfo contextInfo, CharsAllowed charsAllowed, VulnerabilityDescription description) {
        if(contextInfo.getVulnType().equals("xss")){
            boolean exploitable = getInside() instanceof Plain && getInside().isExploitable(contextInfo, charsAllowed, description);
            if(exploitable){
                description.addToExploitPath("Apostrophe are useless inside plain html context for XSS");
            }
            return exploitable;
        }
        
        return false;
    }

    @Override
    public boolean isEscapable(ContextInfo contextInfo, CharsAllowed charsAllowed, VulnerabilityDescription description) {
        boolean escapable = charsAllowed.isCharAllowed(contextInfo.getSufficientEscapes(), '\'');
        if(escapable){
            description.addToExploitPath("Escape the apostrophe with '");
            if(contextInfo.isSQLi()){
                description.flagVulnerable();
            }
            if(contextInfo.isXSS()){
                if(!(getInside() instanceof Plain)){
                    description.flagVulnerable();
                }
            }
        }
        return escapable;
    }
    
}
