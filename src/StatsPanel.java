import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import javax.swing.table.DefaultTableModel;

public class StatsPanel extends JPanel {

    private AppFrame app;

    JComboBox<String> cmbUser; // nur sichtbar für Admin
    JTable table;
    DefaultTableModel model;

    JButton btnBack;
    JLabel lblInfo;

    public StatsPanel(AppFrame app) {
        this.app = app;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Quiz-Statistiken (Verlauf)");
        title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, 20f));
        add(title, BorderLayout.NORTH);

        JPanel top = new JPanel(new BorderLayout());
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.add(new JLabel("User:"));
        cmbUser = new JComboBox<String>();
        left.add(cmbUser);
        top.add(left, BorderLayout.WEST);

        lblInfo = new JLabel(" ");
        top.add(lblInfo, BorderLayout.EAST);

        add(top, BorderLayout.PAGE_START);

        model = new DefaultTableModel(new Object[]{
                "Zeit", "Kartei", "Total", "Richtig", "Falsch", "Timeout", "Sek/Frage", "ØZeit", "Accuracy %"
        }, 0);

        table = new JTable(model);
        table.setRowHeight(26);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnBack = new JButton("Zurück");
        bottom.add(btnBack);
        add(bottom, BorderLayout.SOUTH);

        btnBack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { app.showMenu(); }
        });

        cmbUser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { reload(); }
        });

        reset();
    }

    public void reset() {
        model.setRowCount(0);
        cmbUser.removeAllItems();
        lblInfo.setText(" ");
    }

    public void reload() {
        cmbUser.setVisible(app.isAdmin());

        if (app.isAdmin() && cmbUser.getItemCount() == 0) {
            List<String> users = app.getUserManager().listUsers();
            for (int i = 0; i < users.size(); i++) cmbUser.addItem(users.get(i));
            cmbUser.setSelectedItem(app.getCurrentUser());
        }

        String user = app.isAdmin()
                ? String.valueOf(cmbUser.getSelectedItem())
                : app.getCurrentUser();

        model.setRowCount(0);

        List<String[]> rows = app.getStatsStore().load(user);
        for (int i = 0; i < rows.size(); i++) {
            String[] p = rows.get(i);
            Object[] r = new Object[] {
                    get(p,0), get(p,1), get(p,2), get(p,3), get(p,4),
                    get(p,5), get(p,6), get(p,7), get(p,8)
            };
            model.addRow(r);
        }

        lblInfo.setText("Einträge: " + rows.size());
    }

    private String get(String[] a, int idx) {
        return (idx < a.length) ? a[idx] : "";
    }
}
