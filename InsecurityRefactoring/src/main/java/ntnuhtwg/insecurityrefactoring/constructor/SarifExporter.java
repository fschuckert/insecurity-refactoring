/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.constructor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import ntnuhtwg.insecurityrefactoring.base.SourceLocation;
import ntnuhtwg.insecurityrefactoring.base.Util;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author blubbomat
 */
public class SarifExporter {
    
    
    String submissionDate = "2021-02-01 TODO";
    String author = "Felix Schuckert";
    String description = "TODO";
    boolean vulnerable;
    
    String vulnType = "xss"; // TODO
    
    
    String path = "/home/blubbomat/Development/Pattern_Gen_Sample/sample";
    
    SourceLocation sink;
//    Map<String, String> files = new HashMap<>();
    LinkedList<String> files = new LinkedList<>();

    public SarifExporter(String vulnType, String path, SourceLocation sink, boolean vulnerable) {
        // TODO
        //        files.put("src/sample.php", "<?php echo($a); ?>");
//        files.add("src/sample.php");
//        sink = new SourceLocation("src/sample.php:23");
        this.vulnType = vulnType;
        this.path = path;
        this.sink = sink;
        this.vulnerable = vulnerable;
    }
    
    
    public void addFile(String subPath){
        files.add(subPath);
    }
    
    private String cweId(){
        switch(vulnType){
            case"xss": return "79";
            case"sqli": return "89";
        }
        
        return "unknown vulnType: " + vulnType;
    }
    
    private String cweDescription(){
        switch(vulnType){
            case"xss": return "Improper Neutralization of Input During Web Page Generation ('Cross-site Scripting')";
            case"sqli": return "Improper Neutralization of Special Elements used in an SQL Command ('SQL Injection')";
        }
        
        return "unknown vulnType: " + vulnType;
    }
    
    

    public String export() {
        JSONObject base = new JSONObject();
        base.put("$schema", "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json");
        base.put("version", "2.1.0");

        JSONArray runs = new JSONArray();
        base.put("runs", runs);

        JSONObject run = new JSONObject();
        runs.add(run);

        JSONObject properties = new JSONObject();    
        run.put("properties", properties);
        properties.put("id", -1);
        properties.put("version", "1.0.0");
        properties.put("type", "source code");
        properties.put("status", "candidate");
        properties.put("submissionDate", submissionDate);
        properties.put("language", "php");
        properties.put("author", author);
        properties.put("description", description);
        properties.put("state", vulnerable ? "bad" : "good");
        
        JSONObject tool = createTool();
        run.put("tool", tool);
        
        JSONArray artifacts = createArtifacts();
        run.put("artifacts", artifacts);
        
        run.put("taxonomies", createTaxonomies());
        run.put("results", createResults());

        return format(base.toString());        
    }
    
    private JSONObject createTool(){
        JSONObject tool = new JSONObject();
        
        JSONObject driver = new JSONObject();
        tool.put("driver", driver);
        driver.put("name", "SARD - SAMATE");
        driver.put("fullName", "Software Assurance Reference Dataset Project");
        driver.put("informationUri", "https://samate.nist.gov/SARD/");
        driver.put("version", "5.0");
        driver.put("organization", "NIST");
        
        JSONArray supportedTaxonomies = new JSONArray();
        driver.put("supportedTaxonomies", supportedTaxonomies);
        
        JSONObject taxonomyCWE = new JSONObject();
        supportedTaxonomies.add(taxonomyCWE);
        taxonomyCWE.put("name", "CWE");
        taxonomyCWE.put("index", 0);
        
        
        return tool;
    }
    
    private String format(String unformated){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(unformated);
        return gson.toJson(je);
    }

    private JSONArray createArtifacts() {
        JSONArray artifacts = new JSONArray();
        
        for(String filePath : files){
            String fullPath = path + "/" + filePath;
            JSONObject artifact = new JSONObject();
            
            artifacts.add(artifact);
            artifact.put("length", new File(fullPath).length());
            artifact.put("sourceLanguage", "php");
            
            JSONObject location = new JSONObject();
            artifact.put("location", location);
            location.put("uri", filePath);
            
            JSONObject hashes = new JSONObject();
            artifact.put("hashes", hashes);
            hashes.put("sha-1", Util.sha1FromFile(fullPath));
            //TODO: add sha-1
            
        }
        
        
        return artifacts;
    }
    
   
    
    private JSONArray createTaxonomies(){
        JSONArray taxonomies = new JSONArray();
        
        JSONObject cwe = new JSONObject();
        taxonomies.add(cwe);
        
        cwe.put("name", "CWE");
        cwe.put("informationUri", "https://cwe.mitre.org/data/published/cwe_latest.pdf");
        cwe.put("downloadUri", "https://cwe.mitre.org/data/xml/cwec_latest.xml.zip");
        cwe.put("organization", "MITRE");
        
        JSONObject shortDescription = new JSONObject();
        shortDescription.put("text", "The MITRE Common Weakness Enumeration");
        cwe.put("shortDescription", shortDescription);
        
        cwe.put("isComprehensive", false);
        
        JSONArray taxa = new JSONArray();     
        cwe.put("taxa", taxa);
        
        JSONObject tax = new JSONObject();
        taxa.add(tax);
        tax.put("id", cweId());
        tax.put("name", cweDescription());
        
        
        return taxonomies;
    }
    
    
    private JSONArray createResults(){
        JSONArray results =new JSONArray();        
        results.add(result());
        return results;
    }

    private JSONObject result() {
        JSONObject result = new JSONObject();
        result.put("ruleId", "CWE-" + cweId());
        result.put("message", message());
        result.put("locations", locations());
        
        result.put("taxa", resultTaxa());
    
        return result;
    }
    
    private JSONArray resultTaxa(){
        JSONArray taxa = new JSONArray();
        
        JSONObject tax = new JSONObject();
        taxa.add(tax);        
        tax.put("id", cweId());
        tax.put("index", 0);
        
        JSONObject toolComponent = new JSONObject();
        tax.put("toolComponent", toolComponent);
        toolComponent.put("name", "CWE");
        toolComponent.put("index", 0);        
        
        return taxa;
    }

    private JSONObject message() {
        JSONObject message = new JSONObject();
        message.put("text", cweDescription());
        return message;
    }
    
    private JSONArray locations(){
        JSONArray locations = new JSONArray();
        
        locations.add(location());
        
        return locations;
    }
    
    private JSONObject location(){
        JSONObject location = new JSONObject();
        
        JSONObject physicalLocation = new JSONObject();
        location.put("physicalLocation", physicalLocation);
        
        JSONObject artifactLocation = new JSONObject();
        physicalLocation.put("artifactLocation", artifactLocation);
        artifactLocation.put("uri", sink.getPath());
        artifactLocation.put("index", 0);
        
        JSONObject region = new JSONObject();
        physicalLocation.put("region", region);
        region.put("startLine", sink.getLineNumber());
        
        
        
        return location;
    }
 
}
