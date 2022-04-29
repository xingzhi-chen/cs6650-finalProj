package client.gui;

import client.comm.ClientComm;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginUI extends JPanel{
    protected JTextArea usernameField;
    protected JPasswordField passwordField;
    protected JButton signupButton;
    protected JButton loginButton;
    protected JLabel welcomeTitle;
    protected JLabel usernameTitle;
    protected JLabel passwordTitle;
    protected JPanel panelMain;

    protected ClientComm comm;

    public LoginUI(ClientComm comm){
        this.comm = comm;
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
