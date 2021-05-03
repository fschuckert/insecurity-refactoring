/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.neo4j.driver.internal.value.IntegerValue;

/**
 *
 * @author blubbomat
 */
public class SourceLocation {
    private final String directory;    
    private final String file;
    private final Integer lineNumber;

    
    public SourceLocation(String unified){
        Integer lineNumber = -1;
        String directory = "";
        String file = "";
        directory = "";
        if(unified.contains(":")){
            try{                
                String numStr = unified.split(":")[1];
                unified = unified.split(":")[0];
                
                lineNumber = Integer.valueOf(numStr);
            } catch (Exception ex){
            }
        }
        
        if(unified.contains("/")){
            int lastIndex = unified.lastIndexOf("/");
            directory = unified.subSequence(0, lastIndex).toString();
            file = unified.subSequence(lastIndex+1, unified.length()).toString();
        }
        else{
            directory = "";
            file = unified;
        }
        
        this.directory = directory;
        this.file = file;
        this.lineNumber = lineNumber;
    }
    
    public SourceLocation(String directory, String file, Integer lineNumber) {
        this.directory = directory;
        this.file = file;
        this.lineNumber = lineNumber;
    }

    @Override
    public String toString() {
        return getPath() + ":" + lineNumber;
    }
    
    public String getPath(){
        return directory + "/" + file;
    }

    public String getDirectory() {
        return directory;
    }

    public String getFile() {
        return file;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }
    
    
    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.directory);
        hash = 59 * hash + Objects.hashCode(this.file);
        hash = 59 * hash + Objects.hashCode(this.lineNumber);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SourceLocation other = (SourceLocation) obj;
        if (!Objects.equals(this.directory, other.directory)) {
            return false;
        }
        if (!Objects.equals(this.file, other.file)) {
            return false;
        }
        if (!Objects.equals(this.lineNumber, other.lineNumber)) {
            return false;
        }
        return true;
    }
    
    public String codeSnippet(){
        return codeSnippet(false);
    }
    
    public String codeSnippet(boolean withPath){
        String codeSnippet = "";
        String path = directory + "/" + this.file;
        
        try (Stream<String> lines = Files.lines(Paths.get(directory + "/" + this.file))) {
            codeSnippet = withPath ? path +":" : "";
            codeSnippet +=  lineNumber+":"+ lines.skip(lineNumber - 1).findFirst().get();
        }catch (Exception ex) {
            codeSnippet = "Incorrect path: " + path;
        }
        return codeSnippet;
        
    }
    
    public String codeSnippet(long from, long until){
        String codeSnippet = "";
        String path = directory + "/" + this.file;
        
        try {
            List<String> allLines = Files.readAllLines(Paths.get(path));
            
            int i =0;
            for(String line : allLines){
                i++;
                if(i >= from && (i <=until || until == -1)){
                    codeSnippet += line + "\n";
                }
            }
            
        } catch (IOException ex) {
            Logger.getLogger(SourceLocation.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return codeSnippet;
    }
    
}
