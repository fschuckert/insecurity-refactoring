/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.refactor.temppattern;

/**
 *
 * @author blubbomat
 */
public class VarName {
    private final String varName;
    private final int index;

    public VarName(String varName, int index) {
        this.varName = varName;
        this.index = index;
    }

    public String getVarName() {
        return varName;
    }

    public int getIndex() {
        return index;
    }
    
    public String getVarNameWithIndex(){
        return varName + " (" + index + ")";
    }
}
