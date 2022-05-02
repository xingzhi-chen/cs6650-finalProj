package client.gui;

import client.comm.ClientComm;
import client.config.UIFormatter;
import config.ServerMsg;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;
import java.util.Vector;

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
    protected JPanel panelMain;
    protected JTextField roomIDSearch;
    protected JTextField usernameSearch;
    protected JList availableRoomList;
    private JPanel LUnit;
    private JPanel RUnit;
    private JPanel R1Unit;
    private JPanel R2Unit;
    private JPanel R4Unit;
    private JPanel R3Unit;

    protected ClientComm comm;
    protected Timer chatRoomTimer;
    protected Timer invitedTimer;

    public ChatUI(ClientComm comm) {
        this.comm = comm;
        //initComponents();
    }

    public void initComponents() {
        //setLogOutButton();
        setSendButton();
        setCreateButton();
        setAcceptButton();
        setRejectButton();
        setInviteButton();
        setInputFocus();
        setInputField();
        setChatUpdate();
        setInviteUpdate();
    }

    public void setLogOutButton() {
        this.logOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int input = JOptionPane.showConfirmDialog(logOutButton, "Log out?", "Confirm", JOptionPane.OK_CANCEL_OPTION);
                if (input == 0) { /// 0=ok, 2=cancel
                    comm.setToken(null);
                }
            }
        });
    }

    public void setSendButton() {
        this.sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(availableRoomList.getSelectedIndex() == -1) {
                    JOptionPane.showMessageDialog(sendButton, "Please select a room to send message.");
                } else {
                    String msg = newMessage.getText();
                    int roomIDIdx = availableRoomList.getSelectedIndex();
                    int roomID = comm.getAvailableRoomList().get(roomIDIdx);
                    comm.sendMessage(comm.getToken(), msg, roomID);

                    if (! comm.getClientMsg().equals("success"))
                        JOptionPane.showMessageDialog(sendButton, comm.getClientMsg());
                }
                newMessage.setText(null);
            }
        });
    }

    public void setCreateButton() {
        this.createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                comm.createRoom(comm.getToken());
                availableRoomList.setListData(UIFormatter.formatAvailableRoom(comm.getAvailableRoomList()).toArray()); // update available room list

                if (! comm.getClientMsg().equals("success"))
                    JOptionPane.showMessageDialog(createButton, comm.getClientMsg());
            }
        });
    }

    public void setAcceptButton() {
        this.acceptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(invitedRoomList.getSelectedIndex() == -1) {
                    JOptionPane.showMessageDialog(sendButton, "Please select a room to accept the invitation.");
                } else {
                    int roomIDIdx = invitedRoomList.getSelectedIndex();
                    int roomID = comm.getInvitedList().get(roomIDIdx).getRoomId();
                    comm.sendInvitationRsp(comm.getToken(), roomID, true);
                    invitedRoomList.setListData(UIFormatter.formatInvitedRoom(comm.getInvitedList()).toArray());  // update invited room list
                    availableRoomList.setListData(UIFormatter.formatAvailableRoom(comm.getAvailableRoomList()).toArray());
                }
            }
        });
    }

    public void setRejectButton() {
        this.rejectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(invitedRoomList.getSelectedIndex() == -1) {
                    JOptionPane.showMessageDialog(rejectButton, "Please select a room to reject the invitation.");
                } else {
                    int roomIDIdx = invitedRoomList.getSelectedIndex();
                    int roomID = comm.getInvitedList().get(roomIDIdx).getRoomId();
                    comm.sendInvitationRsp(comm.getToken(), roomID, false);
                    invitedRoomList.setListData(UIFormatter.formatInvitedRoom(comm.getInvitedList()).toArray());  // update invited room list
                    availableRoomList.setListData(comm.getAvailableRoomList().toArray());
                }
            }
        });
    }

    public void setInviteButton() {
        inviteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String username = usernameSearch.getText();
                    Integer roomID = Integer.parseInt(roomIDSearch.getText());
                    if (username == null || roomID == null)
                        throw new NumberFormatException();
                    comm.sendInvitation(comm.getToken(), username, roomID);

                    if (! comm.getClientMsg().equals("success"))
                        JOptionPane.showMessageDialog(inviteButton, comm.getClientMsg());
                } catch (NumberFormatException ne) {
                    JOptionPane.showMessageDialog(inviteButton, "Please enter a valid user and a valid room ID number.");
                }
                usernameSearch.setText("username");
                usernameSearch.setForeground(new Color(150, 150, 150));
                roomIDSearch.setText("roomID");
                roomIDSearch.setForeground(new Color(150, 150, 150));
            }
        });
    }

    public void setInputFocus() {
        usernameSearch.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                usernameSearch.setText("");
                usernameSearch.setForeground(new Color(50, 50, 50));
            }

            @Override
            public void focusLost(FocusEvent e) {
                usernameSearch.setForeground(new Color(150, 150, 150));
            }
        });

        roomIDSearch.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                roomIDSearch.setText("");
                roomIDSearch.setForeground(new Color(50, 50, 50));
            }

            @Override
            public void focusLost(FocusEvent e) {
                roomIDSearch.setForeground(new Color(150, 150, 150));
            }
        });

        newMessage.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                newMessage.setText("");
                newMessage.setForeground(new Color(50, 50, 50));
            }

            @Override
            public void focusLost(FocusEvent e) {
                newMessage.setForeground(new Color(150, 150, 150));
            }
        });
    }

    public void setInputField() {
        if (usernameSearch.getText().length() == 0) {
            usernameSearch.setText("username");
            usernameSearch.setForeground(new Color(150, 150, 150));
        }

        if (roomIDSearch.getText().length() == 0) {
            roomIDSearch.setText("roomID");
            roomIDSearch.setForeground(new Color(150, 150, 150));
        }

        if (newMessage.getText().length() == 0) {
            newMessage.setText("New Message...");
            newMessage.setForeground(new Color(150, 150, 150));
        }
    }

    public void setChatUpdate() {
        chatRoomTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (availableRoomList.getSelectedIndex() != -1) {
                    int roomIDIdx = availableRoomList.getSelectedIndex();
                    int roomID = comm.getAvailableRoomList().get(roomIDIdx);
                    List<ServerMsg> msgList= comm.getChatHistory().get(roomID);
                    List<String> msgString = UIFormatter.formatChat(msgList);
                    chatHistory.setListData(msgString.toArray());
                    roomIDLabel.setText("RoomID: " + roomID);
                }
            }
        });
    }

    public void setInviteUpdate() {
        invitedTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIdx = invitedRoomList.getSelectedIndex();

                List<ServerMsg> invitedList = comm.getInvitedList();
                invitedRoomList.setListData(UIFormatter.formatInvitedRoom(invitedList).toArray());

                if (selectedIdx != -1 && invitedList.size() > selectedIdx)
                    invitedRoomList.setSelectedIndex(selectedIdx);
            }
        });
    }

    public void cleanup() {
        availableRoomList.setListData(new Vector());
        invitedRoomList.setListData(new Vector());
        chatHistory.setListData(new Vector());
        username.setText("Username: ");
        newMessage.setText("");
        chatRoomTimer.stop();
        invitedTimer.stop();
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
