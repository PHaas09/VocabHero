import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class AdminPanel extends JPanel {

    private AppFrame app;

    DefaultListModel<String> userModel;
    JList<String> lstUsers;
    JButton btnDeleteUser;
    JButton btnBack;

    SettingsPanel settingsForOtherUser; // Wiederverwendung

    JLabel lblInfo;

    public AdminPanel(AppFrame app) {
        this.app = app;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Admin-Bereich");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        add(title, BorderLayout.NORTH);

        // Left: Users
        userModel = new DefaultListModel<String>();
        lstUsers = new JList<String>(userModel);
        lstUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel left = new JPanel(new BorderLayout(6, 6));
        left.setBorder(BorderFactory.createTitledBorder("Benutzer"));
        left.add(new JScrollPane(lstUsers), BorderLayout.CENTER);

        btnDeleteUser = new JButton("Benutzer löschen");
        left.add(btnDeleteUser, BorderLayout.SOUTH);

        // Right: SettingsPanel für ausgewählten User
        settingsForOtherUser = new SettingsPanel(app);
        settingsForOtherUser.setTargetUser("Admin"); // wird gleich überschrieben

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, settingsForOtherUser);
        split.setResizeWeight(0.25);
        add(split, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        lblInfo = new JLabel(" ");
        bottom.add(lblInfo, BorderLayout.WEST);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnBack = new JButton("Zurück");
        rightBtns.add(btnBack);
        bottom.add(rightBtns, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        btnBack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { app.showMenu(); }
        });

        lstUsers.addListSelectionListener(e -> {
            // ListSelectionListener ist kein Lambda nötig, aber diese Zeile wäre Lambda.
            // Daher: wir machen es "old school" weiter unten in reload() per MouseListener/Selection.
        });

        btnDeleteUser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { deleteSelectedUser(); }
        });

        // Ohne Lambda: Selection über MouseListener
        lstUsers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                selectUser();
            }
        });

        reset();
    }

    public void reset() {
        userModel.clear();
        lblInfo.setText(" ");
        settingsForOtherUser.reset();
        settingsForOtherUser.setTargetUser(null);
    }

    public void reload() {
        if (!app.isAdmin()) {
            JOptionPane.showMessageDialog(this, "Nur Admin darf hier rein.");
            app.showMenu();
            return;
        }

        userModel.clear();
        java.util.List<String> users = app.getUserManager().listUsers();
        for (int i = 0; i < users.size(); i++) userModel.addElement(users.get(i));

        // default selection: erster nicht-Admin
        if (userModel.size() > 0) {
            lstUsers.setSelectedIndex(0);
            selectUser();
        }
    }

    private void selectUser() {
        String user = lstUsers.getSelectedValue();
        if (user == null) return;

        settingsForOtherUser.setTargetUser(user);
        settingsForOtherUser.reload();

        lblInfo.setText("Bearbeite: " + user);
    }

    private void deleteSelectedUser() {
        String user = lstUsers.getSelectedValue();
        if (user == null) return;

        if ("Admin".equals(user)) {
            JOptionPane.showMessageDialog(this, "Admin kann nicht gelöscht werden.");
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this,
                "Benutzer wirklich löschen?\n" + user + "\n(inkl. dessen Dateien)",
                "Bestätigen", JOptionPane.YES_NO_OPTION);

        if (ok != JOptionPane.YES_OPTION) return;

        boolean removed = app.getUserManager().deleteUser(user);
        boolean folder = app.getDeckStore().deleteUserFolder(user);

        if (removed && folder) {
            JOptionPane.showMessageDialog(this, "Benutzer gelöscht: " + user);
            reload();
        } else {
            JOptionPane.showMessageDialog(this, "Konnte Benutzer nicht komplett löschen.");
        }
    }
}
