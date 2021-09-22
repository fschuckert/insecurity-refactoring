/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.context;

import java.util.Objects;

/**
 *
 * @author blubbomat
 */
public class EscapeChar {

    public EscapeChar(Character escapeChar) {
        this.escapeChar = escapeChar;
    }
    
    
    
    private final Character escapeChar; // null -> double the chars
    
    
    public boolean isDoubleEscape(){
        return escapeChar == null;
    }

    public Character getEscapeChar() {
        return escapeChar;
    }
    
    

    @Override
    public String toString() {
        return escapeChar == null ? "double" : escapeChar.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
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
        final EscapeChar other = (EscapeChar) obj;
        if (!Objects.equals(this.escapeChar, other.escapeChar)) {
            return false;
        }
        return true;
    }
    
    
    
    
}
