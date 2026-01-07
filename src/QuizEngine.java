import java.util.*;

public class QuizEngine {

    private List<Entry> list;
    private int index;

    private int correct;
    private int wrong;
    private int timeout;

    public QuizEngine(Deck deck) {
        list = new ArrayList<Entry>(deck.entries);
        Collections.shuffle(list);
        start();
    }

    public void start() {
        index = 0;
        correct = 0;
        wrong = 0;
        timeout = 0;
    }

    public int total() { return list.size(); }
    public int getIndex() { return index; }

    public Entry current() { return list.get(index); }

    public boolean check(String given) {
        String a = normalize(current().answer);
        String g = normalize(given);
        boolean ok = a.equals(g);
        if (ok) correct++; else wrong++;
        return ok;
    }

    public void markTimeout() {
        timeout++;
        wrong++;
    }

    public void markSkippedWrong() {
        wrong++;
    }

    public boolean next() {
        index++;
        return index < list.size();
    }

    public int getCorrect() { return correct; }
    public int getWrong() { return wrong; }
    public int getTimeout() { return timeout; }

    private String normalize(String s) {
        return s.trim().toLowerCase(Locale.ROOT);
    }
}
