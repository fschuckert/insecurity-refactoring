/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.ast;

import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.LanguagePattern;

/**
 *
 * @author blubbomat
 */
public class AnyNode extends BaseNode{
    

    public AnyNode() {
        this.addProperty("type", "any");
    }
    
    public AnyNode setDots(boolean dots){
        if(dots){
            this.addProperty("...", "true");
        }
        return this;
    }

    
}
