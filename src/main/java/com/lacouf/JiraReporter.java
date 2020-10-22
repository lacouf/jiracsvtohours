package com.lacouf;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class JiraReporter {
    private final Map<String, Integer> secondsPerUser = new HashMap<>();

    @SneakyThrows
    public void printRestReport(List<LogWorkEntry> entries) {
        printCsvReport(entries, JiraConfig.START_DATE_TIME);
    }

    public void printCsvReport(List<LogWorkEntry> entries, String dateFrom) {
        LocalDateTime from = parseDate(dateFrom);
        final Map<String, List<LogWorkEntry>> result =
                entries.stream()
                        .filter(wl -> wl.getLogWorkDateTime().isAfter(from))
                        .collect(Collectors.groupingBy(LogWorkEntry::getUserName));

        result.forEach((username, list) -> list.sort(Comparator.comparing(LogWorkEntry::getLogWorkDateTime)));

        result.entrySet().forEach(this::printEntry);
    }

    private LocalDateTime parseDate(String dateFrom) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.US);
        var date = LocalDateTime.parse(dateFrom, dtf);
        System.out.println("Date From: " + date);
        return date;
    }

    private void printEntry(Map.Entry<String, List<LogWorkEntry>> e) {
        System.out.println(e.getKey());
        secondsPerUser.put(e.getKey(), 0);
        e.getValue().forEach(log -> printEntryDetail(e.getKey(), log));

        System.out.println("Total time : " + prettyPrintTime(secondsPerUser.get(e.getKey())));
        System.out.println();
    }

    private void printEntryDetail(String user, LogWorkEntry log) {
        final int seconds = log.getLogWorkSeconds();
        System.out.println("\t" + StringUtils.rightPad(log.getTaskId(), 10)
                + StringUtils.rightPad(prettyPrintTime(seconds), 8)
                + log.getLogWorkDate() + " "
                + StringUtils.rightPad(log.getUserTask(), 120)
                + log.getLogWorkDescription()
        );
        int newSeconds = secondsPerUser.get(user) + seconds;
        secondsPerUser.put(user, newSeconds);
    }

    private String prettyPrintTime(int seconds) {
        return Duration.ofSeconds(seconds)
                .toString()
                .substring(2)
                .toLowerCase()
                .replaceAll(".[wdhm]", "$0 ");
    }
}
