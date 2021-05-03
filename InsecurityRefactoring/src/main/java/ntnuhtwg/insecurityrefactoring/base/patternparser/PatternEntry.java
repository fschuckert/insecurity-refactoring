/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.patternparser;

/**
 *
 * @author blubbomat
 */
public class PatternEntry {
    public final String identifier;
    
    public final boolean isIdentifier;
    public final boolean isList;
    public final boolean optional;

//    public PatternEntry(String identifier) {
//        this.identifier = identifier;
//        this.isIdentifier = true;
//        isList = false;
//        optional = false;
//    }
//    
//    public PatternEntry(String identifier, boolean isIdentifier) {
//        this.identifier = identifier;
//        this.isIdentifier = isIdentifier;
//        isList = false;
//        optional = false;
//    }

    public PatternEntry(String identifier, boolean isIdentifier, boolean isList, boolean optional) {
        this.identifier = identifier.replace("\\<", "<").replace("\\>", ">");
        this.isIdentifier = isIdentifier;
        this.isList = isList;
        this.optional = optional;
    }
    
    

    @Override
    public String toString() {
        return identifier + "(isId:" + isIdentifier + ')';
    }
    
    
    
}
