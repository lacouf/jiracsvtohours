package com.lacouf;

import lombok.SneakyThrows;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class JiraReporter {
    private final Map<String, Double> hoursPerUser = new HashMap<>();

    @SneakyThrows
    public void printRestReport(List<LogWorkEntry> entries) {
        printCsvReport(entries, JiraConfig.START_DATE);
    }

    public void printCsvReport(List<LogWorkEntry> entries, String dateFrom) {
        LocalDate from = parseDate(dateFrom);
        final Map<String, List<LogWorkEntry>> result =
                entries.stream()
                        .filter(wl -> wl.getLogWorkDateTime().toLocalDate().isAfter(from))
                        .collect(Collectors.groupingBy(LogWorkEntry::getUserName));

        result.entrySet().forEach(this::printEntry);
    }

    private LocalDate parseDate(String dateFrom) {
        System.out.println("Date From: " + dateFrom);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.US);
        return LocalDate.parse(dateFrom, dtf);
    }

    private void printEntry(Map.Entry<String, List<LogWorkEntry>> e) {
        System.out.println(e.getKey());
        hoursPerUser.put(e.getKey(), 0.0);
        e.getValue().forEach(log -> printEntryDetail(e.getKey(), log));
        System.out.println("total hours : " + hoursPerUser.get(e.getKey()));
        System.out.println();
    }

    private void printEntryDetail(String user, LogWorkEntry log) {
        final float hours = (float) log.getLogWorkSeconds() / 3600;
        System.out.println("\t" + log.getTaskId() + " "
                + log.getLogWorkDescription() + " "
                + log.getLogWorkDate() + " "
                + log.getUserTask() + " "
                + hours);
        double newHours = hoursPerUser.get(user) + hours;
        hoursPerUser.put(user, newHours);
    }
}
