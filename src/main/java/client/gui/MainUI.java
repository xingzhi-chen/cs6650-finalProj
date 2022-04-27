package client.gui;

import client.comm.ClientComm;

import javax.swing.*;

public class MainUI extends JFrame {
    ClientComm clientComm;
    LoginUI loginUI;
    ChatUI chatUI;

    public MainUI() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        clientComm = new ClientComm();
        loginUI = new LoginUI(clientComm);
        chatUI = new ChatUI(clientComm);
    }

    public void switchToChatPanel() {
        getContentPane().removeAll();
        getContentPane().invalidate();
        setContentPane(this.chatUI.getPanelMain());
        validate();
        setVisible(true);
    }

    public void switchToLoginPanel() {
        getContentPane().removeAll();
        getContentPane().invalidate();
        setContentPane(this.loginUI.getPanelMain());
        validate();
        setVisible(true);
    }


    public static void main(String[] args) throws InterruptedException {
        MainUI ui = new MainUI();

        ui.switchToLoginPanel();
        ui.pack();
        ui.setVisible(true);

        Thread.sleep(1000);
        ui.switchToChatPanel();
    }

}
