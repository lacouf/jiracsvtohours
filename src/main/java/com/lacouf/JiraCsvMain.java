package com.lacouf;

import java.util.Map;
import java.util.stream.Collectors;

public class JiraCsvMain {

    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            System.out.println("Usage:");
            System.out.println("    java -jar JiraCSV.jar eq1 \"sprint 3\" \"21-10-2020 15:00\"");
            System.out.println("args[0] " + args[0]);
            System.out.println("args[1] " + args[1]);
            System.out.println("args[2] " + args[2]);
            System.out.println("args[3] " + args[3]);
            System.exit(0);
        }
        final Map<String, String> props = readProperties(args[1]);
        props.put("project", args[1]);
        props.put("sprint", args[2]);
        System.out.println(props);

        new JiraCloudConnector(props)
            .getAllIssuesAsync()
            .thenAccept(e -> new JiraReporter().printCsvReport(e,args[3]))
            .join();
//
//            JiraCsvParser parser = new JiraCsvParser();
//            List<LogWorkEntry> logWorkEntries = parser.parse(args[0]);
//            new JiraReporter().printCsvReport(logWorkEntries, args[1]);

    }

    private static Map<String, String> readProperties(String prefix) {
        Map<String, String> maps = new FileResourcesUtils().getProperties("api_keys.properties");
        return maps.entrySet()
            .stream()
            .filter(e -> e.getKey().startsWith(prefix))
            .collect(Collectors.toMap(
                e -> e.getKey().substring(e.getKey().indexOf(".") + 1),
                e -> e.getValue())
            );
    }
}
