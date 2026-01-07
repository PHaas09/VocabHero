import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class LoginPanel extends JPanel {

    JTextField txtUser;
    JPasswordField txtPass;
    JButton btnLogin;
    JButton btnRegister;
    JLabel lblInfo;

    private AppFrame app;

    public LoginPanel(AppFrame app) {
        this.app = app;

        setLayout(new GridBagLayout());

        JPanel box = new JPanel(new GridBagLayout());
        box.setBorder(BorderFactory.createTitledBorder("Login / Registrierung"));

        GridBagConstraints b = new GridBagConstraints();
        b.insets = new Insets(6, 6, 6, 6);
        b.fill = GridBagConstraints.HORIZONTAL;
        b.gridx = 0; b.gridy = 0;

        box.add(new JLabel("Benutzername:"), b);
        b.gridx = 1;
        txtUser = new JTextField(18);
        box.add(txtUser, b);

        b.gridx = 0; b.gridy++;
        box.add(new JLabel("Passwort:"), b);
        b.gridx = 1;
        txtPass = new JPasswordField(18);
        box.add(txtPass, b);

        b.gridx = 0; b.gridy++; b.gridwidth = 2;
        lblInfo = new JLabel(" ");
        box.add(lblInfo, b);

        b.gridy++;
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnLogin = new JButton("Login");
        btnRegister = new JButton("Registrieren");
        btns.add(btnLogin);
        btns.add(btnRegister);
        box.add(btns, b);

        add(box);

        btnLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { doLogin(); }
        });

        btnRegister.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { doRegister(); }
        });
    }

    private void doLogin() {
        String u = txtUser.getText().trim();
        String p = new String(txtPass.getPassword());
        if (u.length() == 0 || p.length() == 0) {
            setInfo("Bitte Benutzername & Passwort eingeben.", true);
            return;
        }
        boolean ok = app.login(u, p);
        if (!ok) setInfo("Login fehlgeschlagen.", true);
    }

    private void doRegister() {
        String u = txtUser.getText().trim();
        String p = new String(txtPass.getPassword());

        if (u.length() < 3) { setInfo("Benutzername min. 3 Zeichen.", true); return; }
        if (p.length() < 4) { setInfo("Passwort min. 4 Zeichen.", true); return; }

        boolean ok = app.register(u, p);
        if (ok) setInfo("Registriert! Jetzt einloggen.", false);
        else setInfo("Benutzer existiert bereits.", true);
    }

    private void setInfo(String msg, boolean error) {
        lblInfo.setText(msg);
        lblInfo.setForeground(error ? new Color(160, 40, 40) : new Color(20, 120, 60));
    }
}
