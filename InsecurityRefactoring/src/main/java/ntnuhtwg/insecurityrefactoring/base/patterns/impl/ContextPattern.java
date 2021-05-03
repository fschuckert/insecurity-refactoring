/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.patterns.impl;

import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.info.ContextInfo;
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;

/**
 *
 * @author blubbomat
 */
public class ContextPattern extends Pattern{
    private final String vulnType;
    private final String pre;
    private final String post;
   

    public ContextPattern(String vulnType, String pre, String post, DataType dataInput, DataType dataOutput) {
        this.vulnType = vulnType;
        this.pre = pre;
        this.post = post;
        this.dataInput = dataInput;
        this.dataOutput = dataOutput;
    }

    public String getVulnType() {
        return vulnType;
    }

    public String getPre() {
        return pre;
    }

    public String getPost() {
        return post;
    }

    public ContextInfo getContextInfo() {
        return new ContextInfo(vulnType, pre, post);
    }

    
    
    
    
    
    
}
