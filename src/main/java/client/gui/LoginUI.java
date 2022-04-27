package client.gui;

import client.comm.ClientComm;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginUI extends JPanel{
    private JTextArea usernameField;
    private JPasswordField passwordField;
    private JButton signupButton;
    private JButton loginButton;
    private JLabel welcomeTitle;
    private JLabel usernameTitle;
    private JLabel passwordTitle;
    private JPanel panelMain;


    public LoginUI(ClientComm comm){
        signupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                comm.register(usernameField.getText(), new String(passwordField.getPassword()));
                JOptionPane.showMessageDialog(signupButton, comm.getClientMsg());
            }
        });

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                comm.login(usernameField.getText(), new String(passwordField.getPassword()));
                JOptionPane.showMessageDialog(loginButton, comm.getClientMsg());
            }
        });

        this.setVisible(true);
    }

    public JPanel getPanelMain() {
        return panelMain;
    }

    public static void main(String[] args) {
        JFrame f = new JFrame();

        LoginUI p = new LoginUI(new ClientComm());
        p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        f.setContentPane(p.panelMain);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        f.setTitle("The Title");
        f.setSize(600, 400);
        f.pack();
        f.setVisible(true);


    }
}
