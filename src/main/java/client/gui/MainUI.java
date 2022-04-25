package client.gui;

import javax.swing.*;
import java.awt.*;

public class MainUI extends JFrame {
    JPanel cards;
    LoginUI loginUI = new LoginUI();
    ChatUI chatUI = new ChatUI();

    public MainUI() {
        this.setSize(600, 400);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Chat App");
        this.setLocation(800, 100);

        cards = new JPanel();
        cards.setLayout(new CardLayout());

        cards.add(loginUI, "LOGIN");
        cards.add(chatUI, "CHAT");

        this.add(cards);
        this.setVisible(true);
}

    public static void main(String[] args)
    {
        new MainUI();
    }

}
