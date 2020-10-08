package com.lacouf;

import com.opencsv.CSVReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JiraCsvParser {
    private List<LogWorkEntry> logWorkEntries = new ArrayList<>();

    public List<LogWorkEntry> parse(String arg) throws Exception {
        final Path path = Paths.get(arg);
        Reader reader = Files.newBufferedReader(path);

        CSVReader csvReader = new CSVReader(reader);
        List<String[]> list = new ArrayList<>();
        list = csvReader.readAll();
        reader.close();
        csvReader.close();
        Pair<Integer, Integer> logWork = startLogWork(list.get(0));
        list.stream()
            .forEach(line -> parseLine(line, logWork));
        return logWorkEntries;
    }

    private void parseLine(String[] line, Pair<Integer, Integer> logWork) {

        for (int i = logWork.getLeft(); i < logWork.getLeft() + logWork.getRight(); i++) {
            final String[] split = rmlf(line[i]).split(";");
            if (split.length >= 4) {
                if (!split[1].isEmpty()) {
                    LogWorkEntry entry = new LogWorkEntry(line[1], line[0], split[2], split[0], split[1],
                            strToInt(split[3]));
                    logWorkEntries.add(entry);
                }
            }
        }
    }

    private int strToInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Pair<Integer, Integer> startLogWork(String[] line) {
        int i = 0;
        int count = 0;
        int startLogWork = 0;
        while (line.length > 0 && !line[i].equals("Log Work") && i < line.length) {
            i++;
        };

        startLogWork = i;
        while (line[i].equals("Log Work")) {
            count++;
            i++;
        }
        return Pair.of(startLogWork, count);
    }

    private String rmlf(String stringToRemoveLf) {
        return StringUtils.replace(stringToRemoveLf, "\n", "");
    }
}
