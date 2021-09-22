/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.context;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ntnuhtwg.insecurityrefactoring.base.context.enclosure.Apostrophe;
import ntnuhtwg.insecurityrefactoring.base.context.enclosure.Enclosure;
import ntnuhtwg.insecurityrefactoring.base.context.enclosure.Quotes;
import ntnuhtwg.insecurityrefactoring.base.info.ContextInfo;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author blubbomat
 */
public class ContextAnalyzer {

    public static Context analyze(ContextInfo contextInfo) {
        switch(contextInfo.getVulnType()){
            case "xss": return analyzeXSSRec(contextInfo.getPre());
            case "sqli" : return analyzeSQLiRec(contextInfo);
        }
        
        throw new NotImplementedException("Context analyzer only implemented for xss and sqli");
    }

    private static Context analyzeXSSRec(String pre) {
        Pattern xssSymbol = Pattern.compile("(>|=|'|\")[^>='\"]*$");
        Matcher matcher = xssSymbol.matcher(pre);

        if (!matcher.find()) {
            return new Plain();
        }

        char symbol = pre.charAt(matcher.start());
        String between = pre.substring(matcher.start() + 1);
        String remaining = pre.substring(0, matcher.start());

        Enclosure enclosure = null;
        Context context = null;
        int i = 10;
        switch (symbol) {
            case '"':                
            case '\'':                
            case '=':
                if(symbol == '"'){
                    enclosure = new Quotes();
                }
                if(symbol == '\''){
                    enclosure = new Apostrophe();
                }
                if (isHtmlAttribute(remaining)) {
                    String param = htmlParameter(remaining);
                    String attribute = htmlAttribute(remaining);
                    context = new Htmlattribute(enclosure, attribute, param);
                    context.setInbetween(between);
                    remaining = remainingofHtmlAttribute(remaining);
                } else {
                    if(symbol == '='){
                        return analyzeXSSRec(remaining);
                    }
                    context = enclosure;
                }

                break;
            case '>':
                if(isScriptTag(remaining)){
                    Javascript javascript = new Javascript();
                    remaining = remainingOfScriptTag(remaining);
                    return javascript;
                }
                return new Plain();

        }
        
        if(context != null){
            Context inside = analyzeXSSRec(remaining);
            context.setInside(inside);
        }
        else{
            context = new Plain();
        }

        return context;
    }
    
    private static String remainingOfScriptTag(String remaining){
        return getBeforeStr(".*\\<\\s*script\\s*$", remaining);
    }
    
    private static boolean isScriptTag(String remaining){
        return matches(".*\\<\\s*script\\s*$", remaining);
    }
  
    private static String remainingofHtmlAttribute(String remaining){
        return getBeforeStr("<([\\s[\\w='\"/\\.#\\?_-~&]])*\\s+\\w+\\s*=\\s*$", remaining);
    }

    private static boolean isHtmlAttribute(String remaining) {
        return matches("<([\\s[\\w='\"/\\.#\\?_-~&]])*\\s+\\w+\\s*=\\s*$", remaining);
    }

    private static String htmlParameter(String remaining) {
        return getStr("\\w+(?=(\\s*=\\s*$))", remaining);
    }

    private static String htmlAttribute(String remaining) {
        return getStr("(?<=(<\\s*))\\w+(?=(([\\s[\\\\w='\\\"/\\.#\\?_-~&]])*\\s+\\w+\\s*=\\s*$))", remaining);
    }

    private static boolean matches(String regex, String str) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);

        return matcher.find();
    }

    private static String getStr(String regex, String str) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);

        if (!matcher.find()) {
            return null;
        }

        return str.substring(matcher.start(), matcher.end());
    }
    
    private static String getBeforeStr(String regex, String str) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);

        if (!matcher.find()) {
            return null;
        }

        return str.substring(0, matcher.start());
    }

    private static Context analyzeSQLiRec(ContextInfo context) {
        if(StringUtils.countMatches(context.getPre(), "\"") % 2 == 1 || StringUtils.countMatches(context.getPost(), "\"") % 2 == 1){
            Quotes quotes = new Quotes();
            quotes.setInside(new Plain());
            return quotes;
        }
        
        if(StringUtils.countMatches(context.getPre(), "'") % 2 == 1 || StringUtils.countMatches(context.getPost(), "'") % 2 == 1){
            Apostrophe apostrophe = new Apostrophe();
            apostrophe.setInside(new Plain());
            return apostrophe;
        }
        
        return new Plain();
    }

}
