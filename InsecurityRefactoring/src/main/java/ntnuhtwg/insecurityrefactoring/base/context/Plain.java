/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.context;

import ntnuhtwg.insecurityrefactoring.base.info.ContextInfo;

/**
 *
 * @author blubbomat
 */
public class Plain extends Context{

    @Override
    public boolean isExploitable(ContextInfo contextInfo, CharsAllowed charsAllowed, VulnerabilityDescription description) {
        switch(contextInfo.getVulnType()){
            case "xss":
                // Plain requires to switch context! Can only be archived with: < >
//                if(charsAllowed.isSpecialAllowed()){
//                    description.flagVulnerable();
//                }
                
                boolean xssCharsAllowed = charsAllowed.areCharsAllowed(contextInfo.getSufficientEscapes(), '<', '>');
                if(xssCharsAllowed){
                    description.addToExploitPath("Create script tag with <script>");
                }
                return xssCharsAllowed;
                
            case "sqli":
                boolean sqliPossible = !contextInfo.getPre().isBlank() && charsAllowed.isSpecialAllowed();                
                if(sqliPossible){
                    description.addToExploitPath("No enclosure and special chars are allowed -> SQL injection");
                }
                if(charsAllowed.isSpecialAllowed()){
                    description.flagVulnerable();
                }
                return sqliPossible;
        }
        
        return false;
    }

    @Override
    public boolean isEscapable(ContextInfo contextInfo, CharsAllowed charsAllowed, VulnerabilityDescription description) {
        return false;
    }
    
}
