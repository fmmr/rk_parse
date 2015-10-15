package no.rodland.rkparse;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RKParser {

    private static final String PATH = "rk_data";
    private static final String IN_FILE_NAME = "cardioActivities.csv";
    private static final Path IN_PATH = FileSystems.getDefault().getPath(PATH, IN_FILE_NAME);
    private static final Path OUT_PATH = FileSystems.getDefault().getPath("rk_out.csv");

    public static void main(String[] args) throws IOException {
        final List<String> strings = dropLineBreaks(Files.readAllLines(IN_PATH));
        final List<String> result = strings.stream().map(RKParser::reformatLine).collect(Collectors.toList());
        Files.write(OUT_PATH, result);
    }

    /**
     * If there are line-breaks in the comment, field they are removed to simplify further parsing and import into Google sheets.
     */
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

    /**
     * Ensure duration and pace contains hour-, minute, and second-parts, and that each have leading 0-s. Also drop fraction of
     * calories, which are really not that interesting.
     */
    private static String reformatLine(String s) {
        final String[] split = s.split(",", -1);
        if (split.length > 5) {
            split[4] = getHHMMSS(split[4]);
            split[5] = getHHMMSS(split[5]);
            split[7] = dropFractional(split[7]);
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
                throw new IllegalArgumentException("only numbers but no colon (:) - hm - I don't know how to handle this: " + s);
            }
            return s;
        } else if (split.length == 2) {
            return "00:" + prefixZero(split[0]) + ":" + prefixZero(split[1]);


        } else if (split.length == 3) {
            return prefixZero(split[0]) + ":" + prefixZero(split[1]) + ":" + prefixZero(split[2]);

        }
        throw new IllegalArgumentException("colons in text but neither 1, 2 or 3 parts - hm - I don't know how to handle this: " + s);
    }

    private static String prefixZero(String s) {
        return s.length() == 1 ? "0" + s : s;
    }
}
