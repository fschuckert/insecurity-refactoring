/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base;

/**
 *
 * @author blubbomat
 */
public enum ASTType {
    variable,
    statement,
    expression;

    @Override
    public String toString() {
        switch(this){
            case variable:
                return "variable";
            case statement:
                return "statement";
            case expression:
                return "expression";
        }
        
        return "";
    }
    
    
    
}
