import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.DefaultListModel;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class SettingsPanel extends JPanel {

    private AppFrame app;

    private String targetUser; // null = aktueller User, sonst Admin bearbeitet anderen User

    JComboBox<String> cmbType;
    DefaultListModel<String> deckListModel;
    JList<String> lstDecks;

    JTable table;
    DefaultTableModel tableModel;

    JLabel lblField1;
    JLabel lblField2;
    JTextField txt1;
    JTextField txt2;

    JButton btnDeckAdd, btnDeckLoad, btnDeckDelete;
    JButton btnEntryAdd, btnEntryDelete;
    JButton btnSave, btnBack;

    JLabel lblStatus;

    private Deck loadedDeck;

    public SettingsPanel(AppFrame app) {
        this.app = app;
        this.targetUser = null;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Einstellungen / Karteien");
        title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, 20f));
        add(title, BorderLayout.NORTH);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.add(new JLabel("Typ:"));
        cmbType = new JComboBox<String>(new String[]{"Quiz", "Hangman"});
        top.add(cmbType);
        add(top, BorderLayout.PAGE_START);

        deckListModel = new DefaultListModel<String>();
        lstDecks = new JList<String>(deckListModel);
        lstDecks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel left = new JPanel(new BorderLayout(6, 6));
        left.setBorder(BorderFactory.createTitledBorder("Karteien"));
        left.add(new JScrollPane(lstDecks), BorderLayout.CENTER);

        JPanel leftBtns = new JPanel(new GridLayout(3, 1, 6, 6));
        btnDeckAdd = new JButton("Kartei hinzufügen");
        btnDeckLoad = new JButton("Kartei laden");
        btnDeckDelete = new JButton("Kartei löschen");
        leftBtns.add(btnDeckAdd);
        leftBtns.add(btnDeckLoad);
        leftBtns.add(btnDeckDelete);
        left.add(leftBtns, BorderLayout.SOUTH);

        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        table.setRowHeight(26);

        JPanel right = new JPanel(new BorderLayout(6, 6));
        right.setBorder(BorderFactory.createTitledBorder("Inhalt"));
        right.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel edit = new JPanel(new GridLayout(3, 2, 6, 6));
        lblField1 = new JLabel("Frage:");
        lblField2 = new JLabel("Antwort:");
        txt1 = new JTextField();
        txt2 = new JTextField();

        edit.add(lblField1);
        edit.add(txt1);
        edit.add(lblField2);
        edit.add(txt2);

        btnEntryAdd = new JButton("Hinzufügen");
        btnEntryDelete = new JButton("Löschen");
        edit.add(btnEntryAdd);
        edit.add(btnEntryDelete);

        right.add(edit, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.3);
        add(split, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        lblStatus = new JLabel(" ");
        bottom.add(lblStatus, BorderLayout.WEST);

        JPanel bBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSave = new JButton("Speichern");
        btnBack = new JButton("Zurück");
        bBtns.add(btnSave);
        bBtns.add(btnBack);
        bottom.add(bBtns, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);

        cmbType.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reload();
            }
        });

        btnDeckAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { addDeck(); }
        });
        btnDeckLoad.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { loadSelectedDeck(); }
        });
        btnDeckDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { deleteSelectedDeck(); }
        });

        btnEntryAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { addItem(); }
        });
        btnEntryDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { deleteItem(); }
        });

        btnSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { saveDeck(); }
        });

        btnBack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { app.showMenu(); }
        });

        reset();
    }

    public void setTargetUser(String userOrNull) {
        this.targetUser = userOrNull;
    }

    private String effectiveUser() {
        if (targetUser != null) return targetUser;
        return app.getCurrentUser();
    }

    public void reset() {
        deckListModel.clear();
        loadedDeck = null;
        setTableForQuiz("Frage", "Antwort");
        lblStatus.setText(" ");
    }

    public void reload() {
        reloadDeckList();
        loadedDeck = null;
        clearTableForType();
    }

    public void reloadDeckList() {
        deckListModel.clear();
        DeckType type = getSelectedType();
        try {
            List<String> decks = app.getDeckStore().listDecks(effectiveUser(), type);
            for (int i = 0; i < decks.size(); i++) deckListModel.addElement(decks.get(i));
            lblStatus.setText(decks.size() == 0 ? "Keine Karteien vorhanden." : " ");
        } catch (Exception ex) {
            lblStatus.setText("Fehler: " + ex.getMessage());
        }
    }

    private DeckType getSelectedType() {
        return ("Hangman".equals(String.valueOf(cmbType.getSelectedItem()))) ? DeckType.HANGMAN : DeckType.QUIZ;
    }

    private void clearTableForType() {
        DeckType type = getSelectedType();
        if (type == DeckType.QUIZ) {
            setTableForQuiz("Frage", "Antwort");
            txt2.setVisible(true);
            lblField2.setVisible(true);
        } else {
            setTableForHangman("Wort");
            txt2.setVisible(false);
            lblField2.setVisible(false);
        }
        revalidate();
        repaint();
    }

    private void setTableForQuiz(String l1, String l2) {
        tableModel.setDataVector(new Object[0][0], new Object[]{l1, l2});
        lblField1.setText(l1 + ":");
        lblField2.setText(l2 + ":");
    }

    private void setTableForHangman(String l1) {
        tableModel.setDataVector(new Object[0][0], new Object[]{l1});
        lblField1.setText(l1 + ":");
    }

    private void addDeck() {
        DeckType type = getSelectedType();

        String name = JOptionPane.showInputDialog(this, "Name der Kartei:", "Neue Kartei",
                JOptionPane.PLAIN_MESSAGE);
        if (name == null) return;
        name = name.trim();
        if (name.length() == 0) return;

        try {
            if (type == DeckType.QUIZ) {
                String l1 = JOptionPane.showInputDialog(this, "Erstes Wort (z.B. English):", "Quiz Label 1",
                        JOptionPane.PLAIN_MESSAGE);
                if (l1 == null) return;
                l1 = l1.trim();
                if (l1.length() == 0) l1 = "Frage";

                String l2 = JOptionPane.showInputDialog(this, "Zweites Wort (z.B. Deutsch):", "Quiz Label 2",
                        JOptionPane.PLAIN_MESSAGE);
                if (l2 == null) return;
                l2 = l2.trim();
                if (l2.length() == 0) l2 = "Antwort";

                boolean ok = app.getDeckStore().createDeck(effectiveUser(), name, DeckType.QUIZ, l1, l2);
                if (!ok) {
                    JOptionPane.showMessageDialog(this, "Kartei existiert bereits.");
                    return;
                }
            } else {
                String l1 = JOptionPane.showInputDialog(this, "Ein Wort (z.B. Words):", "Hangman Label",
                        JOptionPane.PLAIN_MESSAGE);
                if (l1 == null) return;
                l1 = l1.trim();
                if (l1.length() == 0) l1 = "Wort";

                boolean ok = app.getDeckStore().createDeck(effectiveUser(), name, DeckType.HANGMAN, l1, "");
                if (!ok) {
                    JOptionPane.showMessageDialog(this, "Kartei existiert bereits.");
                    return;
                }
            }

            reloadDeckList();
            lstDecks.setSelectedValue(name, true);
            loadSelectedDeck();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Fehler: " + ex.getMessage());
        }
    }

    private void loadSelectedDeck() {
        String sel = lstDecks.getSelectedValue();
        if (sel == null) return;

        try {
            loadedDeck = app.getDeckStore().loadDeck(effectiveUser(), sel);

            if (loadedDeck.type == DeckType.QUIZ) {
                setTableForQuiz(loadedDeck.label1, loadedDeck.label2);
                tableModel.setRowCount(0);
                for (int i = 0; i < loadedDeck.entries.size(); i++) {
                    Entry e = loadedDeck.entries.get(i);
                    tableModel.addRow(new Object[]{e.question, e.answer});
                }
                txt2.setVisible(true);
                lblField2.setVisible(true);
            } else {
                setTableForHangman(loadedDeck.label1);
                tableModel.setRowCount(0);
                for (int i = 0; i < loadedDeck.words.size(); i++) {
                    tableModel.addRow(new Object[]{loadedDeck.words.get(i)});
                }
                txt2.setVisible(false);
                lblField2.setVisible(false);
            }

            lblStatus.setText("Geladen: " + sel);
            revalidate();
            repaint();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Fehler: " + ex.getMessage());
        }
    }

    private void deleteSelectedDeck() {
        String sel = lstDecks.getSelectedValue();
        if (sel == null) return;

        int ok = JOptionPane.showConfirmDialog(this, "Kartei löschen?\n" + sel,
                "Bestätigen", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            app.getDeckStore().deleteDeck(effectiveUser(), sel);
            reload();
            lblStatus.setText("Kartei gelöscht.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Fehler: " + ex.getMessage());
        }
    }

    private void addItem() {
        if (loadedDeck == null) {
            JOptionPane.showMessageDialog(this, "Bitte zuerst eine Kartei laden.");
            return;
        }

        if (loadedDeck.type == DeckType.QUIZ) {
            String q = txt1.getText().trim();
            String a = txt2.getText().trim();
            if (q.length() == 0 || a.length() == 0) return;
            tableModel.addRow(new Object[]{q, a});
            txt1.setText("");
            txt2.setText("");
            txt1.requestFocusInWindow();
        } else {
            String w = txt1.getText().trim();
            if (w.length() == 0) return;
            tableModel.addRow(new Object[]{w});
            txt1.setText("");
            txt1.requestFocusInWindow();
        }
    }

    private void deleteItem() {
        int row = table.getSelectedRow();
        if (row >= 0) tableModel.removeRow(row);
    }

    private void saveDeck() {
        String sel = lstDecks.getSelectedValue();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Bitte eine Kartei auswählen.");
            return;
        }

        try {
            Deck d = app.getDeckStore().loadDeck(effectiveUser(), sel);

            if (d.type == DeckType.QUIZ) {
                d.entries.clear();
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String q = String.valueOf(tableModel.getValueAt(i, 0)).trim();
                    String a = String.valueOf(tableModel.getValueAt(i, 1)).trim();
                    if (q.length() > 0 && a.length() > 0) d.entries.add(new Entry(q, a));
                }
            } else {
                d.words.clear();
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String w = String.valueOf(tableModel.getValueAt(i, 0)).trim();
                    if (w.length() > 0) d.words.add(w);
                }
            }

            app.getDeckStore().saveDeck(effectiveUser(), d);
            lblStatus.setText("Gespeichert: " + sel);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Fehler: " + ex.getMessage());
        }
    }
}
