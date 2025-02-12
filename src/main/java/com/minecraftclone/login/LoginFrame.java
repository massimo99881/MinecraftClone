package com.minecraftclone.login;

import javax.swing.*;
import java.awt.*;
import com.minecraftclone.network.MyApi;
import com.minecraftclone.state.GameState;

public class LoginFrame extends JFrame {

    private JTextField emailField;
    private JButton loginButton, registerButton;
    private boolean loginOk = false;

    public LoginFrame() {
        super("Login");
        setLayout(new FlowLayout());

        emailField = new JTextField(20);
        loginButton = new JButton("Login");
        registerButton = new JButton("Registrati");

        add(new JLabel("Email:"));
        add(emailField);
        add(loginButton);
        add(registerButton);

        loginButton.addActionListener(e -> doLogin());
        registerButton.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
        });

        setSize(300, 120);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void doLogin() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Inserisci un'email!");
            return;
        }

        String result = MyApi.login(email);
        switch (result) {
            case "OK":
                loginOk = true;
                GameState.currentUserEmail = email;
                // Notifica al WS che l'utente si è connesso
                MyApi.connect(email);
                JOptionPane.showMessageDialog(this, "Login OK!");
                dispose();
                break;
            case "NOT_FOUND":
                JOptionPane.showMessageDialog(this, "Utente non presente nel database");
                break;
            case "NOT_ACTIVE":
                JOptionPane.showMessageDialog(this, "Utente non attivo");
                break;
            default:
                JOptionPane.showMessageDialog(this, "Errore: " + result);
        }
    }

    public boolean isLoginOk() {
        return loginOk;
    }
}
