package client.gui;

import client.comm.ClientComm;
import config.ServerMsg;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

public class MainUI extends JFrame {
    protected ClientComm comm;
    protected LoginUI loginUI;
    protected ChatUI chatUI;

    public MainUI() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.comm = new ClientComm();
        this.loginUI = new LoginUI(comm);
        this.chatUI = new ChatUI(comm);
    }

    public void setLoginUI() {
        this.loginUI.signupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                comm.register(loginUI.usernameField.getText(), new String(loginUI.passwordField.getPassword()));
                JOptionPane.showMessageDialog(loginUI.signupButton, comm.getClientMsg());
            }
        });

        this.loginUI.loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                comm.login(loginUI.usernameField.getText(), new String(loginUI.passwordField.getPassword()));
                if (comm.getWebSocketHandler().connected) {
                    JOptionPane.showMessageDialog(loginUI.loginButton, "Connected");
                    switchToChatPanel();
                }
                else
                    JOptionPane.showMessageDialog(loginUI.loginButton, comm.getClientMsg());
            }
        });

        this.loginUI.setVisible(true);
    }

    public void setChatUI() {
        this.chatUI.logOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int input = JOptionPane.showConfirmDialog(chatUI.logOutButton,"Log out?", "Confirm", JOptionPane.OK_CANCEL_OPTION);
                if (input == 0) { /// 0=ok, 2=cancel
                    comm.setToken(null);
                    switchToLoginPanel();
                }
            }
        });

        this.chatUI.createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                comm.createRoom(comm.getToken());
                JOptionPane.showMessageDialog(chatUI.createButton, comm.getClientMsg());
                chatUI.availableRoomList.setListData(comm.getAvailableRoomList().toArray());
            }
        });

        this.chatUI.sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(chatUI.availableRoomList.getSelectedValue() == null) {
                    JOptionPane.showMessageDialog(chatUI.sendButton, "Please select a room to send message.");
                } else {
                    String msg = chatUI.newMessage.getText();
                    int roomID = (int) chatUI.availableRoomList.getSelectedValue();
                    comm.sendMessage(comm.getToken(), msg, roomID);
                }
            }
        });

        this.chatUI.availableRoomList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int roomID = (int) chatUI.availableRoomList.getSelectedValue();
                chatUI.roomIDLabel.setText("RoomID: " + roomID);
                //chatUI.chatHistory.setListData(comm.getChatHistory().get(roomID).toArray());
            }
        });



        updateChat();
        this.chatUI.availableRoomList.setListData(comm.getAvailableRoomList().toArray());
        this.chatUI.username.setText("Username: " + comm.getUsername());
        this.chatUI.setVisible(true);
    }

    public void switchToChatPanel() {
        getContentPane().removeAll();
        getContentPane().add(this.chatUI.panelMain);
        setChatUI();
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    public void switchToLoginPanel() {
        getContentPane().removeAll();
        getContentPane().add(this.loginUI.panelMain);
        setLoginUI();
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    private void updateChat(){
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (chatUI.availableRoomList.getSelectedValue() != null) {
                    int roomID = (int) chatUI.availableRoomList.getSelectedValue();
                    List<String> allMsgString = new ArrayList();
                    for (ServerMsg msg: comm.getChatHistory().get(roomID)) {
                        allMsgString.add(msgFormatter(msg));
                    }
                    chatUI.chatHistory.setListData(allMsgString.toArray());
                }
            }
        });
        timer.start();
    }

    private String msgFormatter(ServerMsg msg) {
        Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
        String d = format.format(new Date(msg.getTimestamp()));
        return String.format("[%s] %s: %s", d, msg.getFromUser(), msg.getMsg());
    }

    public static void main(String[] args) throws InterruptedException {
        MainUI ui = new MainUI();

        ui.getContentPane().add(ui.loginUI.panelMain);
        ui.setLoginUI();
        ui.setVisible(true);
        ui.pack();
    }

}
