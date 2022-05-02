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
                if (availableRoomList.getSelectedIndex() == -1) {
                    JOptionPane.showMessageDialog(sendButton, "Please select a room to send message.");
                } else {
                    String msg = newMessage.getText();
                    int roomIDIdx = availableRoomList.getSelectedIndex();
                    int roomID = comm.getAvailableRoomList().get(roomIDIdx);
                    comm.sendMessage(comm.getToken(), msg, roomID);

                    if (!comm.getClientMsg().equals("success"))
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

                if (!comm.getClientMsg().equals("success"))
                    JOptionPane.showMessageDialog(createButton, comm.getClientMsg());
            }
        });
    }

    public void setAcceptButton() {
        this.acceptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (invitedRoomList.getSelectedIndex() == -1) {
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
                if (invitedRoomList.getSelectedIndex() == -1) {
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

                    if (!comm.getClientMsg().equals("success"))
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
                    List<ServerMsg> msgList = comm.getChatHistory().get(roomID);
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

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panelMain = new JPanel();
        panelMain.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(10, 10, 10, 10), -1, -1));
        LUnit = new JPanel();
        LUnit.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panelMain.add(LUnit, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        LUnit.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        chatHistory = new JList();
        scrollPane1.setViewportView(chatHistory);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        LUnit.add(panel1, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_SOUTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(242, 60), null, 0, false));
        newMessage = new JTextPane();
        newMessage.setText("");
        newMessage.setToolTipText("New Message...");
        panel1.add(newMessage, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 60), null, 0, false));
        sendButton = new JButton();
        sendButton.setText("Send");
        panel1.add(sendButton, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(90, 30), null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        LUnit.add(panel2, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(-1, 30), null, 0, false));
        roomIDLabel = new JLabel();
        roomIDLabel.setText("RoomID:");
        panel2.add(roomIDLabel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        RUnit = new JPanel();
        RUnit.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        panelMain.add(RUnit, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, -1), null, 0, false));
        R3Unit = new JPanel();
        R3Unit.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        RUnit.add(R3Unit, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        inviteButton = new JButton();
        inviteButton.setText("Invite");
        R3Unit.add(inviteButton, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(90, 30), null, 0, false));
        roomIDSearch = new JTextField();
        roomIDSearch.setText("");
        roomIDSearch.setToolTipText("RoomID");
        R3Unit.add(roomIDSearch, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        usernameSearch = new JTextField();
        usernameSearch.setText("");
        usernameSearch.setToolTipText("Username");
        R3Unit.add(usernameSearch, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("username");
        R3Unit.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("roomID");
        R3Unit.add(label2, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        R1Unit = new JPanel();
        R1Unit.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        RUnit.add(R1Unit, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_SOUTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(-1, 30), null, 0, false));
        username = new JLabel();
        username.setHorizontalAlignment(0);
        username.setText("Username:");
        R1Unit.add(username, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(240, 30), null, 0, false));
        logOutButton = new JButton();
        logOutButton.setText("Log out");
        R1Unit.add(logOutButton, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(90, 30), new Dimension(90, 30), new Dimension(90, 30), 0, false));
        R2Unit = new JPanel();
        R2Unit.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        RUnit.add(R2Unit, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        R2Unit.add(scrollPane2, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        availableRoomList = new JList();
        scrollPane2.setViewportView(availableRoomList);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        R2Unit.add(panel3, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        panel3.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        createButton = new JButton();
        createButton.setText("Create");
        panel3.add(createButton, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(90, 30), new Dimension(90, 30), new Dimension(90, 30), 0, false));
        R4Unit = new JPanel();
        R4Unit.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        RUnit.add(R4Unit, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane3 = new JScrollPane();
        R4Unit.add(scrollPane3, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 3, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        invitedRoomList = new JList();
        scrollPane3.setViewportView(invitedRoomList);
        acceptButton = new JButton();
        acceptButton.setText("Accept");
        R4Unit.add(acceptButton, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(90, 30), new Dimension(90, 30), new Dimension(90, 30), 0, false));
        rejectButton = new JButton();
        rejectButton.setText("Reject");
        R4Unit.add(rejectButton, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(90, 30), new Dimension(90, 30), new Dimension(90, 30), 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        R4Unit.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panelMain;
    }
}
