package com.lacouf;

import com.opencsv.CSVReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class JiraCsv {
    static List<LogWorkEntry> logWorkEntries = new ArrayList<>();
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/LLL/yy h:mm a", Locale.US);

    public static void main(String[] args) throws Exception {
        LocalDateTime date = LocalDateTime.now();
        LocalDateTime newDate = LocalDateTime.parse(date.format(formatter), formatter);
        System.out.println(newDate.format(formatter));

        if (args[0].isEmpty()) {
            System.out.printf("Usage java -jar JiraCSV.jar file.csv");
            System.exit(0);
        }

        final Path path = Paths.get(args[0]);
        Reader reader = Files.newBufferedReader(path);

        CSVReader csvReader = new CSVReader(reader);
        List<String[]> list = new ArrayList<>();
        list = csvReader.readAll();
        reader.close();
        csvReader.close();
        Pair<Integer, Integer> logWork = startLogWork(list.get(0));
        list.stream()
            .forEach(line -> parseLine(line, logWork));
        logWorkEntries.stream()
                      .forEach(System.out::println);
    }

    private static void parseLine(String[] line, Pair<Integer, Integer> logWork) {

        for (int i = logWork.getLeft(); i < logWork.getLeft() + logWork.getRight(); i++) {
            final String[] split = rmlf(line[i]).split(";");
            if (split.length >= 4) {
                if (!split[0].isEmpty() && !split[1].isEmpty()) {
                    LogWorkEntry entry = new LogWorkEntry(line[1], line[0], split[2], split[0], split[1],
                            strToInt(split[3]));
                    logWorkEntries.add(entry);
                }
            }
        }

    }

    private static int strToInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static Pair<Integer, Integer> startLogWork(String[] line) {
        int i = 0;
        int count = 0;
        int startLogWork = 0;
        while (!line[i].equals("Log Work")) {
            i++;
        }
        ;
        startLogWork = i;
        while (line[i].equals("Log Work")) {
            count++;
            i++;
        }
        return Pair.of(startLogWork, count);
    }

    private static String rmlf(String stringToRemoveLf) {
        return StringUtils.replace(stringToRemoveLf, "\n", "");
    }
}
