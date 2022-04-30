package client.gui;

import client.comm.ClientComm;
import config.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainUI extends JFrame{
    protected CardLayout cardLayout;
    protected ClientComm comm;
    protected JPanel mainPanel;
    protected LoginUI loginUI;
    protected ChatUI chatUI;

    public MainUI() {
//        comm = new ClientComm();
//        cardLayout = new CardLayout();
//        mainPanel = new JPanel(cardLayout);
//        loginUI = new LoginUI(comm);
//        chatUI = new ChatUI(comm);
//        mainPanel.add(loginUI, "loginUI");
//        mainPanel.add(chatUI, "chatUI");
//
//        loginUI.loginButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                comm.login(loginUI.usernameField.getText(), new String(loginUI.passwordField.getPassword()));
//                JOptionPane.showMessageDialog(loginUI.loginButton, comm.getClientMsg());
//
//                comm.websocketConnection(comm.getToken());
//                JOptionPane.showMessageDialog(loginUI.loginButton, comm.getClientMsg());
//
//                if (comm.getWebSocketHandler().connected) {
//                    cardLayout.show(mainPanel, "chatUI");
//                }
//            }
//        });
//
//        chatUI.logOutButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                int input = JOptionPane.showConfirmDialog(chatUI.logOutButton, "Log out?", "Confirm", JOptionPane.OK_CANCEL_OPTION);
//                if (input == 0) { /// 0=ok, 2=cancel
//                    comm.setToken(null);
//                    cardLayout.show(mainPanel, "loginUI");
//                }
//            }
//        });
//
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        add(mainPanel);
//        setSize(800,600);
//        cardLayout.show(mainPanel, "loginUI");
//        setVisible(true);
//        pack();
    }

    public static void main(String[] args) throws InterruptedException {
        MainUI ui = new MainUI();
        ui.displayGUI();
    }

    private void displayGUI() {
        JFrame frame = new JFrame("Chat App");
        frame.setSize(800, 600);

        ClientComm comm = new ClientComm();
        LoginUI loginUI = new LoginUI(comm);
        ChatUI chatUI = new ChatUI(comm);


        loginUI.loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                comm.login(loginUI.usernameField.getText(), new String(loginUI.passwordField.getPassword()));
                JOptionPane.showMessageDialog(loginUI.loginButton, comm.getClientMsg());

                comm.websocketConnection(comm.getToken());
                JOptionPane.showMessageDialog(loginUI.loginButton, comm.getClientMsg());

                if (comm.getWebSocketHandler().connected) {
                    chatUI.availableRoomList.setListData(comm.getAvailableRoomList().toArray());
                    chatUI.username.setText("Username: " + comm.getUsername());

                    frame.getContentPane().remove(loginUI.panelMain);
                    frame.setContentPane(chatUI.panelMain);
                    frame.getContentPane().revalidate();
                    frame.setSize(800, 600);
                    frame.getContentPane().repaint();
                }
            }
        });

        chatUI.logOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int input = JOptionPane.showConfirmDialog(chatUI.logOutButton, "Log out?", "Confirm", JOptionPane.OK_CANCEL_OPTION);
                if (input == 0) { /// 0=ok, 2=cancel
                    comm.clear();

                    frame.getContentPane().remove(chatUI.panelMain);
                    frame.setContentPane(loginUI.panelMain);
                    frame.getContentPane().revalidate();
                    frame.setSize(800, 600);
                    frame.getContentPane().repaint();

                }
            }
        });

        frame.setSize(800, 600);
        frame.setContentPane(loginUI.panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.pack();
    }
}
