import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class DeckStore {

    private Path baseDir = Paths.get("data");

    public void ensureUserFolder(String user) {
        try {
            Files.createDirectories(baseDir.resolve(user));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteUserFolder(String user) {
        try {
            Path dir = baseDir.resolve(user);
            if (!Files.exists(dir)) return true;

            DirectoryStream<Path> ds = Files.newDirectoryStream(dir);
            try {
                for (Path p : ds) Files.deleteIfExists(p);
            } finally {
                ds.close();
            }
            Files.deleteIfExists(dir);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public List<String> listDecks(String user, DeckType type) throws IOException {
        ensureUserFolder(user);
        Path dir = baseDir.resolve(user);

        List<String> decks = new ArrayList<String>();
        if (!Files.exists(dir)) return decks;

        DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.txt");
        try {
            for (Path p : ds) {
                String fn = p.getFileName().toString();
                if (!fn.endsWith(".txt")) continue;

                // Header check (Type)
                DeckType t = readTypeFromFile(p);
                if (t == type) {
                    decks.add(fn.substring(0, fn.length() - 4));
                }
            }
        } finally {
            ds.close();
        }

        Collections.sort(decks, String.CASE_INSENSITIVE_ORDER);
        return decks;
    }

    public boolean createDeck(String user, String name, DeckType type, String label1, String label2) throws IOException {
        ensureUserFolder(user);
        Path f = baseDir.resolve(user).resolve(safe(name) + ".txt");
        if (Files.exists(f)) return false;

        List<String> lines = new ArrayList<String>();
        if (type == DeckType.QUIZ) {
            lines.add("#TYPE=QUIZ;L1=" + escape(label1) + ";L2=" + escape(label2));
        } else {
            lines.add("#TYPE=HANGMAN;L1=" + escape(label1));
        }
        Files.write(f, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        return true;
    }

    public void deleteDeck(String user, String name) throws IOException {
        Path f = baseDir.resolve(user).resolve(safe(name) + ".txt");
        Files.deleteIfExists(f);
    }

    public Deck loadDeck(String user, String name) throws IOException {
        Path f = baseDir.resolve(user).resolve(safe(name) + ".txt");

        // Fallback falls Datei fehlt:
        if (!Files.exists(f)) {
            Deck d = new Deck(name, DeckType.QUIZ);
            d.label1 = "Frage";
            d.label2 = "Antwort";
            return d;
        }

        List<String> lines = Files.readAllLines(f, StandardCharsets.UTF_8);
        if (lines.size() == 0) {
            Deck d = new Deck(name, DeckType.QUIZ);
            return d;
        }

        String header = lines.get(0).trim();
        Deck deck;

        if (header.startsWith("#TYPE=HANGMAN")) {
            deck = new Deck(name, DeckType.HANGMAN);
            deck.label1 = parseValue(header, "L1", "Wort");
        } else {
            // Default QUIZ
            deck = new Deck(name, DeckType.QUIZ);
            deck.label1 = parseValue(header, "L1", "Frage");
            deck.label2 = parseValue(header, "L2", "Antwort");
        }

        // Ab Zeile 1 Inhalte
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().length() == 0) continue;

            if (deck.type == DeckType.QUIZ) {
                String[] p = line.split("\t", 2);
                if (p.length == 2) deck.entries.add(new Entry(p[0], p[1]));
            } else {
                deck.words.add(line.trim());
            }
        }

        return deck;
    }

    public void saveDeck(String user, Deck deck) throws IOException {
        Path f = baseDir.resolve(user).resolve(safe(deck.name) + ".txt");

        List<String> lines = new ArrayList<String>();
        if (deck.type == DeckType.QUIZ) {
            lines.add("#TYPE=QUIZ;L1=" + escape(deck.label1) + ";L2=" + escape(deck.label2));
            for (int i = 0; i < deck.entries.size(); i++) {
                Entry e = deck.entries.get(i);
                lines.add(e.question + "\t" + e.answer);
            }
        } else {
            lines.add("#TYPE=HANGMAN;L1=" + escape(deck.label1));
            for (int i = 0; i < deck.words.size(); i++) {
                lines.add(deck.words.get(i));
            }
        }

        Files.write(f, lines, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private DeckType readTypeFromFile(Path file) {
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            if (lines.size() == 0) return DeckType.QUIZ;
            String h = lines.get(0).trim();
            if (h.startsWith("#TYPE=HANGMAN")) return DeckType.HANGMAN;
            return DeckType.QUIZ;
        } catch (IOException e) {
            return DeckType.QUIZ;
        }
    }

    private String parseValue(String header, String key, String def) {
        // header: #TYPE=QUIZ;L1=English;L2=Deutsch
        String[] parts = header.split(";");
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i].trim();
            if (p.startsWith(key + "=")) {
                return unescape(p.substring((key + "=").length()));
            }
        }
        return def;
    }

    private String safe(String name) {
        return name.trim().replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private String escape(String s) {
        return s.replace(";", "\\;").replace("=", "\\=");
    }

    private String unescape(String s) {
        return s.replace("\\;", ";").replace("\\=", "=");
    }
}
