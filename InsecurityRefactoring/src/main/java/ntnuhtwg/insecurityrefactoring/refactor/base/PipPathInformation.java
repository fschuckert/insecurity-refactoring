/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.refactor.base;

import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;

/**
 *
 * @author blubbomat
 */
public class PipPathInformation {
    final DFATreeNode source;
    final boolean containsTimeout;
    final boolean containsSanitization;

    public PipPathInformation(DFATreeNode source, boolean containsTimeout, boolean containsSanitization) {
        this.source = source;
        this.containsTimeout = containsTimeout;
        this.containsSanitization = containsSanitization;
    }

    public DFATreeNode getSource() {
        return source;
    }

    public boolean isContainsTimeout() {
        return containsTimeout;
    }

    public boolean isContainsSanitization() {
        return containsSanitization;
    }
    
    public boolean isVulnerable(){
        return !containsTimeout && !containsSanitization;
    }
    
    
}
