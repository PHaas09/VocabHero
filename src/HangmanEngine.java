import java.util.*;

public class HangmanEngine {

    public enum Result { HIT, MISS, ALREADY, INVALID }

    private String original;
    private String upper;
    private int maxWrong;

    private Set<Character> guessed;
    private LinkedHashSet<Character> wrong;
    private int wrongCount;

    public HangmanEngine(String word, int maxWrong) {
        this.original = word;
        this.upper = word.toUpperCase(Locale.ROOT);
        this.maxWrong = maxWrong;

        guessed = new HashSet<Character>();
        wrong = new LinkedHashSet<Character>();
        wrongCount = 0;
    }

    public Result guess(char c) {
        if (!Character.isLetter(c)) return Result.INVALID;
        char ch = Character.toUpperCase(c);

        if (guessed.contains(ch) || wrong.contains(ch)) return Result.ALREADY;

        if (upper.indexOf(ch) >= 0) {
            guessed.add(ch);
            return Result.HIT;
        } else {
            wrong.add(ch);
            wrongCount++;
            return Result.MISS;
        }
    }

    public boolean isWon() {
        for (int i = 0; i < upper.length(); i++) {
            char ch = upper.charAt(i);
            if (Character.isLetter(ch) && !guessed.contains(ch)) return false;
        }
        return true;
    }

    public boolean isLost() {
        return wrongCount >= maxWrong;
    }

    public String maskedWithSpaces() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < original.length(); i++) {
            char orig = original.charAt(i);
            char up = Character.toUpperCase(orig);

            if (!Character.isLetter(orig)) sb.append(orig);
            else if (guessed.contains(up) || isLost()) sb.append(orig);
            else sb.append("_");

            if (i < original.length() - 1) sb.append(" ");
        }
        return sb.toString();
    }

    public String wrongLettersString() {
        if (wrong.isEmpty()) return "-";
        StringBuilder sb = new StringBuilder();
        Iterator<Character> it = wrong.iterator();
        while (it.hasNext()) {
            sb.append(it.next());
            if (it.hasNext()) sb.append(", ");
        }
        return sb.toString();
    }

    public int getWrongCount() { return wrongCount; }
    public int getMaxWrong() { return maxWrong; }
    public String getOriginal() { return original; }
}
