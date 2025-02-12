package com.minecraftclone.login;

import javax.swing.*;
import java.awt.*;
import com.minecraftclone.network.MyApi;

public class RegisterFrame extends JFrame {
    private JTextField emailField;
    private JButton registerBtn;

    public RegisterFrame() {
        super("Registrati");
        setLayout(new FlowLayout());
        emailField = new JTextField(20);
        registerBtn = new JButton("Registra");

        add(new JLabel("Email:"));
        add(emailField);
        add(registerBtn);

        registerBtn.addActionListener(e -> doRegister());

        setSize(300,100);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void doRegister() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Inserisci un'email!");
            return;
        }
        String resp = MyApi.register(email);
        if (resp.startsWith("REGISTERED")) {
            JOptionPane.showMessageDialog(this, "Registrazione completata! Controlla la mail per attivarti.");
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Errore: " + resp);
        }
    }
}
