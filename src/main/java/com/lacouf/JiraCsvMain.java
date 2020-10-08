package com.lacouf;

import java.util.List;

public class JiraCsvMain {

    public static void main(String[] args) throws Exception {

        if (args.length < 2) {
            System.out.printf("Usage java -jar JiraCSV.jar file.csv 02/05/20");
            System.exit(0);
        }

        JiraCsvParser parser = new JiraCsvParser();
        List<LogWorkEntry> logWorkEntries = parser.parse(args[0]);
        new JiraCsvReporter().printReport(logWorkEntries, args[1]);
    }
}
