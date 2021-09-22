/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.patternparser;

import ntnuhtwg.insecurityrefactoring.base.patternpersist.JsonPatternParser;
import java.io.File;
import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.exception.ParsingException;
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SanitizePattern;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author blubbomat
 */
public class JsonPatternParserTest {
    
    public JsonPatternParserTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of parseJsonPattern method, of class JsonPatternParser.
     */
    @Test
    public void testParseJsonPattern() throws Exception {
        List<Pattern> patterns = getPatterns("src/test/non-packaged-test-files/pattern/preg_replace.json");        
        assertEquals(3, patterns.size());
        
        for(Pattern pattern : patterns){
            
        }
    }
    
    @Test
    public void testFilterVar() throws Exception{
        List<Pattern> patterns = getPatterns("src/test/non-packaged-test-files/pattern/filter_var.json");  
        assertEquals(2, patterns.size());
//        for(Pattern pattern : patterns)
    }
    
    @Test
    public void testReplaceFilters() throws Exception{
        Pattern pattern = getFirstNonAnyPattern("src/test/non-packaged-test-files/pattern/preg_replace_filters.json");
        
        SanitizePattern san = (SanitizePattern)pattern;
        assertTrue(san.getCharsAllowed().isLetterAllowed());
        assertTrue(san.getCharsAllowed().isNumbersAllowed());
        assertTrue(san.getCharsAllowed().isSpecialAllowed());
    }

    /**
     * Test of main method, of class JsonPatternParser.
     */
    @Test
    public void testMain() {
    }

    /**
     * Test of replaceAnyWithParameters method, of class JsonPatternParser.
     */
    @Test
    public void testReplaceAnyWithParameters() throws Exception{
        Pattern pattern = getFirstNonAnyPattern("src/test/non-packaged-test-files/pattern/preg_match_all.json");
        
        String code = pattern.getCodeLines().get(0);
        assertEquals("<call>(preg_match_all,<s>(first), %input,<s>(second))", code);      
    }
    
    @Test
    public void testReplaceAnyWithParametersList() throws Exception{
        Pattern pattern = getFirstNonAnyPattern("src/test/non-packaged-test-files/pattern/replace_list.json");
        
        String code = pattern.getCodeLines().get(0);
        assertEquals("<s>(first,test,second,third)", code);      
    }
    
    @Test
    public void testReplaceAnyWithParametersOptional() throws Exception{
        Pattern pattern = getFirstNonAnyPattern("src/test/non-packaged-test-files/pattern/replace_optional.json");
        
        String code = pattern.getCodeLines().get(0);
        assertEquals("<s>(first,test)", code);      
    }
    
    @Test
    public void testReplaceAnyWithParametersOptionalFailed() throws Exception{
        try{
            Pattern pattern = getFirstNonAnyPattern("src/test/non-packaged-test-files/pattern/replace_optional_failed.json");
            fail("Should not be parsed!");
        } catch(ParsingException ex){
        }   
    }
    
    @Test
    public void testReplaceAnyWithParametersListFailed() throws Exception{
        try{
            Pattern pattern = getFirstNonAnyPattern("src/test/non-packaged-test-files/pattern/replace_list_failed.json");
            fail("Should not be parsed!");
        } catch(ParsingException ex){
        }   
    }
    
    private Pattern getPatternWithNameStartsWith(String path, String startsWith) throws Exception{
        for(Pattern pattern : getPatterns(path)){
            if(pattern.getName().startsWith(startsWith)){
                return pattern;
            }
        }
        
        return null;
    }
    
    private Pattern getFirstNonAnyPattern(String path) throws Exception{
        for(Pattern pattern : getPatterns(path)){
            if(!pattern.containsAny()){
                return pattern;
            }
        }
        
        return null;
    }
    
    

    private List<Pattern> getPatterns(String path) throws Exception {
        File preg_replace = new File(path);
        PatternStorage patternStorage = new PatternStorage();
        JsonPatternParser parser = new JsonPatternParser();
        List<Pattern> patterns = parser.parseJsonPattern(preg_replace, patternStorage);
        return patterns;
    }

    
}
