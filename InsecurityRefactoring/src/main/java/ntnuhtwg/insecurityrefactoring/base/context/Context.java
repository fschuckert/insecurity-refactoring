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
public abstract class Context {
    private Context inside;
    
    private String inbetween;
    
    boolean isInbetweenEmpty(){
        return this.getInbetween() == null || this.getInbetween().strip().equals("");
    }

    public String getInbetween() {
        return inbetween;
    }

    public void setInbetween(String inbetween) {
        this.inbetween = inbetween;
    }

    public Context getInside() {
        return inside;
    }

    public void setInside(Context inside) {
        this.inside = inside;
    }

    public abstract boolean isExploitable(ContextInfo contextInfo, CharsAllowed charsAllowed, VulnerabilityDescription description);

    public abstract boolean isEscapable(ContextInfo contextInfo, CharsAllowed charsAllowed, VulnerabilityDescription description);
    
    
}
