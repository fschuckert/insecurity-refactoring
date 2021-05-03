/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author blubbomat
 */
public class RefactoredCode {
    private SourceLocation sourceLocation;
    private String code;
    private Set<Integer> modifiedLines;

    public RefactoredCode(SourceLocation sourceLocation, String code, Set<Integer> modifiedLines) {
        this.sourceLocation = sourceLocation;
        this.code = code;
        this.modifiedLines = modifiedLines;
    }

    

    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    public String getCode() {
        return code;
    }

    public Set<Integer> getModifiedLines() {
        return modifiedLines;
    }
    
    
    
    
}
