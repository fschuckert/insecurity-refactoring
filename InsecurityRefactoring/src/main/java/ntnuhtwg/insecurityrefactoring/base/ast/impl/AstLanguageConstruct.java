/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.ast.impl;

import ntnuhtwg.insecurityrefactoring.base.ast.BaseNode;

/**
 *
 * @author blubbomat
 */
public class AstLanguageConstruct extends BaseNode{

    public AstLanguageConstruct(String constructName) {
        this.addProperty("type", "AST_" + constructName.toUpperCase());
    }
    
}
