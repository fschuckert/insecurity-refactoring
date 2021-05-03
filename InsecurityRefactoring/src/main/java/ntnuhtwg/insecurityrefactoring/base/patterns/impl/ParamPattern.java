/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.patterns.impl;

import java.util.LinkedList;
import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;

/**
 *
 * @author blubbomat
 */
public class ParamPattern extends Pattern{

    public ParamPattern(String paramStr) {
        List<String> codeLines  = new LinkedList<>();
        codeLines.add(paramStr);
        
        this.setCodeLines(codeLines);
    }
    
}
