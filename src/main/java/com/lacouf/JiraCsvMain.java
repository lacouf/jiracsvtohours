package com.lacouf;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class JiraCsvMain {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage:");
            System.out.println("    java -jar JiraCSV.jar eq1 \"sprint 3\" \"21-10-2020 15:00\"");
            System.out.println("    The 3rd param (date) is optional");
            //System.out.println("args[0] " + args[0]);
            //System.out.println("args[1] " + args[1]);
            if (args.length == 3)
                System.out.println("args[2] " + args[2]);
            System.exit(0);
        }

        final Map<String, String> props = readProperties(args[0]);
        props.put("project", args[0]);
        props.put("sprint", args[1]);
        System.out.println(props);

        var dateFrom = args.length == 3 ? args[2] : "01-01-1900 00:00";
        new JiraReporter().printCsvReport(new JiraCloudConnector(props).getAllWorklogs(), dateFrom);
    }

    private static Map<String, String> readProperties(String prefix) {
        Map<String, String> maps = new FileResourcesUtils().getProperties("api_keys.properties");
        return maps.entrySet()
            .stream()
            .filter(e -> e.getKey().startsWith(prefix.toLowerCase()))
            .collect(toMap(
                e -> e.getKey().substring(e.getKey().indexOf(".") + 1),
                e -> e.getValue())
            );
    }
}
