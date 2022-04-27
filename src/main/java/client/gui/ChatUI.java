package client.gui;

import client.comm.ClientComm;

import javax.swing.*;

public class ChatUI extends JPanel {
    private JList chatHistory;
    private JTextPane newMessage;
    private JButton sendButton;
    private JLabel roomIDLabel;
    private JButton logOutButton;
    private JLabel username;
    private JList invitedRoomList;
    private JButton acceptButton;
    private JButton rejectButton;
    private JButton createButton;
    private JButton inviteButton;
    private JButton joinButton;
    private JPanel panelMain;
    private JTextField roomIDSearch;
    private JTextField usernameSearch;
    private JTextField newCreatedRoom;
    private JList availableRoomList;
    private JButton enterButton;
    private JButton leaveButton;

    public ChatUI(ClientComm clientComm) {

        setUI();
    }

    private void setUI() {
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

    public JPanel getPanelMain() {
        return panelMain;
    }
}
