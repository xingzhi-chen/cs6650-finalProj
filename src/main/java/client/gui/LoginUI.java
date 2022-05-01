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

    public LoginUI(ClientComm comm) {
        this.comm = comm;
        //setSignupButton();
        //setLoginButton();
    }

    public void initComponents() {
        setSignupButton();
    }

    public void setSignupButton () {
        signupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                if (password.length() < 4) {
                    JOptionPane.showMessageDialog(signupButton, "Password must be at least four characters long");
                    return;
                }
                comm.register(usernameField.getText(), new String(passwordField.getPassword()));
                JOptionPane.showMessageDialog(signupButton, comm.getClientMsg());
                usernameField.setText(null);
                passwordField.setText(null);
            }
        });
    }

    public void setLoginButton () {
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                comm.login(usernameField.getText(), new String(passwordField.getPassword()));
                if (comm.getToken() != null) {
                    comm.websocketConnection(comm.getToken());
                }
                JOptionPane.showMessageDialog(loginButton, "Connect " + comm.getClientMsg());
            }
        });
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
