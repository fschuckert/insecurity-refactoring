/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.contextnew;

import ntnuhtwg.insecurityrefactoring.base.context.EscapeChar;
import ntnuhtwg.insecurityrefactoring.base.context.CharsAllowed;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
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
public class CharsAllowedTest {
    
    CharsAllowed charsAllowed;
    
    public CharsAllowedTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        charsAllowed = new CharsAllowed();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of addFiltersOut method, of class CharsAllowed.
     */
    @Test
    public void testAddFiltersOut() {
    }

    /**
     * Test of areCharsAllowed method, of class CharsAllowed.
     */
    @Test
    public void testAreCharsAllowed() {
    }

    /**
     * Test of isCharAllowed method, of class CharsAllowed.
     */
    @Test
    public void testIsCharAllowed() {
    }

    /**
     * Test of setNumbers method, of class CharsAllowed.
     */
    @Test
    public void testSetNumbers() {
    }

    /**
     * Test of setLetter method, of class CharsAllowed.
     */
    @Test
    public void testSetLetter() {
    }

    /**
     * Test of setSpecial method, of class CharsAllowed.
     */
    @Test
    public void testSetSpecial() {
    }

    /**
     * Test of isNumbersAllowed method, of class CharsAllowed.
     */
    @Test
    public void testIsNumbersAllowed() {
    }

    /**
     * Test of isLetterAllowed method, of class CharsAllowed.
     */
    @Test
    public void testIsLetterAllowed() {
    }

    /**
     * Test of isSpecialAllowed method, of class CharsAllowed.
     */
    @Test
    public void testIsSpecialAllowed() {
    }

    /**
     * Test of allowedSpecials method, of class CharsAllowed.
     */
    @Test
    public void testAllowedSpecials() {
        charsAllowed.allowedSpecials("[]ab;");
        
        assertTrue(charsAllowed.isCharAllowed(Collections.EMPTY_SET, '['));
        assertTrue(charsAllowed.isCharAllowed(Collections.EMPTY_SET, ']'));
        assertTrue(charsAllowed.isCharAllowed(Collections.EMPTY_SET, ';'));
        assertTrue(charsAllowed.isCharAllowed(Collections.EMPTY_SET, 'd'));
        assertTrue(charsAllowed.isCharAllowed(Collections.EMPTY_SET, 'f'));
        assertTrue(charsAllowed.isCharAllowed(Collections.EMPTY_SET, '9'));
        
        assertFalse(charsAllowed.isCharAllowed(Collections.EMPTY_SET, '<'));
        assertFalse(charsAllowed.isCharAllowed(Collections.EMPTY_SET, '>'));
        assertFalse(charsAllowed.isCharAllowed(Collections.EMPTY_SET, ':'));
    }
    
    @Test
    public void testEscapedSpecials() {
        charsAllowed.addEscape(new EscapeChar('\\'), '<');
        charsAllowed.addEscape(new EscapeChar('\\'), '\'');
        charsAllowed.addEscape(new EscapeChar('\\'), '\\'); // required to not escaping the escaping
        
        Set<EscapeChar> sufficientEscapes = new HashSet<>();
        sufficientEscapes.add(new EscapeChar('\\'));
        
        assertTrue(charsAllowed.isCharAllowed(sufficientEscapes, '['));
        assertTrue(charsAllowed.isCharAllowed(sufficientEscapes, 'f'));
        
        assertFalse(charsAllowed.isCharAllowed(sufficientEscapes, '<'));
        assertFalse(charsAllowed.isCharAllowed(sufficientEscapes, '\''));
    }
    
    @Test
    public void testEscapedSpecialsDouble() {
        charsAllowed.addEscape(new EscapeChar(null), '<');
        charsAllowed.addEscape(new EscapeChar(null), '\'');
        
        Set<EscapeChar> sufficientEscapes = new HashSet<>();
        sufficientEscapes.add(new EscapeChar(null));
        
        assertTrue(charsAllowed.isCharAllowed(sufficientEscapes, '['));
        assertTrue(charsAllowed.isCharAllowed(sufficientEscapes, 'f'));
        
        assertFalse(charsAllowed.isCharAllowed(sufficientEscapes, '<'));
        assertFalse(charsAllowed.isCharAllowed(sufficientEscapes, '\''));
    }
    
    @Test
    public void testEscapedSpecialsWithoutSelfEscape() {
        charsAllowed.addEscape(new EscapeChar('\\'), '<');
        charsAllowed.addEscape(new EscapeChar('\\'), '\'');
        
        Set<EscapeChar> sufficientEscapes = new HashSet<>();
        sufficientEscapes.add(new EscapeChar('\\'));
        
        assertTrue(charsAllowed.isCharAllowed(sufficientEscapes, '['));
        assertTrue(charsAllowed.isCharAllowed(sufficientEscapes, 'f'));
        
        assertTrue(charsAllowed.isCharAllowed(sufficientEscapes, '<'));
        assertTrue(charsAllowed.isCharAllowed(sufficientEscapes, '\''));
    }
    
}
