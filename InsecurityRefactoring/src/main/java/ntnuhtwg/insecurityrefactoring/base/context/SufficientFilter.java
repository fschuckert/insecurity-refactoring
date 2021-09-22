/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.context;

import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.context.enclosure.Enclosure;
import ntnuhtwg.insecurityrefactoring.base.info.ContextInfo;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SanitizePattern;
import org.junit.experimental.runners.Enclosed;

/**
 *
 * @author blubbomat
 */
public class SufficientFilter {

    
    public static VulnerabilityDescription isSufficient(CharsAllowed charsAllowed, ContextInfo contextInfo, Enclosure sanEnclosings) {
        return isSufficient(charsAllowed, contextInfo, Util.list(sanEnclosings));
    }
    public static VulnerabilityDescription isSufficient(CharsAllowed charsAllowed, ContextInfo contextInfo, List<Enclosure> sanEnclosings) {
        Context context = ContextAnalyzer.analyze(contextInfo);
        
        for(Context sanEnclosing : sanEnclosings){
            if(sanEnclosing == null){
                continue;
            }
            sanEnclosing.setInside(context);
            context = sanEnclosing;
        }
        
        VulnerabilityDescription description = new VulnerabilityDescription();
        
        while(context != null){
            if(context.isExploitable(contextInfo, charsAllowed, description)){
                description.setExploitable(true);
                return description;
            }
            
            if(!context.isEscapable(contextInfo, charsAllowed, description)){
                description.setExploitable(false);
                return description;
            }
            
            context = context.getInside();
        }
        
      
        
        return description;
    }
    
}
