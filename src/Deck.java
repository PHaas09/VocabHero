import java.util.*;

public class Deck {
    public String name;
    public DeckType type;

    // Labels:
    // QUIZ: label1=Frage-Sprache (z.B. English), label2=Antwort-Sprache (z.B. Deutsch)
    // HANGMAN: label1=Word-Label (z.B. Words), label2 unused
    public String label1;
    public String label2;

    // QUIZ entries:
    public List<Entry> entries;

    // HANGMAN words:
    public List<String> words;

    public Deck(String name, DeckType type) {
        this.name = name;
        this.type = type;
        this.entries = new ArrayList<Entry>();
        this.words = new ArrayList<String>();
        this.label1 = (type == DeckType.QUIZ) ? "Frage" : "Wort";
        this.label2 = (type == DeckType.QUIZ) ? "Antwort" : "";
    }
}
