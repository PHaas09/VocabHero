import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class QuizStatsStore {

    private Path baseDir = Paths.get("data");

    public void ensureUserFolder(String user) {
        try { Files.createDirectories(baseDir.resolve(user)); }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    private Path statsFile(String user) {
        return baseDir.resolve(user).resolve("quiz_stats.csv");
    }

    public void append(String user, String deckName, int total, int correct, int wrong, int timeout,
                       int secondsPerQuestion, double avgTimeSec) {
        ensureUserFolder(user);

        String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        double acc = (total == 0) ? 0 : (100.0 * correct / total);

        String line = ts + ";" + deckName + ";" + total + ";" + correct + ";" + wrong + ";" + timeout + ";"
                + secondsPerQuestion + ";" + String.format(Locale.ROOT, "%.1f", avgTimeSec) + ";"
                + String.format(Locale.ROOT, "%.1f", acc);

        try {
            Files.write(statsFile(user),
                    Arrays.asList(line),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String[]> load(String user) {
        ensureUserFolder(user);
        Path f = statsFile(user);

        List<String[]> out = new ArrayList<String[]>();
        if (!Files.exists(f)) return out;

        try {
            List<String> lines = Files.readAllLines(f, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.length() == 0) continue;
                String[] p = line.split(";");
                out.add(p);
            }
            return out;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
