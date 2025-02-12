package com.minecraftclone;

import java.awt.FlowLayout;
import java.io.UnsupportedEncodingException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

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
        registerButton.addActionListener(e -> new RegisterFrame().setVisible(true));

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

        // Chiamata REST:
        String result="";
		try {
			result = MyApi.login(email);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // "OK", "NOT_FOUND", "NOT_ACTIVE"
        switch (result) {
            case "OK":
                JOptionPane.showMessageDialog(this, "Login OK!");
                loginOk = true;
                dispose(); // chiudo la finestra
                break;
            case "NOT_FOUND":
                JOptionPane.showMessageDialog(this, "Utente non presente nel database");
                break;
            case "NOT_ACTIVE":
                JOptionPane.showMessageDialog(this, "Utente non attivo");
                break;
            default:
                JOptionPane.showMessageDialog(this, "Errore generico: " + result);
        }
    }

    public boolean isLoginOk() {
        return loginOk;
    }
}
