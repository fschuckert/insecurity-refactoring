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
public class Javascript extends Context {

    @Override
    public boolean isExploitable(ContextInfo contextInfo, CharsAllowed charsAllowed, VulnerabilityDescription description) {
        if (charsAllowed.isSpecialAllowed()) {
            description.flagVulnerable();
        }

        boolean exploitable = charsAllowed.areCharsAllowed(contextInfo.getSufficientEscapes(), ';');
        if (exploitable) {
            description.addToExploitPath("The ; can be used to chain commands");
        }
        return exploitable;
    }

    @Override
    public boolean isEscapable(ContextInfo contextInfo, CharsAllowed charsAllowed, VulnerabilityDescription description) {
        boolean isEscapable = charsAllowed.areCharsAllowed(contextInfo.getSufficientEscapes(), '>');
        if (isEscapable) {
            description.addToExploitPath("Use the > to escape the Javascript context");
        }
        return isEscapable;
    }

}
