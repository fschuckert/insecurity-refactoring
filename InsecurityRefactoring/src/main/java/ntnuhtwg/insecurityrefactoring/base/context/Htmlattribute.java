/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.context;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.context.enclosure.Enclosure;
import ntnuhtwg.insecurityrefactoring.base.info.ContextInfo;

/**
 *
 * @author blubbomat
 */
public class Htmlattribute extends Context {

    private final Enclosure enclosure;
    private final String attribute;
    private final String parameter;

    private Map<String, List<String>> jsAttributes = new HashMap<>() {
        {
            put("a", new LinkedList<>(Arrays.asList("onclick")));
            put("img", new LinkedList<>(Arrays.asList("onbeforeactivate")));
            put("img", new LinkedList<>(Arrays.asList("onerror")));
        }
    };

    private Map<String, List<String>> javascriptPreAttributes = new HashMap<>() {
        {
            put("a", new LinkedList<>(Arrays.asList("href")));
        }
    };

    public Htmlattribute(Enclosure enclosure, String attribute, String parameter) {
        this.enclosure = enclosure;
        this.attribute = attribute;
        this.parameter = parameter;
    }

    public Enclosure getEnclosure() {
        return enclosure;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getParameter() {
        return parameter;
    }

    @Override
    public boolean isExploitable(ContextInfo contextInfo, CharsAllowed charsAllowed, VulnerabilityDescription description) {
        // check if javascript parameter
        if (isInsideAttributeMap(jsAttributes)) {
            
            //decision tree
            if(charsAllowed.isSpecialAllowed()){
                description.flagVulnerable();
            }
            
            if(charsAllowed.isCharAllowed(contextInfo.getSufficientEscapes(), ';')){
                description.addToExploitPath("Is already inside a javascript parameter -> ; can be used to create another statement");
                return true;
            }
        }
        
        // check if an parameter than can be injected with -> javascript:payload
        if(isInsideAttributeMap(javascriptPreAttributes)){
            
            //decision tree
            if(charsAllowed.isSpecialAllowed()){
                description.flagVulnerable();
            }
            
            if(this.isInbetweenEmpty()){
                if(charsAllowed.isCharAllowed(contextInfo.getSufficientEscapes(), ':') && charsAllowed.isLetterAllowed()){
                    description.addToExploitPath("It is possible to create a javascript context with: javascript:alert(1)");
                    return true;
                }
            }
        }

        // inject with another parameter possible?
        if (enclosure == null || enclosure.isEscapable(contextInfo, charsAllowed, description)) {
            if(jsAttributes.containsKey(attribute) && charsAllowed.areCharsAllowed(contextInfo.getSufficientEscapes(), '=')){
                description.addToExploitPath("It is possible to create javascript parameters for: " +attribute + " attributes: " + Util.joinStr(jsAttributes.get(attribute), ", "));
                return true;
            }
            
            if(javascriptPreAttributes.containsKey(attribute) && charsAllowed.areCharsAllowed(contextInfo.getSufficientEscapes(), '=', ':')){
                description.addToExploitPath("It is possible to create pre-javascript: parameters with ='javascript:...' " +attribute + " attributes: " + Util.joinStr(javascriptPreAttributes.get(attribute), ", "));
                return true;
            }
        } 
        
        return false;
    }

    @Override
    public boolean isEscapable(ContextInfo contextInfo, CharsAllowed charsAllowed, VulnerabilityDescription description) {
        boolean isEnclosureEscapable = enclosure.isEscapable(contextInfo, charsAllowed, description);
        if(isEnclosureEscapable){
            description.flagVulnerable();
        }
        
        boolean isEscapable = isEnclosureEscapable && charsAllowed.isCharAllowed(contextInfo.getSufficientEscapes(), '>');
        if(isEscapable){
            description.addToExploitPath("Escape html attribute using > attribute: " + attribute);
            
        }
        return isEscapable;
    }

    private boolean isInsideAttributeMap(Map<String, List<String>> attributeMap) {
        List<String> parameters = attributeMap.get(attribute);
        if (parameters == null) {
            return false;
        }

        return parameters.contains(parameter);
    }

}
