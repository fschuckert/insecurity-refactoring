/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.requirements.contextimpl;

import ntnuhtwg.insecurityrefactoring.base.context.contextimpl.ContextHTMLAttribute;
import ntnuhtwg.insecurityrefactoring.base.info.ContextInfo;
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
public class ContextHTMLAttributeTest {
    
    public ContextHTMLAttributeTest() {
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
     * Test of fullfillsRequirement method, of class ContextHTMLAttribute.
     */
    @Test
    public void testFullfillsRequirement() {
        ContextHTMLAttribute context = new ContextHTMLAttribute();
        
        assertTrue(context.fullfillsRequirement(new ContextInfo("xss", "<a ='", "'>")));
        assertTrue(context.fullfillsRequirement(new ContextInfo("xss", "<a =\"", "\">")));
        
        
        assertFalse(context.fullfillsRequirement(new ContextInfo("xss", "<a =\"www.url.com\">", "</a>")));
    }
    
    

}
