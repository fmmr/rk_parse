package no.rodland.rkparse;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RKParser {

    public static void main(String[] args) throws IOException {
        Path inFile = FileSystems.getDefault().getPath("rk_data", "cardioActivities.csv");
        Path outFile = FileSystems.getDefault().getPath("rk_out.csv");
        final List<String> strings = dropLineBreaks(Files.readAllLines(inFile));
        final List<String> result = strings.stream().map(RKParser::reformatLine).collect(Collectors.toList());
        Files.write(outFile, result);
    }

    private static List<String> dropLineBreaks(List<String> lines) {
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            final String[] split = line.split(",", -1);
            if (split.length > 9) {
                result.add(line);
            } else {
                result.set(result.size() - 1, result.get(result.size() - 1) + "   -   " + line);
            }
        }
        return result;
    }

    private static String reformatLine(String s) {
        final String[] split = s.split(",", -1);

        if (split.length > 5) {
            split[4] = getHHMMSS(split[4]);
            split[5] = getHHMMSS(split[5]);
            split[7] = dropFractional(split[7]);
//            return split[4];
            return String.join(",", split);
        }
        return s;
    }

    private static String dropFractional(String s) {
        final String[] split = s.split("\\.");
        if (split.length == 2) {
            return split[0];
        }
        return s;
    }

    private static String getHHMMSS(String s) {
        final String[] split = s.split(":");
        if (split.length == 1) {
            if (split[0].matches("^[0-9]+$")) {
                throw new IllegalArgumentException("hmmm: " + s);
            }
            return s;
        } else if (split.length == 2) {
            return "00:" + prefixZero(split[0]) + ":" + prefixZero(split[1]);


        } else if (split.length == 3) {
            return prefixZero(split[0]) + ":" + prefixZero(split[1]) + ":" + prefixZero(split[2]);

        }
        throw new IllegalArgumentException("hmmm2: " + s);
    }

    private static String prefixZero(String s) {
        return s.length() == 1 ? "0" + s : s;
    }
}
