import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class HangmanPanel extends JPanel {

    private AppFrame app;

    JComboBox<String> cmbDeck;
    JLabel lblWord;
    JLabel lblWrong;
    JLabel lblStatus;

    JTextField txtChar;
    JButton btnGuess;
    JButton btnNew;
    JButton btnBack;

    private HangmanEngine engine;
    private Deck loadedDeck;

    public HangmanPanel(AppFrame app) {
        this.app = app;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Hangman");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        add(title, BorderLayout.NORTH);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.add(new JLabel("Kartei:"));
        cmbDeck = new JComboBox<String>();
        cmbDeck.setPreferredSize(new Dimension(250, 28));
        top.add(cmbDeck);
        add(top, BorderLayout.PAGE_START);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createTitledBorder("Spiel"));

        lblWord = new JLabel("Wähle eine Kartei …");
        lblWord.setFont(lblWord.getFont().deriveFont(Font.BOLD, 28f));
        lblWord.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblWrong = new JLabel("Falsche Buchstaben: -");
        lblWrong.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(new JLabel("Buchstabe:"));
        txtChar = new JTextField(3);
        row.add(txtChar);
        btnGuess = new JButton("Raten");
        row.add(btnGuess);

        lblStatus = new JLabel(" ");
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        center.add(lblWord);
        center.add(Box.createVerticalStrut(10));
        center.add(lblWrong);
        center.add(Box.createVerticalStrut(10));
        center.add(row);
        center.add(Box.createVerticalStrut(10));
        center.add(lblStatus);

        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnNew = new JButton("Neue Runde");
        btnBack = new JButton("Zurück");
        bottom.add(btnNew);
        bottom.add(btnBack);
        add(bottom, BorderLayout.SOUTH);

        cmbDeck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { startRound(); }
        });
        btnNew.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { startRound(); }
        });
        btnGuess.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { guess(); }
        });
        txtChar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { guess(); }
        });
        btnBack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { app.showMenu(); }
        });

        reset();
    }

    public void reset() {
        cmbDeck.removeAllItems();
        lblWord.setText("Wähle eine Kartei …");
        lblWrong.setText("Falsche Buchstaben: -");
        lblStatus.setText(" ");
        txtChar.setText("");
        engine = null;
        loadedDeck = null;
    }

    public void reloadDecks() {
        cmbDeck.removeAllItems();
        try {
            java.util.List<String> decks = app.getDeckStore().listDecks(app.getCurrentUser(), DeckType.HANGMAN);
            for (int i = 0; i < decks.size(); i++) cmbDeck.addItem(decks.get(i));
            if (decks.size() > 0) cmbDeck.setSelectedIndex(0);
            startRound();
        } catch (Exception ex) {
            lblStatus.setText("Fehler: " + ex.getMessage());
        }
    }

    private void startRound() {
        String deckName = (String) cmbDeck.getSelectedItem();
        if (deckName == null) {
            engine = null;
            lblWord.setText("Keine Hangman-Kartei vorhanden.");
            return;
        }

        try {
            loadedDeck = app.getDeckStore().loadDeck(app.getCurrentUser(), deckName);
            if (loadedDeck.type != DeckType.HANGMAN) {
                lblWord.setText("Diese Kartei ist keine Hangman-Kartei.");
                return;
            }

            if (loadedDeck.words.size() == 0) {
                engine = null;
                lblWord.setText("Diese Kartei ist leer.");
                return;
            }

            String word = loadedDeck.words.get(new java.util.Random().nextInt(loadedDeck.words.size()));
            word = word.replaceAll("[^A-Za-zÄÖÜäöüß]", "");
            if (word.length() < 2) word = "JAVA";

            engine = new HangmanEngine(word, 8);
            updateUI("Neue Runde!");

        } catch (Exception ex) {
            lblStatus.setText("Fehler: " + ex.getMessage());
        }
    }

    private void guess() {
        if (engine == null) return;
        String s = txtChar.getText().trim();
        if (s.length() == 0) return;

        char ch = s.charAt(0);
        txtChar.setText("");
        txtChar.requestFocusInWindow();

        HangmanEngine.Result r = engine.guess(ch);
        if (r == HangmanEngine.Result.INVALID) updateUI("Nur Buchstaben!");
        else if (r == HangmanEngine.Result.ALREADY) updateUI("Schon geraten.");
        else if (r == HangmanEngine.Result.HIT) updateUI("Treffer!");
        else updateUI("Falsch!");

        if (engine.isWon()) {
            updateUI("Gewonnen! Wort: " + engine.getOriginal());
            showEndPopup(true);
        } else if (engine.isLost()) {
            updateUI("Verloren. Wort: " + engine.getOriginal());
            showEndPopup(false);
        }
    }

    private void showEndPopup(boolean won) {
        String title = won ? "Gewonnen!" : "Verloren!";
        String msg = (won ? "Du hast es geschafft!\n" : "Leider nicht geschafft.\n")
                + "Was möchtest du jetzt tun?";

        Object[] options = new Object[] {
                "Gleiches nochmal",
                "Andere Kartei",
                "Menü"
        };

        int choice = JOptionPane.showOptionDialog(
                this, msg, title,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            // gleicher Deck bleibt ausgewählt
            startRound();
        } else if (choice == 1) {
            // anderer Deck: einfach hier lassen, User wählt Combo
            lblStatus.setText("Wähle eine andere Kartei aus.");
        } else {
            app.showMenu();
        }
    }

    private void updateUI(String msg) {
        lblWord.setText(engine.maskedWithSpaces());
        lblWrong.setText("Falsche Buchstaben: " + engine.wrongLettersString());
        lblStatus.setText(msg + "  (" + engine.getWrongCount() + "/" + engine.getMaxWrong() + ")");
    }
}
