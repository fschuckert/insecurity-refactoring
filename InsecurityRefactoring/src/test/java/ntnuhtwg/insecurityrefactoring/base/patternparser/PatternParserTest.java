/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.patternparser;

import java.io.IOException;
import java.util.List;
import ntnuhtwg.insecurityrefactoring.Framework;
import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.exception.TimeoutException;
import ntnuhtwg.insecurityrefactoring.base.patterns.PassthroughPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.ConcatPattern;
import ntnuhtwg.insecurityrefactoring.base.tree.TreeNode;
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
public class PatternParserTest {

    public PatternParserTest() {
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
     * Test of parseCode method, of class PatternParser.
     */
    @Test
    public void testParseCode() {
//        System.out.println("parseCode");
//        String code = "";
//        TreeNode<PatternEntry> expResult = null;
//        TreeNode<PatternEntry> result = PatternParser.parseCode(code);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of parseDataType method, of class PatternParser.
     */
    @Test
    public void testParseDataType() {
        System.out.println("parseDataType");

        assertEquals(DataType.String(), PatternParser.parseDataType("String"));
        assertEquals(DataType.Boolean(), PatternParser.parseDataType("Boolean"));
        assertEquals(DataType.Float(), PatternParser.parseDataType("Float"));

        assertEquals(DataType.Array().setArraySubType(DataType.String()), PatternParser.parseDataType("Array(String)"));
        assertEquals(DataType.Array().setArraySubType(DataType.Array().setArraySubType(DataType.String())), PatternParser.parseDataType("Array(Array(String))"));

    }

    @Test
    public void testEscaping() {
        {
            TreeNode<PatternEntry> tree = PatternParser.parsePatternCode("<s>(\\<)", "test");
            assertEquals("s", tree.getObj().identifier);
        }
//        {
//            TreeNode<PatternEntry> tree = PatternParser.parsePatternCode("<=>(%output, <con>(<con>(<s>(\\<img src=\"), %input), <s>(\"/\\>)))", "test");
//            assertEquals("s", tree.getObj().identifier);
//        }
    }

    @Test
    public void testPatternsParse() throws IOException, TimeoutException {
        Framework framework = new Framework();
        framework.init();
//        framework.connectDB();

//        INode node = framework.getDb().findNode(8L);
        for (ConcatPattern pattern : framework.getPatternStorage().getConcatPatterns()) {
            System.out.println("Testing: " + pattern);
            pattern.equalsPattern(null, null);

            System.err.println("Fine");
        }

//        for(PassthroughPattern pattern : framework.getPatternStorage().getPassthroughs()){
//            if(pattern.equalsPattern(node, framework.getDb())){
//                List<INode> inputs = pattern.findInputNodes(framework.getDSL(), node);
//                int i =10;
//            }
//            
//        }
        for (Pattern pattern : framework.getPatternStorage().getSources()) {
            System.out.println("Testing: " + pattern);
            pattern.equalsPattern(null, null);
            System.err.println("Fine");
        }

        for (Pattern pattern : framework.getPatternStorage().getSinks()) {
            System.out.println("Testing: " + pattern);
            pattern.equalsPattern(null, null);
            System.err.println("Fine");
        }

        for (Pattern pattern : framework.getPatternStorage().getSanitizations()) {
            System.out.println("Testing: " + pattern);
            pattern.equalsPattern(null, null);
            System.err.println("Fine");
        }

        for (Pattern pattern : framework.getPatternStorage().getDataflowIdentifies()) {
            System.out.println("Testing: " + pattern);
            pattern.equalsPattern(null, null);
            System.err.println("Fine");
        }

        for (Pattern pattern : framework.getPatternStorage().getDataflows()) {
            System.out.println("Testing: " + pattern);
            pattern.equalsPattern(null, null);
            System.err.println("Fine");
        }

        for (Pattern pattern : framework.getPatternStorage().getFailedSan()) {
            System.out.println("Testing: " + pattern);
            pattern.equalsPattern(null, null);
            System.err.println("Fine");
        }

        for (Pattern pattern : framework.getPatternStorage().getInsecureSources()) {
            System.out.println("Testing: " + pattern);
            pattern.equalsPattern(null, null);
            System.err.println("Fine");
        }

    }

}
