import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Vokabeltrainer GUI – nur Oberfläche, keine Fachlogik.
 */
public class VokabelTrainerGUI extends JFrame {

    private CardLayout cardLayout;
    private JPanel contentPanel;

    // Karten-Namen für CardLayout
    public static final String CARD_MAIN = "mainMenu";
    public static final String CARD_SETTINGS = "settings";
    public static final String CARD_QUIZ = "quiz";
    public static final String CARD_HANGMAN = "hangman";

    public VokabelTrainerGUI() {
        super("Vokabeltrainer");

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Panels anlegen
        MainMenuPanel mainMenu = new MainMenuPanel();
        SettingsPanel settingsPanel = new SettingsPanel();
        QuizPanel quizPanel = new QuizPanel();
        HangmanPanel hangmanPanel = new HangmanPanel();

        // Panels registrieren
        contentPanel.add(mainMenu, CARD_MAIN);
        contentPanel.add(settingsPanel, CARD_SETTINGS);
        contentPanel.add(quizPanel, CARD_QUIZ);
        contentPanel.add(hangmanPanel, CARD_HANGMAN);

        getContentPane().add(contentPanel);

        // --- Navigation / Aktionen ---

        // Hauptmenü → Quiz
        mainMenu.btnQuiz.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(contentPanel, CARD_QUIZ);
            }
        });

        // Hauptmenü → Hangman
        mainMenu.btnHangman.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(contentPanel, CARD_HANGMAN);
            }
        });

        // Hauptmenü → Einstellungen
        mainMenu.btnEinstellungen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(contentPanel, CARD_SETTINGS);
            }
        });

        // Hauptmenü → Beenden
        mainMenu.btnBeenden.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // Zurück-Buttons auf anderen Screens
        settingsPanel.btnZurueck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(contentPanel, CARD_MAIN);
            }
        });
        quizPanel.btnAbbrechen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(contentPanel, CARD_MAIN);
            }
        });
        hangmanPanel.btnAbbrechen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(contentPanel, CARD_MAIN);
            }
        });

        // Standard-Frame-Einstellungen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); // zentrieren
    }

    // ---------- Hauptmenü ----------

    private static class MainMenuPanel extends JPanel {
        JButton btnQuiz;
        JButton btnHangman;
        JButton btnEinstellungen;
        JButton btnBeenden;

        MainMenuPanel() {
            setLayout(new BorderLayout());

            JLabel titel = new JLabel("Vokabeltrainer - Hauptmenü", JLabel.CENTER);
            titel.setFont(titel.getFont().deriveFont(Font.BOLD, 24f));
            add(titel, BorderLayout.NORTH);

            JPanel center = new JPanel(new GridLayout(4, 1, 10, 10));

            btnQuiz = new JButton("Quizmodus");
            btnHangman = new JButton("Spielmodus Hangman");
            btnEinstellungen = new JButton("Einstellungen / Lernkarteien");
            btnBeenden = new JButton("Programm beenden");

            center.add(btnQuiz);
            center.add(btnHangman);
            center.add(btnEinstellungen);
            center.add(btnBeenden);

            int border = 40;
            center.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
            add(center, BorderLayout.CENTER);
        }
    }

    // ---------- Einstellungen / Lernkartei-Verwaltung ----------

    private static class SettingsPanel extends JPanel {

        JList<String> lstKarteien;
        JButton btnKarteiHinzufuegen;
        JButton btnKarteiAuswaehlen;
        JButton btnKarteiLoeschen;
        JButton btnKarteiBearbeiten;
        JButton btnSpeichern;
        JButton btnAbbrechen;
        JButton btnZurueck;

        JTable tblVokabeln;
        JButton btnVokabelHinzufuegen;
        JButton btnVokabelLoeschen;
        JTextField txtFrage;
        JTextField txtAntwort;

        SettingsPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JLabel titel = new JLabel("Einstellungen - Lernkarteien verwalten", JLabel.CENTER);
            titel.setFont(titel.getFont().deriveFont(Font.BOLD, 20f));
            add(titel, BorderLayout.NORTH);

            // Linke Seite: Liste der Karteien + Buttons
            JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
            leftPanel.setBorder(BorderFactory.createTitledBorder("Lernkarteien"));

            lstKarteien = new JList<String>(new DefaultListModel<String>());
            JScrollPane scrollKarteien = new JScrollPane(lstKarteien);
            leftPanel.add(scrollKarteien, BorderLayout.CENTER);

            JPanel karteiButtonPanel = new JPanel(new GridLayout(4, 1, 5, 5));
            btnKarteiHinzufuegen = new JButton("Kartei hinzufügen");
            btnKarteiAuswaehlen = new JButton("Kartei auswählen");
            btnKarteiLoeschen = new JButton("Kartei löschen");
            btnKarteiBearbeiten = new JButton("Kartei bearbeiten");
            karteiButtonPanel.add(btnKarteiHinzufuegen);
            karteiButtonPanel.add(btnKarteiAuswaehlen);
            karteiButtonPanel.add(btnKarteiLoeschen);
            karteiButtonPanel.add(btnKarteiBearbeiten);
            leftPanel.add(karteiButtonPanel, BorderLayout.SOUTH);

            add(leftPanel, BorderLayout.WEST);

            // Rechte Seite: Vokabel-Tabelle und Eingabe
            JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
            rightPanel.setBorder(BorderFactory.createTitledBorder("Vokabeln"));

            String[] columnNames = {"Frage", "Antwort"};
            Object[][] data = new Object[0][2]; // leer, später füllen
            tblVokabeln = new JTable(new javax.swing.table.DefaultTableModel(data, columnNames));
            JScrollPane scrollVokabeln = new JScrollPane(tblVokabeln);
            rightPanel.add(scrollVokabeln, BorderLayout.CENTER);

            JPanel editPanel = new JPanel(new GridLayout(3, 2, 5, 5));
            editPanel.add(new JLabel("Frage:"));
            txtFrage = new JTextField();
            editPanel.add(txtFrage);
            editPanel.add(new JLabel("Antwort:"));
            txtAntwort = new JTextField();
            editPanel.add(txtAntwort);

            btnVokabelHinzufuegen = new JButton("Vokabel hinzufügen");
            btnVokabelLoeschen = new JButton("Vokabel löschen");
            editPanel.add(btnVokabelHinzufuegen);
            editPanel.add(btnVokabelLoeschen);

            rightPanel.add(editPanel, BorderLayout.SOUTH);

            add(rightPanel, BorderLayout.CENTER);

            // Untere Button-Leiste
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            btnSpeichern = new JButton("Bearbeitungen speichern");
            btnAbbrechen = new JButton("Abbrechen");
            btnZurueck = new JButton("Zurück zum Menü");
            bottomPanel.add(btnSpeichern);
            bottomPanel.add(btnAbbrechen);
            bottomPanel.add(btnZurueck);

            add(bottomPanel, BorderLayout.SOUTH);
        }
    }

    // ---------- Quizmodus ----------

    private static class QuizPanel extends JPanel {

        JComboBox<String> cmbKartei;
        JLabel lblFrage;
        JTextField txtAntwort;
        JButton btnAntwortPruefen;
        JButton btnNaechsteFrage;
        JButton btnAbbrechen;
        JLabel lblFeedback;
        JLabel lblFragenZaehler;
        JLabel lblStatistikKurz;

        QuizPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JLabel titel = new JLabel("Quizmodus", JLabel.CENTER);
            titel.setFont(titel.getFont().deriveFont(Font.BOLD, 20f));
            add(titel, BorderLayout.NORTH);

            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            topPanel.add(new JLabel("Lernkartei:"));
            cmbKartei = new JComboBox<String>();
            topPanel.add(cmbKartei);
            add(topPanel, BorderLayout.PAGE_START);

            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

            lblFrage = new JLabel("Frage wird hier angezeigt");
            lblFrage.setAlignmentX(Component.LEFT_ALIGNMENT);
            lblFrage.setFont(lblFrage.getFont().deriveFont(Font.BOLD, 18f));
            centerPanel.add(lblFrage);
            centerPanel.add(Box.createVerticalStrut(10));

            JPanel answerPanel = new JPanel(new BorderLayout(5, 5));
            answerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            answerPanel.add(new JLabel("Antwort eingeben:"), BorderLayout.WEST);
            txtAntwort = new JTextField();
            answerPanel.add(txtAntwort, BorderLayout.CENTER);
            centerPanel.add(answerPanel);
            centerPanel.add(Box.createVerticalStrut(10));

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            btnAntwortPruefen = new JButton("Antwort prüfen");
            btnNaechsteFrage = new JButton("Nächste Frage");
            buttonPanel.add(btnAntwortPruefen);
            buttonPanel.add(btnNaechsteFrage);
            centerPanel.add(buttonPanel);

            centerPanel.add(Box.createVerticalStrut(10));
            lblFeedback = new JLabel("Feedback erscheint hier");
            lblFeedback.setAlignmentX(Component.LEFT_ALIGNMENT);
            centerPanel.add(lblFeedback);

            add(centerPanel, BorderLayout.CENTER);

            JPanel bottomPanel = new JPanel(new BorderLayout());
            JPanel infoPanel = new JPanel(new GridLayout(2, 1));
            lblFragenZaehler = new JLabel("Frage 0/0");
            lblStatistikKurz = new JLabel("Richtig: 0  Falsch: 0");
            infoPanel.add(lblFragenZaehler);
            infoPanel.add(lblStatistikKurz);

            bottomPanel.add(infoPanel, BorderLayout.WEST);

            btnAbbrechen = new JButton("Abbrechen / zurück");
            bottomPanel.add(btnAbbrechen, BorderLayout.EAST);

            add(bottomPanel, BorderLayout.SOUTH);
        }
    }

    // ---------- Hangmanmodus ----------

    private static class HangmanPanel extends JPanel {

        JComboBox<String> cmbKartei;
        JLabel lblAngezeigtesWort;
        JLabel lblFalscheBuchstaben;
        JTextField txtBuchstabe;
        JButton btnRateversuch;
        JButton btnNeueRunde;
        JButton btnAbbrechen;
        JLabel lblStatus;

        HangmanPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JLabel titel = new JLabel("Spielmodus Hangman", JLabel.CENTER);
            titel.setFont(titel.getFont().deriveFont(Font.BOLD, 20f));
            add(titel, BorderLayout.NORTH);

            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            topPanel.add(new JLabel("Lernkartei:"));
            cmbKartei = new JComboBox<String>();
            topPanel.add(cmbKartei);
            add(topPanel, BorderLayout.PAGE_START);

            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

            lblAngezeigtesWort = new JLabel("_ _ _ _ _");
            lblAngezeigtesWort.setFont(lblAngezeigtesWort.getFont().deriveFont(Font.BOLD, 26f));
            lblAngezeigtesWort.setAlignmentX(Component.LEFT_ALIGNMENT);
            centerPanel.add(lblAngezeigtesWort);
            centerPanel.add(Box.createVerticalStrut(10));

            lblFalscheBuchstaben = new JLabel("Falsche Buchstaben: -");
            lblFalscheBuchstaben.setAlignmentX(Component.LEFT_ALIGNMENT);
            centerPanel.add(lblFalscheBuchstaben);
            centerPanel.add(Box.createVerticalStrut(10));

            JPanel ratePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            ratePanel.add(new JLabel("Buchstabe:"));
            txtBuchstabe = new JTextField(3);
            ratePanel.add(txtBuchstabe);
            btnRateversuch = new JButton("Rateversuch");
            ratePanel.add(btnRateversuch);
            centerPanel.add(ratePanel);

            centerPanel.add(Box.createVerticalStrut(10));
            lblStatus = new JLabel("Status: Neues Spiel starten");
            lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
            centerPanel.add(lblStatus);

            add(centerPanel, BorderLayout.CENTER);

            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            btnNeueRunde = new JButton("Neue Runde");
            btnAbbrechen = new JButton("Abbrechen / zurück");
            bottomPanel.add(btnNeueRunde);
            bottomPanel.add(btnAbbrechen);
            add(bottomPanel, BorderLayout.SOUTH);
        }
    }

    // ---------- main ----------

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new VokabelTrainerGUI().setVisible(true);
            }
        });
    }
}
