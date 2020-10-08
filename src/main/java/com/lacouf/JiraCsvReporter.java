package com.lacouf;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class JiraCsvReporter {
    private Map<String, Double> hoursPerUser = new HashMap<>();

    public void printReport(List<LogWorkEntry> entries, String dateFrom) {
        LocalDate from = paresDate(dateFrom);
        final Map<String, List<LogWorkEntry>> result =
            entries.stream()
                   .filter(wl -> wl.getLogWorkDateTime().toLocalDate().isAfter(from))
                   .collect(
                       Collectors.groupingBy(
                           wl -> wl.getUserName()
                       )
                   );
        result.entrySet()
              .stream()
              .forEach(e -> printEntry(e));
    }

    private LocalDate paresDate(String dateFrom) {
        System.out.println("Date From: " + dateFrom);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.US);
        return LocalDate.parse(dateFrom, dtf);
    }

    private void printEntry(Map.Entry<String, List<LogWorkEntry>> e) {
        System.out.println(e.getKey());
        hoursPerUser.put(e.getKey(), 0.0);
        e.getValue()
         .stream()
         .forEach(log -> printEntryDetail(e.getKey(), log));
        System.out.println("total hours : " + hoursPerUser.get(e.getKey()));
        System.out.println();
    }

    private void printEntryDetail(String user, LogWorkEntry log) {
        final float hours = Float.valueOf(log.getLogWorkSeconds()) / 3600;
        System.out.println("\t" + log.getTaskId() + " "
            + log.getLogWorkDescription() + " "
            + log.getLogWorkDate() + " "
            + log.getUserTask() + " "
            + hours);
        double newHours = hoursPerUser.get(user) + hours;
        hoursPerUser.put(user, newHours);
    }
}
