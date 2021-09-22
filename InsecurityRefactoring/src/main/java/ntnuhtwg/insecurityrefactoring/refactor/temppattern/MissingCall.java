/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.refactor.temppattern;

import java.util.Objects;

/**
 *
 * @author blubbomat
 */
public class MissingCall {
    final String callType;
    final String name;
    final int numberOfParams;

    public MissingCall(String callType, String name, int numberOfParams) {
        this.callType = callType;
        this.name = name;
        this.numberOfParams = numberOfParams;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.callType);
        hash = 97 * hash + Objects.hashCode(this.name);
        hash = 97 * hash + this.numberOfParams;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MissingCall other = (MissingCall) obj;
        if (this.numberOfParams != other.numberOfParams) {
            return false;
        }
        if (!Objects.equals(this.callType, other.callType)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    

    @Override
    public String toString() {
        return callType + ": " + name + "(" + numberOfParams + ")";
    }

    public String getCallType() {
        return callType;
    }

    public String getName() {
        return name;
    }

    public int getNumberOfParams() {
        return numberOfParams;
    }
    
    
    
    
    
    
}
