package com.minecraftclone;

import java.awt.FlowLayout;
import java.io.UnsupportedEncodingException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

class RegisterFrame extends JFrame {
    private JTextField emailField;
    private JButton registerBtn;

    public RegisterFrame() {
        super("Registrazione");
        setLayout(new FlowLayout());
        emailField = new JTextField(20);
        registerBtn = new JButton("Registra");
        add(new JLabel("Email:"));
        add(emailField);
        add(registerBtn);

        registerBtn.addActionListener(e -> doRegister());

        setSize(300, 100);
        setLocationRelativeTo(null);
    }

    private void doRegister() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Inserisci email");
            return;
        }

        String resp="";
		try {
			resp = MyApi.register(email);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // chiama /api/register
        if (resp.startsWith("REGISTERED")) {
            JOptionPane.showMessageDialog(this, "Registrazione completata! Controlla la mail per attivarti");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Errore: " + resp);
        }
    }
}
