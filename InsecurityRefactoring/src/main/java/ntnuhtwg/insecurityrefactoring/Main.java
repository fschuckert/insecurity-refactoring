/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring;

import ntnuhtwg.insecurityrefactoring.base.info.DataflowPathInfo;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import ntnuhtwg.insecurityrefactoring.base.ASTType;
import ntnuhtwg.insecurityrefactoring.base.SourceLocation;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.info.ACIDTree;
import ntnuhtwg.insecurityrefactoring.base.stats.StringCounter;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
import ntnuhtwg.insecurityrefactoring.gui.GUI;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4JConnector;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4jDB;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.Neo4jEmbedded;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;
import ntnuhtwg.insecurityrefactoring.constructor.CodeSampleCreator;
import ntnuhtwg.insecurityrefactoring.gui.dockable.GuiDocking;
import ntnuhtwg.insecurityrefactoring.refactor.temppattern.ScanProgress;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

/**
 *
 * @author blubbomat
 */
public class Main {

    public static void main(String[] args) throws Exception {
        CommandLine cmd = null;
        try {
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options(), args);
        } catch (Exception ex) {
            printHelp();
            System.out.println("" + ex.getMessage());
            return;
        }

        Framework framework = new Framework();
        framework.init();

        if (!cmd.hasOption("g") && cmd.hasOption("generate_samples")) {
            String pathToGen = cmd.getOptionValue("generate_samples");
            CodeSampleCreator codeSampleCreator = new CodeSampleCreator(framework.getPatternStorage());
            if (cmd.hasOption("no_docker")) {
                codeSampleCreator.setCreateGenerateFiles(false);
            }
            if (cmd.hasOption("no_manifest")) {
                codeSampleCreator.setCreateManifest(false);
            }
            if (cmd.hasOption("split_by")) {
                String splitBy = cmd.getOptionValue("split_by");
                codeSampleCreator.setSplitBy(splitBy);
            }
//            options.addOption("genFiles", false, "This parameter is required to actually generate files. Otherwise just a dry run will count the sample size!");
//        options.addOption("onlyPattern", true, "Only generates with specific pattern. [patternType:patternName]. E.g. [dataflow:assignment]");

            if (cmd.hasOption("genFiles")) {
                codeSampleCreator.setOnlyCount(false);
            }

            if (cmd.hasOption("onlyPattern")) {
                for (String onlyPattern : cmd.getOptionValues("onlyPattern")) {
                    String[] splitted = onlyPattern.split(":");
                    if (splitted.length != 2) {
                        System.err.println("A only pattern parameter is incorrect formated It needs to be: patternType:patternName " + onlyPattern);
                        continue;
                    }

                    codeSampleCreator.addGenerateOnlyPattern(splitted[0], splitted[1]);
                }

            }

            codeSampleCreator.createAllPossibleSamples(pathToGen);
            return;
        }

        LinkedList<String> specificPattern = new LinkedList<>();
        boolean requiresSanitize = cmd.hasOption("r");

        if (cmd.hasOption("v")) {
            String specificPatternStr = cmd.getOptionValue("v");
            specificPattern.add(specificPatternStr);
        }

