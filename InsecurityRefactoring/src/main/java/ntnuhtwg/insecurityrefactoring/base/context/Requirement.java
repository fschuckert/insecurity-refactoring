/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.context;

/**
 *
 * @author blubbomat
 */
public class Requirement {
    private final Context context;
    private final boolean hasToBeTrue;
    

    public Requirement(Context requirement, boolean hasToBeTrue) {
        this.hasToBeTrue = hasToBeTrue;
        this.context = requirement;
    }

    public Context getContext() {
        return context;
    }
    
    public boolean hasToBeTrue() {
        return hasToBeTrue;
    }
    
    
    
    
    
}
