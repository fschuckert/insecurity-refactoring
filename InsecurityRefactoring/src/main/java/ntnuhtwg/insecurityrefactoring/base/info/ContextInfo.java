/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.info;

import java.util.Set;
import ntnuhtwg.insecurityrefactoring.base.context.EscapeChar;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author blubbomat
 */
public class ContextInfo {
    
    private final String vulnType;
    private final Set<EscapeChar> sufficientEscapes;
    private final String pre;
    private final String post;

    public ContextInfo(String vulnType, Set<EscapeChar> sufficientEscapes, String pre, String post) {
        this.vulnType = vulnType;
        this.sufficientEscapes = sufficientEscapes;
        this.pre = pre;
        this.post = post;
    }

    public String getPre() {
        return pre;
    }

    public String getPost() {
        return post;
    }

    public String getVulnType() {
        return vulnType;
    }

    public Set<EscapeChar> getSufficientEscapes() {
        return sufficientEscapes;
    }
    
    
    public boolean isXSS(){
        return "xss".equals(vulnType);
    }
    
    public boolean isSQLi(){
        return "sqli".equals(vulnType);
    }
    
    
    
    
    
    
}