        if (cmd.hasOption("g")) {
            boolean development = cmd.hasOption("e");
            GuiDocking gui = new GuiDocking();
            gui.init(framework, development);
        } else if (cmd.hasOption("p")) {
            SourceLocation specificLocation = null;
            if (cmd.hasOption("specific-sink")) {
                String sinkPath = cmd.getOptionValue("specific-sink");
                specificLocation = new SourceLocation(sinkPath);
            }
            String path = cmd.getOptionValue("p");
            ScanProgress scanProgress = new ScanProgress();
            framework.scan(path, !cmd.hasOption("n"), specificPattern, scanProgress, cmd.hasOption("c"), specificLocation);

            List<ACIDTree> pipInformation = framework.getPipInformation();

            if (cmd.hasOption("o")) {
                for (ACIDTree pip : pipInformation) {
                    for (DataflowPathInfo pathInfo : pip.getPossibleSources()) {
                        pathInfo.getVulnerabilityInfo();
                        if (pip.isVulnSink() && pathInfo.getVulnerabilityInfo().isVulnerable()) {
                            System.out.println("\nVulnerability sink: " + pip.getSink().getObj() + "\n####################################################################################################################################");
                            Util.printSourceCodeRec(framework.getDb(), pathInfo.getSource(), null);
                            System.out.println("####################################################################################################################################\n");
                            break;
                        } else {
                            System.out.println("\nPIP sink: " + pip.getSink().getObj());
                            System.out.println("PIP location: " + Util.codeLocation(framework.getDb(), pip.getSink().getObj()));
                            System.out.println("####################################################################################################################################");
                            Util.printSourceCodeRec(framework.getDb(), pathInfo.getSource(), null);
                            System.out.println("####################################################################################################################################\n");
                        }
                    }
                }
            }

            StringCounter pipTypes = new StringCounter();
            StringCounter pipSpecific = new StringCounter();

            for (DFATreeNode pip : framework.getPips(requiresSanitize, false)) {
                String type = pip.getSinkPattern().getVulnType();
                pipTypes.countString(type);
                pipSpecific.countString("(" + type + ") " + pip.getSinkPattern().getName());

            }
            System.out.println("");
            System.out.println("##### Pip info #####");
            pipTypes.prettyPrint("PIP TYPE ");
            pipSpecific.prettyPrint("PIP SPECIFIC ");

//            int paths = 0;
//            int vulns = 0;
//            int timeouts = 0;
//            int vulnerabilities = 0;
//            for (ACIDTree pipInfo : pipInformation) {
//                boolean vulnerable = false;
//                for (DataflowPathInfo pathInfo : pipInfo.getPossibleSources()) {
//                    paths++;
//                    if (pathInfo.getVulnerabilityInfo().isVulnerable()) {
//                        vulns++;
//                        vulnerable = true;
//                    }
//                    if (pathInfo.isContainsTimeout()) {
//                        timeouts++;
//                    }
//                }
//                if (vulnerable) {
//                    vulnerabilities++;
//                }
//            }
//
//            System.out.println("");
//            System.out.println("##### Paths info #####");
//            System.out.println("Path Amount: " + paths);
//            System.out.println("Path Vulns: " + vulns);
//            System.out.println("Path Timeouts: " + timeouts);
//
//            System.out.println("");
//            System.out.println("##### Summary #####");
//            System.out.println("FOUND PIPs: " + framework.getPips(requiresSanitize, false).size());
//            System.out.println("Vulnerable: " + vulnerabilities);

           
        } else if (cmd.hasOption("s")) {
            String path = cmd.getOptionValue("s");
            framework.scanSubFolders(path, specificPattern, requiresSanitize, new ScanProgress(), cmd.hasOption("c"));

        } else if (cmd.hasOption("f")) {
            String path = cmd.getOptionValue("f");
            framework.scan(path, false, specificPattern, new ScanProgress(), cmd.hasOption("c"), null);
            framework.formatCode();
        } else {
            printHelp();
        }

        if (!cmd.hasOption("g")) {
            framework.closeDB();
            System.out.println("Finished closing db... should close now.");
        }

//        cmd.
    }

    private static void printHelp() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("Insecurity Refactoring", options());
    }

    private static Options options() {
        Options options = new Options();
        options.addOption("p", "path", true, "Path to analyse.");

        options.addOption("s", "subprojects", true, "Scan the subfolder and store results in txt files.");

        options.addOption("g", "gui", false, "Launch the User interface to view the results and do refactoring choices. Only works in combination with analyzing one project");
        options.addOption("e", "dev", false, "Gui will include development and experimental options");
        options.addOption("n", "skip-neo4j-prepare", false, "Skip the preparing of neo4jdb");

        options.addOption("v", "vuln", true, "Specific sink (category currently not supported)");
        options.addOption("r", "requires-san", false, "Specific sink (category currently not supported)");

        options.addOption("o", "output vulns", false, "Outsputs a vulnerable path for each vulnerability");
        options.addOption("f", "format", true, "Formats the folder");
        options.addOption("d", "details", false, "Print more details");

        options.addOption("c", "control-check", false, "Do a check for control functions. Takes a lot of time!");
        options.addOption("specificsink", false, "specific sink code location: provided like following path:linenumber");

        options.addOption("generate_samples", true, "Generate samples into the given folder. To generate the files the -genFiles parameter is required");
        options.addOption("no_docker", false, "Will not generate docker files");
        options.addOption("no_manifest", false, "Will not generate manifest files");
        options.addOption("split_by", true, "Split by pattern. [source|sanitize|dataflow|context|sink]");
        options.addOption("genFiles", false, "This parameter is required to actually generate files. Otherwise just a dry run will count the sample size!");
        options.addOption("onlyPattern", true, "Only generates with specific pattern. [patternType:patternName]. E.g. [dataflow:assignment]");

        return options;
    }
}
