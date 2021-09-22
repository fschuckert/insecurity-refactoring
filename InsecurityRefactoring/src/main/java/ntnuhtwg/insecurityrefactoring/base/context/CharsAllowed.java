/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.context;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;
import scala.NotImplementedError;

/**
 *
 * @author blubbomat
 */
public class CharsAllowed {

    protected boolean numbers = true;
    protected boolean letter = true;
    protected boolean special = true;

    protected Set<Character> filteredSymbols = new HashSet<Character>();
    protected Map<EscapeChar, Set<Character>> escapeChars = new HashMap<>();

    

    public void mergeFromAnother(CharsAllowed other) {
        numbers = numbers && other.numbers;
        letter = letter && other.letter;
        special = special && other.special;
        
        filteredSymbols.addAll(other.filteredSymbols);
        for(Entry<EscapeChar, Set<Character>> otherEscapeChars : other.escapeChars.entrySet()){
            if(!escapeChars.containsKey(otherEscapeChars.getKey())){
                escapeChars.put(otherEscapeChars.getKey(), new HashSet<>());
            }
            escapeChars.get(otherEscapeChars.getKey()).addAll(otherEscapeChars.getValue());
        }
    }
    
    public void addFiltersOut(Character chr) {
        filteredSymbols.add(chr);
    }

    public boolean areCharsAllowed(Set<EscapeChar> sufficientEscapes, char... chars) {
        for (char c : chars) {
            if (!isCharAllowed(sufficientEscapes, c)) {
                return false;
            }
        }

        return true;
    }

    public boolean isCharAllowed(Set<EscapeChar> sufficientEscapes, char c) {
        if (Character.isLetter(c)) {
            return letter;
        }
        if (Character.isDigit(c)) {
            return numbers;
        }

        for (EscapeChar sufficientEscape : sufficientEscapes) {
            if (!escapeChars.containsKey(sufficientEscape)) {
                continue;
            }

            Set<Character> escapedCharsForSufficient = escapeChars.get(sufficientEscape);

            // checks if it can be escaped with the escape character itself
            if (!sufficientEscape.isDoubleEscape() && !escapedCharsForSufficient.contains(sufficientEscape.getEscapeChar())) {
                continue;
            }

            if (escapedCharsForSufficient.contains(c)) {
                return false;
            }
        }

        return special && !filteredSymbols.contains(c);
    }

    public void addEscape(EscapeChar escapeId, char[] escaped) {
        for (Character chr : escaped) {
            addEscape(escapeId, chr);
        }
    }

    public void addEscape(EscapeChar escapeId, Character escaped) {
        if (!escapeChars.containsKey(escapeId)) {
            escapeChars.put(escapeId, new HashSet<>());
        }

        escapeChars.get(escapeId).add(escaped);
    }

    public void setNumbers(boolean numbers) {
        this.numbers = numbers;
    }

    public void setLetter(boolean letter) {
        this.letter = letter;
    }

    public void setSpecial(boolean special) {
        this.special = special;
    }

    public boolean isNumbersAllowed() {
        return numbers;
    }

    public boolean isLetterAllowed() {
        return letter;
    }

    public boolean isSpecialAllowed() {
        return special;
    }

    public void allowedNumbers(String allowedChars) {
        for (int i = 0; i < 256; i++) {
            char c = (char) i;

            if (!Character.isDigit(c)) {
                continue;
            }

            addToFilteredSymbol(c, allowedChars);
        }
    }

    public void allowedLetters(String allowedChars) {
        for (int i = 0; i < 256; i++) {
            char c = (char) i;

            if (!Character.isLetter(c)) {
                continue;
            }

            addToFilteredSymbol(c, allowedChars);
        }
    }

    private void addToFilteredSymbol(char c, String allowedChars) {
        String chr = "" + c;

        if (!allowedChars.contains(chr)) {
            filteredSymbols.add(c);
        }
    }

    public void allowedSpecials(String allowedChars) {
        for (int i = 0; i < 256; i++) {
            char c = (char) i;

            if (Character.isLetterOrDigit(c)) {
                continue;
            }

            addToFilteredSymbol(c, allowedChars);
        }
    }

//    public static void main(String[] args){
//        CharsAllowed charsAllowed = new CharsAllowed();
//        charsAllowed
//    }
    public String prettyPrint() {
        StringJoiner joiner = new StringJoiner(", ");
        if (!numbers) {
            joiner.add("nums");
        }
        if (!letter) {
            joiner.add("letters");
        }
        if (!special) {
            joiner.add("specials");
        }

        if (!filteredSymbols.isEmpty()) {
            StringJoiner filteredJ = new StringJoiner(", ");
            for (Character chr : filteredSymbols) {
                filteredJ.add(chr.toString());
            }

            joiner.add("Filtered(" + filteredJ.toString() + ")");
        }

        escapeChars.forEach((escapeChar, characters) -> {
            StringJoiner chrJoiner = new StringJoiner(", ");
            for (Character chr : characters) {
                chrJoiner.add(chr.toString());
            }

            joiner.add("Escape[" + escapeChar + "](" + chrJoiner.toString() + ")");
        });

        return "Filters:[" + joiner.toString() + "]";
    }

}
