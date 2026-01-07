import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class AppFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel content;

    public static final String CARD_LOGIN    = "login";
    public static final String CARD_MENU     = "menu";
    public static final String CARD_SETTINGS = "settings";
    public static final String CARD_QUIZ     = "quiz";
    public static final String CARD_HANGMAN  = "hangman";
    public static final String CARD_STATS    = "stats";
    public static final String CARD_ADMIN    = "admin";

    private JLabel lblUser;
    private JButton btnLogout;

    private String currentUser;

    private UserManager userManager;
    private DeckStore deckStore;
    private QuizStatsStore statsStore;

    private LoginPanel loginPanel;
    private MainMenuPanel menuPanel;
    private SettingsPanel settingsPanel;
    private QuizPanel quizPanel;
    private HangmanPanel hangmanPanel;
    private StatsPanel statsPanel;
    private AdminPanel adminPanel;

    public AppFrame() {
        super("Vokabeltrainer (einfach)");

        userManager = new UserManager();
        deckStore = new DeckStore();
        statsStore = new QuizStatsStore();

        cardLayout = new CardLayout();
        content = new JPanel(cardLayout);

        loginPanel = new LoginPanel(this);
        menuPanel = new MainMenuPanel(this);
        settingsPanel = new SettingsPanel(this);
        quizPanel = new QuizPanel(this);
        hangmanPanel = new HangmanPanel(this);
        statsPanel = new StatsPanel(this);
        adminPanel = new AdminPanel(this);

        content.add(loginPanel, CARD_LOGIN);
        content.add(menuPanel, CARD_MENU);
        content.add(settingsPanel, CARD_SETTINGS);
        content.add(quizPanel, CARD_QUIZ);
        content.add(hangmanPanel, CARD_HANGMAN);
        content.add(statsPanel, CARD_STATS);
        content.add(adminPanel, CARD_ADMIN);

        setLayout(new BorderLayout());
        add(buildTopBar(), BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 620);
        setLocationRelativeTo(null);

        showLogin();
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JLabel title = new JLabel("Vokabeltrainer");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        bar.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        lblUser = new JLabel("Nicht eingeloggt");
        btnLogout = new JButton("Logout");
        btnLogout.setEnabled(false);

        btnLogout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });

        right.add(lblUser);
        right.add(btnLogout);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    public void showLogin() {
        currentUser = null;
        lblUser.setText("Nicht eingeloggt");
        btnLogout.setEnabled(false);

        settingsPanel.reset();
        quizPanel.reset();
        hangmanPanel.reset();
        statsPanel.reset();
        adminPanel.reset();

        cardLayout.show(content, CARD_LOGIN);
    }

    public void showMenu() {
        menuPanel.refreshAdminButton();
        cardLayout.show(content, CARD_MENU);
    }

    public void showSettings() {
        settingsPanel.setTargetUser(null); // normal user
        settingsPanel.reload();
        cardLayout.show(content, CARD_SETTINGS);
    }

    public void showQuiz() {
        quizPanel.reloadDecks();
        cardLayout.show(content, CARD_QUIZ);
    }

    public void showHangman() {
        hangmanPanel.reloadDecks();
        cardLayout.show(content, CARD_HANGMAN);
    }

    public void showStats() {
        statsPanel.reload();
        cardLayout.show(content, CARD_STATS);
    }

    public void showAdmin() {
        adminPanel.reload();
        cardLayout.show(content, CARD_ADMIN);
    }

    public boolean login(String user, String pass) {
        if (userManager.login(user, pass)) {
            currentUser = user;
            lblUser.setText("Eingeloggt als: " + user);
            btnLogout.setEnabled(true);

            deckStore.ensureUserFolder(user);
            statsStore.ensureUserFolder(user);

            showMenu();
            return true;
        }
        return false;
    }

    public boolean register(String user, String pass) {
        return userManager.register(user, pass);
    }

    public void logout() {
        showLogin();
    }

    public String getCurrentUser() { return currentUser; }
    public boolean isAdmin() { return "Admin".equals(currentUser); }

    public UserManager getUserManager() { return userManager; }
    public DeckStore getDeckStore() { return deckStore; }
    public QuizStatsStore getStatsStore() { return statsStore; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new AppFrame().setVisible(true);
            }
        });
    }
}
