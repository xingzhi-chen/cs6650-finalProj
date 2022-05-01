package client.gui;

import client.comm.ClientComm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class MainGUI {
    JPanel cards;
    public void addComponentToPane(Container pane) {

        //Create the "cards"
        ClientComm comm = new ClientComm();
        LoginUI loginUI = new LoginUI(comm);
        ChatUI chatUI = new ChatUI(comm);

        //Create the panel that contains the "cards".
        cards = new JPanel(new CardLayout());
        cards.add(loginUI.panelMain, "login page");
        cards.add(chatUI.panelMain, "chat page");

        pane.add(cards, BorderLayout.CENTER);

        loginUI.initComponents();
        loginUI.loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                comm.login(loginUI.usernameField.getText(), new String(loginUI.passwordField.getPassword()));
                loginUI.usernameField.setText(null);
                loginUI.passwordField.setText(null);

                if (!comm.getClientMsg().equals("success")) {
                    JOptionPane.showMessageDialog(loginUI.loginButton, comm.getClientMsg());
                    return;
                }
                JOptionPane.showMessageDialog(loginUI.loginButton, "Connecting...");
                comm.websocketConnection(comm.getToken());

                JOptionPane.showMessageDialog(loginUI.loginButton, comm.getClientMsg());

                if (comm.getWebSocketHandler().connected) {
                    chatUI.availableRoomList.setListData(comm.getAvailableRoomList().toArray());
                    chatUI.username.setText("Username: " + comm.getUsername());
                    chatUI.setInputField();
                    chatUI.invitedTimer.start();
                    chatUI.chatRoomTimer.start();

                    // change page
                    CardLayout c1 = (CardLayout) (cards.getLayout());
                    c1.show(cards, "chat page");
                }
            }
        });

        chatUI.initComponents();
        chatUI.logOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int input = JOptionPane.showConfirmDialog(chatUI.logOutButton, "Log out?", "Confirm", JOptionPane.OK_CANCEL_OPTION);
                if (input == 0) { /// 0=ok, 2=cancel
                    comm.getWebSocketHandler().close();
                    comm.initComm();
                    chatUI.cleanup();

                    // change page
                    CardLayout c1 = (CardLayout) (cards.getLayout());
                    c1.show(cards, "login page");
                }
            }
        });
    }

    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Chat Room");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        //Create and set up the content pane.
        MainGUI demo = new MainGUI();
        demo.addComponentToPane(frame.getContentPane());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        /* Use an appropriate Look and Feel */
        try {
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        /* Turn off metal's use of bold fonts */
        UIManager.put("swing.boldMetal", Boolean.FALSE);


        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
