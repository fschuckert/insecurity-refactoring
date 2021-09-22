/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.contextnew;

import ntnuhtwg.insecurityrefactoring.base.context.Context;
import ntnuhtwg.insecurityrefactoring.base.context.ContextAnalyzer;
import ntnuhtwg.insecurityrefactoring.base.context.Plain;
import ntnuhtwg.insecurityrefactoring.base.context.Htmlattribute;
import ntnuhtwg.insecurityrefactoring.base.context.Javascript;
import java.util.Collections;
import ntnuhtwg.insecurityrefactoring.base.context.enclosure.Apostrophe;
import ntnuhtwg.insecurityrefactoring.base.context.enclosure.Quotes;
import ntnuhtwg.insecurityrefactoring.base.info.ContextInfo;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SinkPattern;
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
public class ContextAnalyzerTest {

    public ContextAnalyzerTest() {
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
     * Test of analyze method, of class ContextAnalyzer.
     */
    @Test
    public void testAnalyze() {

        ContextAnalyzer analyzer = new ContextAnalyzer();

        {
            ContextInfo contextInfo = new ContextInfo("xss", Collections.EMPTY_SET, "<a onclick='alert(\"lala", "\")'>link</a>"); // should be: quotes -> htmlattribute(a, onclick, apostrope)
            Context context = analyzer.analyze(contextInfo);

            assertTrue(context instanceof Quotes);

            assertNotNull(context.getInside());
            assertTrue(context.getInside() instanceof Htmlattribute);

            Htmlattribute htmlattribute = (Htmlattribute) context.getInside();
            assertEquals("a", htmlattribute.getAttribute());
            assertEquals("onclick", htmlattribute.getParameter());
            assertTrue(htmlattribute.getEnclosure() instanceof Apostrophe);
        }

        {
            ContextInfo contextInfo = new ContextInfo("xss", Collections.EMPTY_SET, "<div> Hello '", "'</div>"); // should be: apostrophe -> plain
            Context context = analyzer.analyze(contextInfo);

            assertTrue(context instanceof Apostrophe);

            assertNotNull(context.getInside());
            assertTrue(context.getInside() instanceof Plain);
        }

        {
            ContextInfo contextInfo = new ContextInfo("xss", Collections.EMPTY_SET, "<div> Hello ", "</div>"); // should be: plain
            Context context = analyzer.analyze(contextInfo);

            assertTrue(context instanceof Plain);
        }

        {
            ContextInfo contextInfo = new ContextInfo("xss", Collections.EMPTY_SET, "", ""); // should be: plain
            Context context = analyzer.analyze(contextInfo);

            assertTrue(context instanceof Plain);
        }

        {
            //TODO: improve xss parser to solve the sample:
//            ContextInfo contextInfo = new ContextInfo("xss", "Hello 'how are u' input:", "blubb"); // should be: plain
//            Context context = analyzer.analyze(contextInfo);
//
//            assertTrue(context instanceof Plain);
        }
    }

    @Test
    public void testJavascript() {
        ContextInfo contextInfo = new ContextInfo("xss", Collections.EMPTY_SET, "<script>alert(\"Hello", "\");</script>"); // should be: quotes -> javascript
        Context context = ContextAnalyzer.analyze(contextInfo);

        assertTrue(context instanceof Quotes);

        assertNotNull(context.getInside());
        assertTrue(context.getInside() instanceof Javascript);
    }

}
