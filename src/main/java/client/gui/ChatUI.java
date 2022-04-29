package client.gui;

import client.comm.ClientComm;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatUI extends JPanel {
    protected JList chatHistory;
    protected JTextPane newMessage;
    protected JButton sendButton;
    protected JLabel roomIDLabel;
    protected JButton logOutButton;
    protected JLabel username;
    protected JList invitedRoomList;
    protected JButton acceptButton;
    protected JButton rejectButton;
    protected JButton createButton;
    protected JButton inviteButton;
    protected JButton joinButton;
    protected JPanel panelMain;
    protected JTextField roomIDSearch;
    protected JTextField usernameSearch;
    protected JTextField newCreatedRoom;
    protected JList availableRoomList;
    protected JButton enterButton;
    protected JButton leaveButton;

    protected ClientComm comm;
    protected int currentRoom;

    public ChatUI(ClientComm comm) {
        this.comm = comm;
    }

    public static void main(String[] args) {
        JFrame f = new JFrame();

        ChatUI p = new ChatUI(new ClientComm());
        p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        f.setContentPane(p.panelMain);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        f.setTitle("The Title");
        f.setSize(600, 400);
        f.pack();
        f.setVisible(true);
    }

}
