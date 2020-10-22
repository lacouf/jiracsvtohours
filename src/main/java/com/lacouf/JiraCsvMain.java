package com.lacouf;

import java.util.List;

public class JiraCsvMain {

    public static void main(String[] args) throws Exception {
        if (args.length != 0 && args.length != 2) {
            System.out.println("Usage:");
            System.out.println("  From REST API:");
            System.out.println("    java -jar JiraCSV.jar");
            System.out.println("  With CSV file:");
            System.out.println("    java -jar JiraCSV.jar file.csv 21-10-2020 15:00");
            System.exit(0);
        }

        if (args.length == 0) {
            new JiraCloudConnector().getAllIssuesAsync().thenAccept(new JiraReporter()::printRestReport).join();
        } else {
            JiraCsvParser parser = new JiraCsvParser();
            List<LogWorkEntry> logWorkEntries = parser.parse(args[0]);
            new JiraReporter().printCsvReport(logWorkEntries, args[1]);
        }
    }
}
