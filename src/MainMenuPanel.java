import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainMenuPanel extends JPanel {

    JButton btnQuiz;
    JButton btnHangman;
    JButton btnSettings;
    JButton btnStats;
    JButton btnAdmin;
    JButton btnExit;

    private AppFrame app;

    public MainMenuPanel(AppFrame app) {
        this.app = app;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Hauptmenü", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(6, 1, 10, 10));
        btnQuiz = new JButton("Quiz");
        btnHangman = new JButton("Hangman");
        btnSettings = new JButton("Einstellungen / Karteien");
        btnStats = new JButton("Statistiken");
        btnAdmin = new JButton("Admin-Bereich");
        btnExit = new JButton("Beenden");

        center.add(btnQuiz);
        center.add(btnHangman);
        center.add(btnSettings);
        center.add(btnStats);
        center.add(btnAdmin);
        center.add(btnExit);

        center.setBorder(BorderFactory.createEmptyBorder(30, 160, 30, 160));
        add(center, BorderLayout.CENTER);

        btnQuiz.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { app.showQuiz(); }
        });
        btnHangman.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { app.showHangman(); }
        });
        btnSettings.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { app.showSettings(); }
        });
        btnStats.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { app.showStats(); }
        });
        btnAdmin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { app.showAdmin(); }
        });
        btnExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { System.exit(0); }
        });

        refreshAdminButton();
    }

    public void refreshAdminButton() {
        btnAdmin.setEnabled(app.isAdmin());
        btnAdmin.setVisible(app.isAdmin());
    }
}
