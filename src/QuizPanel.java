import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class QuizPanel extends JPanel {

    private AppFrame app;

    JComboBox<String> cmbDeck;
    JSpinner spSeconds;

    JLabel lblQuestion;
    JTextField txtAnswer;
    JButton btnCheck;
    JButton btnNext;
    JButton btnBack;

    JLabel lblInfo;
    JLabel lblCounter;
    JLabel lblTimer;

    private QuizEngine engine;
    private Deck loadedDeck;

    private javax.swing.Timer timer;
    private int secondsLeft;
    private int secondsLimit;

    private boolean answeredCurrent;

    private int totalTimeUsed; // Sekunden summiert

    public QuizPanel(AppFrame app) {
        this.app = app;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Quiz (mit Zeitdruck)");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        add(title, BorderLayout.NORTH);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.add(new JLabel("Kartei:"));
        cmbDeck = new JComboBox<String>();
        cmbDeck.setPreferredSize(new Dimension(250, 28));
        top.add(cmbDeck);

        top.add(new JLabel("Sekunden pro Frage:"));
        spSeconds = new JSpinner(new SpinnerNumberModel(10, 3, 120, 1));
        top.add(spSeconds);

        add(top, BorderLayout.PAGE_START);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createTitledBorder("Frage"));

        lblQuestion = new JLabel("Wähle eine Kartei …");
        lblQuestion.setFont(lblQuestion.getFont().deriveFont(Font.BOLD, 18f));
        lblQuestion.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(lblQuestion);

        center.add(Box.createVerticalStrut(10));

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(new JLabel("Antwort:"), BorderLayout.WEST);
        txtAnswer = new JTextField();
        row.add(txtAnswer, BorderLayout.CENTER);
        center.add(row);

        center.add(Box.createVerticalStrut(10));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnCheck = new JButton("Prüfen");
        btnNext = new JButton("Nächste");
        btns.add(btnCheck);
        btns.add(btnNext);
        btns.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(btns);

        center.add(Box.createVerticalStrut(10));
        lblInfo = new JLabel(" ");
        lblInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(lblInfo);

        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        lblCounter = new JLabel("Frage 0/0");
        lblTimer = new JLabel("Zeit: -");

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.add(lblCounter);
        left.add(lblTimer);

        btnBack = new JButton("Zurück");
        bottom.add(left, BorderLayout.WEST);
        bottom.add(btnBack, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);

        cmbDeck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { startDeckFromCombo(); }
        });

        btnCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { checkAnswer(); }
        });

        btnNext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { nextQuestion(); }
        });

        btnBack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopTimer();
                app.showMenu();
            }
        });

        txtAnswer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { checkAnswer(); }
        });

        timer = new javax.swing.Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) { tick(); }
        });
        timer.setRepeats(true);

        reset();
    }

    public void reset() {
        stopTimer();
        cmbDeck.removeAllItems();
        lblQuestion.setText("Wähle eine Kartei …");
        lblInfo.setText(" ");
        lblCounter.setText("Frage 0/0");
        lblTimer.setText("Zeit: -");
        txtAnswer.setText("");
        engine = null;
        loadedDeck = null;
        answeredCurrent = false;
        totalTimeUsed = 0;
    }

    public void reloadDecks() {
        cmbDeck.removeAllItems();
        try {
            java.util.List<String> decks = app.getDeckStore().listDecks(app.getCurrentUser(), DeckType.QUIZ);
            for (int i = 0; i < decks.size(); i++) cmbDeck.addItem(decks.get(i));
            if (decks.size() > 0) cmbDeck.setSelectedIndex(0);
            startDeckFromCombo();
        } catch (Exception ex) {
            lblInfo.setText("Fehler: " + ex.getMessage());
        }
    }

    private void startDeckFromCombo() {
        stopTimer();
        totalTimeUsed = 0;

        String deckName = (String) cmbDeck.getSelectedItem();
        if (deckName == null) {
            engine = null;
            lblQuestion.setText("Keine Quiz-Kartei vorhanden.");
            return;
        }

        try {
            loadedDeck = app.getDeckStore().loadDeck(app.getCurrentUser(), deckName);
            if (loadedDeck.type != DeckType.QUIZ) {
                lblQuestion.setText("Diese Kartei ist keine Quiz-Kartei.");
                return;
            }

            engine = new QuizEngine(loadedDeck);
            if (engine.total() == 0) {
                lblQuestion.setText("Diese Kartei ist leer.");
                lblTimer.setText("Zeit: -");
                lblCounter.setText("Frage 0/0");
                return;
            }
            engine.start();
            showCurrentQuestion();
        } catch (Exception ex) {
            lblInfo.setText("Fehler: " + ex.getMessage());
        }
    }

    private void showCurrentQuestion() {
        if (engine == null || engine.total() == 0) return;

        answeredCurrent = false;
        lblInfo.setText(" ");
        lblInfo.setForeground(new Color(30, 30, 30));

        lblQuestion.setText(engine.current().question);
        txtAnswer.setText("");
        txtAnswer.requestFocusInWindow();

        lblCounter.setText("Frage " + (engine.getIndex() + 1) + "/" + engine.total());

        secondsLimit = ((Integer) spSeconds.getValue()).intValue();
        secondsLeft = secondsLimit;
        updateTimerLabel();
        timer.start();
    }

    private void tick() {
        if (engine == null) return;
        if (answeredCurrent) return;

        secondsLeft--;
        updateTimerLabel();

        if (secondsLeft <= 0) timeUp();
    }

    private void updateTimerLabel() {
        lblTimer.setText("Zeit: " + secondsLeft + "s");
    }

    private void timeUp() {
        stopTimer();
        if (engine == null || answeredCurrent) return;

        answeredCurrent = true;
        engine.markTimeout();
        totalTimeUsed += secondsLimit;

        lblInfo.setForeground(new Color(160, 40, 40));
        lblInfo.setText("⏰ Zeit abgelaufen! Richtig wäre: " + engine.current().answer);
    }

    private void checkAnswer() {
        if (engine == null || engine.total() == 0) return;
        if (answeredCurrent) return;

        String given = txtAnswer.getText().trim();
        if (given.length() == 0) return;

        stopTimer();
        answeredCurrent = true;

        int used = secondsLimit - secondsLeft;
        if (used < 0) used = 0;
        totalTimeUsed += used;

        boolean ok = engine.check(given);

        if (ok) {
            lblInfo.setForeground(new Color(20, 120, 60));
            lblInfo.setText("✅ Richtig!");
        } else {
            lblInfo.setForeground(new Color(160, 40, 40));
            lblInfo.setText("❌ Falsch. Richtig wäre: " + engine.current().answer);
        }
    }

    private void nextQuestion() {
        if (engine == null || engine.total() == 0) return;

        if (!answeredCurrent) {
            stopTimer();
            answeredCurrent = true;
            engine.markSkippedWrong();
            totalTimeUsed += (secondsLimit - secondsLeft);
        }

        boolean hasNext = engine.next();
        if (hasNext) {
            showCurrentQuestion();
        } else {
            stopTimer();
            showStatisticsAndSave();
        }
    }

    private void showStatisticsAndSave() {
        int total = engine.total();
        int correct = engine.getCorrect();
        int wrong = engine.getWrong();
        int timeout = engine.getTimeout();

        double acc = (total == 0) ? 0 : (100.0 * correct / total);
        double avgTime = (total == 0) ? 0 : (1.0 * totalTimeUsed / total);

        // Speichern in Verlauf
        String deckName = (String) cmbDeck.getSelectedItem();
        int secPerQ = ((Integer) spSeconds.getValue()).intValue();
        app.getStatsStore().append(app.getCurrentUser(), deckName, total, correct, wrong, timeout, secPerQ, avgTime);

        String msg =
                "Quiz beendet!\n\n" +
                        "Kartei: " + deckName + "\n" +
                        "Gesamt: " + total + "\n" +
                        "Richtig: " + correct + "\n" +
                        "Falsch: " + wrong + "\n" +
                        "Timeouts: " + timeout + "\n\n" +
                        "Genauigkeit: " + String.format(java.util.Locale.ROOT, "%.1f", acc) + " %\n" +
                        "Ø Zeit/Frage: " + String.format(java.util.Locale.ROOT, "%.1f", avgTime) + " s";

        JOptionPane.showMessageDialog(this, msg, "Statistik", JOptionPane.INFORMATION_MESSAGE);

        startDeckFromCombo();
    }

    private void stopTimer() {
        if (timer.isRunning()) timer.stop();
    }
}
