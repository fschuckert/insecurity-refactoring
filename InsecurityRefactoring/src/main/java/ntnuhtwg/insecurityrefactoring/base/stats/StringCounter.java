/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.analysis.CharArrayMap;

/**
 *
 * @author blubbomat
 */
public class StringCounter {
    private HashMap<String, Integer> counter = new HashMap<>();
    
    public void countString(String str){
        countString(str, 1);
    }
    
    public void countString(String str, int amount){
        if(!counter.containsKey(str)){
            counter.put(str, 0);           
        }
        
        counter.put(str, counter.get(str) + amount);
    }
    
    public void prettyPrint(String prePrint){
        int sum = 0;
        for(Map.Entry<String, Integer> entry : counter.entrySet()){
            System.out.println(prePrint + " " + entry.getKey() + ":" + entry.getValue());
            sum += entry.getValue();
        }
        System.out.println("Sum: " + sum);
            
    }
}
