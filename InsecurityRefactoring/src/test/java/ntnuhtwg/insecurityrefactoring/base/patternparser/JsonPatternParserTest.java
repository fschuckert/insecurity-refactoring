/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.patternparser;

import java.io.File;
import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.context.contextimpl.ContextApostroph;
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.sufficient.Sufficient;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.sufficient.VulnSufficient;
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
        File preg_replace = new File("src/test/non-packaged-test-files/pattern/preg_replace.json");
        PatternStorage patternStorage = new PatternStorage();
        
        JsonPatternParser parser = new JsonPatternParser();
        List<Pattern> patterns = parser.parseJsonPattern(preg_replace, patternStorage);
        
        assertEquals(3, patterns.size());
        
        for(Pattern pattern : patterns){
            if(pattern.getName().endsWith("has_requirements")){
                Sufficient suff = (Sufficient)pattern;
                assertEquals(2, suff.getSufficients().size());
                for(VulnSufficient vulnSuff : suff.getSufficients()){
                    if("xss".equals(vulnSuff.getVulnType())){
                        assertEquals(1, vulnSuff.getRequirements().size());
                        
                        assertEquals(2, vulnSuff.getRequirements().get(0).asList().size());
                    }
                }
            }
        }
    }

    /**
     * Test of main method, of class JsonPatternParser.
     */
    @Test
    public void testMain() {
    }
    
}
