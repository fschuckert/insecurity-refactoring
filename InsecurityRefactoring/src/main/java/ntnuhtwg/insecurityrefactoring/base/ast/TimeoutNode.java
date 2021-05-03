/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.ast;

import ntnuhtwg.insecurityrefactoring.base.SourceLocation;

/**
 *
 * @author blubbomat
 */
public class TimeoutNode extends BaseNode{
    final SourceLocation from;
    final SourceLocation to;

    public TimeoutNode(SourceLocation from, SourceLocation to) {
        this.from = from;
        this.to = to;
    }
    
    
    
}
