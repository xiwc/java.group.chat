package cho.xwc.chat.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.TextArea;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.FontUIResource;

import cho.xwc.chat.server.Server;

public class DataPanel extends JPanel {

    private static final String DEFAULT_PORT = "52254"; // @jve:decl-index=0:
    private static final String DEFAULT_MUL_IP = "228.5.6.7"; // @jve:decl-index=0:
    private static final String DEFAULT_GMH_IP = "228.15.26.37";//广播消息监听IP

    private Border border = BorderFactory.createMatteBorder(1, 1, 1, 1,
            Color.BLUE);

    private JLabel lblIP = null;
    private JTextField txtIP = null;
    private JTextField txtPort = null;
    private JLabel lblPort = null;
    private JButton btnConnect = null;
    private JButton btnRefresh = null;
    private TextArea txtaChatContent = null;
    private TextArea txtaSendContent = null;
    private JButton btnSend = null;
    private JTextField txtUserName = null;
    private JLabel lblUserName = null;
    private JButton btnClear = null;
    private JComboBox userList = null;
    private boolean flag = true;
    private Server server = null;
    private Client client = null;
    private UserClientMgr userMgr = null;
    private String serverMsg = null;
    private FolderTransmit folderTransmit = null;
    private TrayIcon trayIcon = null;
    private JFrame frame = null;
    private PropertiesMgr propertiesMgr = null;
    private JRadioButton rbtnImMsg = null;
    private JPanel scrollPaneChat = null;
    private JPanel scrollPaneSend = null;

    private GroupMsgHandler commonMsgHandler = null; // @jve:decl-index=0:
    private List<String> listMulIpAndPort = new ArrayList<String>(); // @jve:decl-index=0:
    private List<String> listListenMulIpAndPort = new ArrayList<String>(); // @jve:decl-index=0:
    private List<GroupMsgHandler> listMsgHandler = new ArrayList<GroupMsgHandler>(); // @jve:decl-index=0:

    public static boolean isSending = false;
    public static boolean isRecving = false;

    public DataPanel(TrayIcon trayIcon, JFrame frame) {
        this.trayIcon = trayIcon;
        this.frame = frame;
        userMgr = new UserClientMgr(this);
        propertiesMgr = new PropertiesMgr();
        Object obj = propertiesMgr.readObject();
        if (obj != null && (obj instanceof List)) {
            listListenMulIpAndPort = (List<String>) obj;
        }
        initialize();

        // start common listen
        startCommonListen(getCommonMsgHandler());
        initGlobalFontSetting(new Font("华文楷体", Font.PLAIN, 14));

    }
  //设置全局字体
    public static void initGlobalFontSetting(Font fnt){
        FontUIResource fontRes = new FontUIResource(fnt);
        for(Enumeration keys = UIManager.getDefaults().keys(); keys.hasMoreElements();){
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if(value instanceof FontUIResource)
                UIManager.put(key, fontRes);
        }
    }
    public JFrame getFrame() {
        return frame;
    }

    public List<String> getListMulIpAndPort() {
        return listMulIpAndPort;
    }

    public List<String> getListListenMulIpAndPort() {
        return listListenMulIpAndPort;
    }

    public GroupMsgHandler getCommonMsgHandler() {
        if (commonMsgHandler == null) {
            try {
                commonMsgHandler = new GroupMsgHandler(DataPanel.DEFAULT_GMH_IP, 53241);
            } catch (IOException e) {
                e.printStackTrace();
                commonMsgHandler = null;
                JOptionPane.showMessageDialog(frame, "组播端口[53241]被占用.", "错误提示",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        return commonMsgHandler;
    }

    private boolean commFlg = true;

    public void startCommonListen(final GroupMsgHandler commMsgHandler) {
        if (commMsgHandler != null) {
            Runnable recThread = new Runnable() {

                @Override
                public void run() {
                    while (commFlg) {
                        String recMsg = commMsgHandler.receiveMsg();
                        if (recMsg.startsWith("request@")) {
                            String mulIp = txtIP.getText().trim();
                            String mulPort = txtPort.getText().trim();
                            String mulIpAndPort = mulIp + ":" + mulPort;
                            String comeFrom = recMsg.replace("request@", "");
                            if (btnConnect.getText().equals("断开")) {
                                commMsgHandler.sendMsg("response@" + comeFrom
                                        + "$" + mulIpAndPort);
                            }
                        } else if (recMsg.startsWith("response@")) {// response@xwc@228.5.6.7:23223$223.4.5.6:33444
                            recMsg = recMsg.replace("response@", "");
                            if (recMsg.startsWith(serverMsg + "$")) {
                                recMsg = recMsg.replace(serverMsg + "$", "");
                                if (!listMulIpAndPort.contains(recMsg)) {
                                    listMulIpAndPort.add(recMsg);
                                }
                            }
                        }
                    }
                }
            };
            commFlg = true;
            new Thread(recThread).start();
        }
    }

    private void initialize() {
        this.setLayout(null);
        // this.setPreferredSize(new Dimension(474, 400));
        this.setSize(new Dimension(503, 425));
        this.add(getLblIP(), null);
        this.add(getTxtIP(), null);
        this.add(getLblPort(), null);
        this.add(getTxtPort(), null);
        this.add(getBtnConnect(), null);
        this.add(getBtnSend(), null);
        this.add(getScrollPaneSend(), null);
        this.add(getScrollPaneChat(), null);
        this.add(getTxtUserName(), null);
        this.add(getLblUserName(), null);
        this.add(getBtnClear(), null);
        this.add(getUserList(), null);
        this.add(getBtnFolder(), null);
        this.add(getBtnRecv(), null);
        this.add(getLblInfo(), null);
        this.add(getRbtnImMsg(), null);
        this.add(getRbtnTop(), null);
        this.add(getRbtnEnter(), null);
        this.add(getRbtnGroupRefuse(), null);
        this.add(getBtnRefresh(), null);
        this.add(getBtnSetNet(), null);
    }

    public String getServerMsg() {
        return serverMsg;
    }

    private Timer timer = new Timer();

    public JPanel getScrollPaneChat() {
        if (scrollPaneChat == null) {
            scrollPaneChat = new JPanel();
            scrollPaneChat.setFont(new Font("华文楷体", Font.PLAIN, 14));
            scrollPaneChat.setBorder(BorderFactory.createTitledBorder(border,
                    "聊天内容-----------------------------------------["
                            + Utils.formatDate(new Date()) + "]"));
            scrollPaneChat.setBounds(new Rectangle(8, 25, 482, 226));
            scrollPaneChat.setLayout(new BorderLayout());
            scrollPaneChat.add(getTxtaChatContent(), BorderLayout.CENTER);

            timer.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                    scrollPaneChat.setBorder(BorderFactory.createTitledBorder(
                            border,
                            "聊天内容-----------------------------------------["
                                    + Utils.formatDate(new Date()) + "]"));
                }
            }, 1000, 1000);

        }
        return scrollPaneChat;
    }

    public JPanel getScrollPaneSend() {
        if (scrollPaneSend == null) {
            scrollPaneSend = new JPanel();
            scrollPaneSend.setFont(new Font("华文楷体", Font.PLAIN, 14));
            scrollPaneSend.setBorder(BorderFactory.createTitledBorder(border,
                    "发送消息"));
            scrollPaneSend.setBounds(new Rectangle(8, 275, 482, 109));
            scrollPaneSend.setLayout(new BorderLayout());
            scrollPaneSend.add(getTxtaSendContent(), BorderLayout.CENTER);
        }
        return scrollPaneSend;
    }

    public JRadioButton getRbtnImMsg() {
        if (rbtnImMsg == null) {
            rbtnImMsg = new JRadioButton();
            rbtnImMsg.setFont(new Font("华文楷体", Font.PLAIN, 14));
            rbtnImMsg.setText("重要信息");
            rbtnImMsg.setBounds(new Rectangle(3, 254, 85, 20));
            rbtnImMsg.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    txtaSendContent.setFocusable(true);
                    txtaSendContent.requestFocus();
                }
            });
        }
        return rbtnImMsg;
    }

    public JLabel getLblInfo() {
        if (lblInfo == null) {
            lblInfo = new JLabel();
            lblInfo.setFont(new Font("华文楷体", Font.PLAIN, 14));
            lblInfo.setForeground(Color.RED);
            lblInfo.setVisible(false);
            lblInfo.setBounds(new Rectangle(360, 252, 65, 20));
            lblInfo.setText("完成:  0%");
        }
        return lblInfo;
    }

    public JLabel getLblIP() {
        if (lblIP == null) {
            lblIP = new JLabel();
            lblIP.setFont(new Font("华文楷体", Font.PLAIN, 14));
            lblIP.setText("组播IP:");
            lblIP.setBounds(new Rectangle(9, 5, 50, 20));
        }
        return lblIP;
    }

    public JLabel getLblPort() {
        if (lblPort == null) {
            lblPort = new JLabel();
            lblPort.setFont(new Font("华文楷体", Font.PLAIN, 14));
            lblPort.setText("端口:");
            lblPort.setBounds(new Rectangle(150, 5, 33, 20));
        }
        return lblPort;
    }

    public JLabel getLblUserName() {
        if (lblUserName == null) {
            lblUserName = new JLabel();
            lblUserName.setFont(new Font("华文楷体", Font.PLAIN, 14));
            lblUserName.setText("用户名:");
            lblUserName.setBounds(new Rectangle(230, 4, 46, 20));
        }
        return lblUserName;
    }

    public JTextField getTxtIP() {
        if (txtIP == null) {
            txtIP = new JTextField();
            txtIP.setFont(new Font("华文楷体", Font.PLAIN, 14));
            txtIP.setBounds(new Rectangle(60, 5, 86, 20));
            String ip = propertiesMgr.getValueByKey("ip");
            if (ip != null) {
                txtIP.setText(ip);
            } else {
                txtIP.setText(DEFAULT_MUL_IP);
            }
        }
        return txtIP;
    }

    public JTextField getTxtPort() {
        if (txtPort == null) {
            txtPort = new JTextField();
            txtPort.setFont(new Font("华文楷体", Font.PLAIN, 14));
            txtPort.setBounds(new Rectangle(185, 5, 44, 20));
            String port = propertiesMgr.getValueByKey("port");
            if (port != null) {
                txtPort.setText(port);
            } else {
                txtPort.setText(DEFAULT_PORT);
            }

        }
        return txtPort;
    }

    private void setIpPortNameStatus(boolean value) {
        this.txtIP.setEnabled(value);
        this.txtPort.setEnabled(value);
        this.txtUserName.setEnabled(value);
        this.btnSend.setEnabled(!value);
        this.btnRefresh.setEnabled(!value);
        this.btnSetNet.setEnabled(value);
        if (value) {
            this.btnFolder.setEnabled(!value);
        }
    }

    public JButton getBtnRefresh() {
        if (btnRefresh == null) {
            btnRefresh = new JButton();
            btnRefresh.setFont(new Font("华文楷体", Font.PLAIN, 12));
            btnRefresh.setText("刷新");
            btnRefresh.setBounds(new Rectangle(300, 390, 60, 25));
            btnRefresh.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (listMsgHandler.size() > 0) {
                        userList.removeAllItems();
                        userList.addItem("组内群聊");
                        sendGroupMsgToAll("!#*$reFresh$#*!" + serverMsg);
                    }
                }
            });
        }
        return btnRefresh;
    }

    private String localMulIpAndPort = null;

    public JButton getBtnConnect() {
        if (btnConnect == null) {
            btnConnect = new JButton();
            btnConnect.setFont(new Font("华文楷体", Font.PLAIN, 12));
            btnConnect.setText("连接");
            btnConnect.setBounds(new Rectangle(360, 5, 60, 20));
            btnConnect.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    String btnText = btnConnect.getText();
                    if (btnText.equals("断开")) {
                        btnConnect.setText("连接");
                        setIpPortNameStatus(true);
                        sendGroupMsgToAll("!#*$outLine$#*!" + serverMsg);
                        leaveGroup();
                        userList.removeAllItems();
                        frame.setTitle("群组聊天器  (离线状态)");

                    } else {
                        String ip = txtIP.getText().trim();
                        int port = 0;
                        try {
                            port = Integer.parseInt(txtPort.getText().trim());
                            if (port < 30000 || port > 65535) {
                                JOptionPane.showMessageDialog(frame,
                                        "组播端口输入错误,请重新输入.[范围: 30000-65535]",
                                        "错误提示", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        } catch (Exception e2) {
                            JOptionPane.showMessageDialog(frame,
                                    "组播端口输入错误,请重新输入.[范围: 30000-65535]", "错误提示",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        try {
                            createConn();
                            userList.addItem("组内群聊");
                            sendGroupMsgToAll("!#*$onLine$#*!" + serverMsg);
                            btnConnect.setText("断开");
                            setIpPortNameStatus(false);

                        } catch (Exception e1) {
                            JOptionPane
                                    .showMessageDialog(
                                            frame,
                                            "组播IP输入错误,请重新输入.[范围: 224.0.0.1-239.255.255.255]",
                                            "错误提示", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                }

                private void createConn() throws IOException {
                    server = new Server(DataPanel.this);
                    String endpoint = server.listen();
                    serverMsg = txtUserName.getText().trim() + "@" + endpoint;

                    localMulIpAndPort = txtIP.getText().trim() + ":"
                            + txtPort.getText().trim();
                    if (!listListenMulIpAndPort.contains(localMulIpAndPort)) {
                        listListenMulIpAndPort.add(0, localMulIpAndPort);
                    } else {
                        listListenMulIpAndPort.remove(localMulIpAndPort);
                        listListenMulIpAndPort.add(0, localMulIpAndPort);
                    }
                    flag = true;
                    for (int i = 0; i < listListenMulIpAndPort.size(); i++) {
                        String ipt = listListenMulIpAndPort.get(i);
                        int indexMH = ipt.indexOf(":");
                        String ip = ipt.substring(0, indexMH);
                        int port = Integer.parseInt(ipt.substring(indexMH + 1));

                        GroupMsgHandler msgHandler = createGroupMsgHandler(ip,
                                port);
                        listMsgHandler.add(msgHandler);
                    }
                }

                private GroupMsgHandler createGroupMsgHandler(String ip,
                        int port) throws IOException {
                    final GroupMsgHandler msgHandler = new GroupMsgHandler(ip,
                            port);
                    Runnable recThread = new Runnable() {
                        @Override
                        public void run() {
                            while (flag) {
                                String recMsg = msgHandler.receiveMsg();

                                if (recMsg.startsWith("△imXwc▲")) {
                                    recMsg = recMsg.substring(7);
                                    if (!isOwnSend(recMsg)) {
                                        WarningFrame.showMsg(recMsg);
                                    }
                                }
                                if (!recMsg.startsWith("!#*$")) {
                                    if (!rbtnGroupRefuse.isSelected()) {
                                        if (!isOwnSend(recMsg)) {
                                            appendChatMsg(recMsg);
                                        }
                                        if (!frame.isVisible()
                                                && MainFrame.WarningFlag) {
                                            trayIcon.displayMessage("群聊消息",
                                                    recMsg, MessageType.INFO);
                                        }
                                    }
                                } else {
                                    if (recMsg.startsWith("!#*$onLine$#*!")) {//用户上线
                                        recMsg = recMsg.replace(
                                                "!#*$onLine$#*!", "");
                                        userMgr.onLine(recMsg, trayIcon);
                                        msgHandler.sendMsg("!#*$reBack$#*!"
                                                + recMsg + "$" + serverMsg);
                                    } else if (recMsg
                                            .startsWith("!#*$outLine$#*!")) {//用户下线
                                        recMsg = recMsg.replace(
                                                "!#*$outLine$#*!", "");
                                        userMgr.outLine(recMsg, trayIcon);
                                    } else if (recMsg
                                            .startsWith("!#*$reBack$#*!")) {//响应信息
                                        recMsg = recMsg.replace(
                                                "!#*$reBack$#*!", "");
                                        if (recMsg.startsWith(serverMsg + "$")) {
                                            recMsg = recMsg.replace(serverMsg
                                                    + "$", "");
                                            userMgr.onLine(recMsg, null);
                                        }
                                    } else if (recMsg
                                            .startsWith("!#*$reFresh$#*!")) {//刷新信息
                                        recMsg = recMsg.replace(
                                                "!#*$reFresh$#*!", "");
                                        msgHandler.sendMsg("!#*$reBack$#*!"
                                                + recMsg + "$" + serverMsg);
                                    } else {
                                        if (!rbtnGroupRefuse.isSelected()) {
                                            if (!isOwnSend(recMsg)) {
                                                appendChatMsg(recMsg);
                                            }
                                            if (!frame.isVisible()
                                                    && MainFrame.WarningFlag) {
                                                trayIcon.displayMessage("群聊消息",
                                                        recMsg,
                                                        MessageType.INFO);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        private boolean isOwnSend(String msg) {
                            String[] msgs = msg.split("\n");
                            int indexL = msgs[0].lastIndexOf("(");
                            int indexR = msgs[0].lastIndexOf(")");

                            String name = msgs[0].substring(0, indexL);
                            String ip = msgs[0].substring(indexL + 1, indexR);
                            if (serverMsg.startsWith(name)
                                    && serverMsg.contains(ip)) {
                                return true;
                            } else {
                                return false;
                            }
                        }
                    };
                    new Thread(recThread).start();
                    return msgHandler;
                }

            });
        }
        return btnConnect;
    }

    private String senderInfo = ""; 

    public synchronized void appendChatMsg(String msg) {
        String sender = msg.substring(0, msg.indexOf("\n")).trim();
        if (!senderInfo.equals(sender)) {
            senderInfo = sender;
            txtaChatContent.append(msg);
            txtaChatContent.append("\n");
        }
    }

    private void sendGroupMsg(String msg) {
        if (listMsgHandler.size() > 0) {
            listMsgHandler.get(0).sendMsg(msg);
        }
    }

    private void sendGroupMsgToAll(String msg) {
        for (GroupMsgHandler handler : listMsgHandler) {
            handler.sendMsg(msg);
        }
    }

    private void leaveGroup() {
        flag = false;
        for (GroupMsgHandler handler : listMsgHandler) {
            handler.leaveGroup();
            handler = null;
        }
        listMsgHandler.clear();
        listListenMulIpAndPort.remove(localMulIpAndPort);

        server.stop();
        server = null;
    }

    public TextArea getTxtaChatContent() {
        if (txtaChatContent == null) {
            txtaChatContent = new TextArea();
//            txtaChatContent.setFont(new Font(getFont().getFontName(), getFont()
//                    .getStyle(), getFont().getSize() + 3));
            txtaChatContent.setEditable(false);
            txtaChatContent.setBackground(Color.WHITE);

            final JPopupMenu popupMenu_1 = new JPopupMenu();
            addPopup(txtaChatContent, popupMenu_1);

            final JMenuItem newItemMenuItem = new JMenuItem();
            newItemMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    if (!txtaChatContent.getText().trim().isEmpty()) {
                        ChatRecordMgr chatRecordMgr = new ChatRecordMgr(
                                txtaChatContent.getText());
                        chatRecordMgr.save();
                        txtaChatContent.setText("");
                    }
                }
            });
            newItemMenuItem.setText("保存聊天记录");
            popupMenu_1.add(newItemMenuItem);

            popupMenu_1.addSeparator();

            final JMenuItem newItemMenuItem2 = new JMenuItem();
            newItemMenuItem2.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    new ChatRecordMgr("").openChatRecord();
                }
            });
            newItemMenuItem2.setText("打开聊天记录");
            popupMenu_1.add(newItemMenuItem2);

            popupMenu_1.addSeparator();

            final JMenuItem newItemMenuItem3 = new JMenuItem();
            newItemMenuItem3.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    txtaChatContent.setText("");
                }
            });
            newItemMenuItem3.setText("清空聊天窗口");
            popupMenu_1.add(newItemMenuItem3);

            popupMenu_1.addSeparator();

            final JMenuItem newItemMenuItem4 = new JMenuItem();
            newItemMenuItem4.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    new ChatRecordMgr("").openMemo();
                }
            });
            newItemMenuItem4.setText("打开备忘记录");
            popupMenu_1.add(newItemMenuItem4);

            popupMenu_1.addSeparator();

            final JMenuItem newItemMenuItem5 = new JMenuItem();
            newItemMenuItem5.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    new ChatRecordMgr("").openRecordDir();
                }
            });
            newItemMenuItem5.setText("打开所在目录");
            popupMenu_1.add(newItemMenuItem5);

            txtaChatContent.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyPressed(java.awt.event.KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_F1) {
                        File dir = new File(Utils.getUserDir()
                                + "GroupChat_XWC/ExecProgram/Exec_F1");
                        dir.mkdirs();
                        File[] files = dir.listFiles();
                        Desktop desktop = Desktop.getDesktop();
                        for (File file : files) {
                            if (file.isFile()) {
                                try {
                                    desktop.open(file);
                                } catch (IOException ee) {
                                    ee.printStackTrace();
                                }
                            }
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_F2) {
                        File dir = new File(Utils.getUserDir()
                                + "GroupChat_XWC/ExecProgram/Exec_F2");
                        dir.mkdirs();
                        File[] files = dir.listFiles();
                        Desktop desktop = Desktop.getDesktop();
                        for (File file : files) {
                            if (file.isFile()) {
                                try {
                                    desktop.open(file);
                                } catch (IOException ee) {
                                    ee.printStackTrace();
                                }
                            }
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_F3) {
                        File dir = new File(Utils.getUserDir()
                                + "GroupChat_XWC/ExecProgram/Exec_F3");
                        dir.mkdirs();
                        File[] files = dir.listFiles();
                        Desktop desktop = Desktop.getDesktop();
                        for (File file : files) {
                            if (file.isFile()) {
                                try {
                                    desktop.open(file);
                                } catch (IOException ee) {
                                    ee.printStackTrace();
                                }
                            }
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_F4) {
                        File dir = new File(Utils.getUserDir()
                                + "GroupChat_XWC/ExecProgram/Exec_F4");
                        dir.mkdirs();
                        File[] files = dir.listFiles();
                        Desktop desktop = Desktop.getDesktop();
                        for (File file : files) {
                            if (file.isFile()) {
                                try {
                                    desktop.open(file);
                                } catch (IOException ee) {
                                    ee.printStackTrace();
                                }
                            }
                        }
                    }
                }
            });
            txtaChatContent.addMouseWheelListener(new MouseWheelListener() {

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    if (e.isControlDown()) {
                        e.consume();
                        Font dFont = txtaChatContent.getFont();
                        int size = dFont.getSize();
                        size -= e.getUnitsToScroll();
                        Font font = new Font(dFont.getFontName(), dFont
                                .getStyle(), size);
                        txtaChatContent.setFont(font);
                    }
                }
            });
        }
        return txtaChatContent;
    }

    public TextArea getTxtaSendContent() {
        if (txtaSendContent == null) {
            txtaSendContent = new TextArea();
//            txtaSendContent.setFont(new Font(getFont().getFontName(), getFont()
//                    .getStyle(), getFont().getSize() + 3));

            final JPopupMenu popupMenu_1 = new JPopupMenu();
            addPopup(txtaSendContent, popupMenu_1);

            final JMenuItem newItemMenuItem = new JMenuItem();
            newItemMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    if (!txtaSendContent.getText().trim().isEmpty()) {
                        txtaSendContent.setText(Utils
                                .trimLinesStartByFirstLine(txtaSendContent
                                        .getText()));
                    }
                }
            });
            newItemMenuItem.setText("去掉行前空白");
            popupMenu_1.add(newItemMenuItem);

            popupMenu_1.addSeparator();

            final JMenuItem newItemMenuItem_2 = new JMenuItem();
            newItemMenuItem_2.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    if (!txtaSendContent.getText().trim().isEmpty()) {
                        new ChatRecordMgr(txtaSendContent.getText()).saveMemo();
                        btnSend.doClick();
                    }
                }
            });
            newItemMenuItem_2.setText("保存为备忘");
            popupMenu_1.add(newItemMenuItem_2);

            txtaSendContent.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyPressed(java.awt.event.KeyEvent e) {
                    if (rbtnEnter.isSelected()) {
                        if (!e.isControlDown()
                                && e.getKeyCode() == KeyEvent.VK_ENTER) {
                            btnSend.doClick();
                            e.consume();
                        }
                    } else {
                        if (e.isControlDown()
                                && e.getKeyCode() == KeyEvent.VK_ENTER) {
                            btnSend.doClick();
                            e.consume();
                        }
                    }

                    // 备忘记录信息保存
                    if (e.isAltDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
                        if (!txtaSendContent.getText().isEmpty()) {
                            ChatRecordMgr cmMgr = new ChatRecordMgr(
                                    txtaSendContent.getText());
                            cmMgr.saveMemo();
                            btnSend.doClick();
                        }
                        e.consume();
                    }
                    if (e.getKeyCode() == KeyEvent.VK_F1) {
                        File dir = new File(Utils.getUserDir()
                                + "GroupChat_XWC/ExecProgram/Exec_F1");
                        dir.mkdirs();
                        File[] files = dir.listFiles();
                        Desktop desktop = Desktop.getDesktop();
                        for (File file : files) {
                            if (file.isFile()) {
                                try {
                                    desktop.open(file);
                                } catch (IOException ee) {
                                    ee.printStackTrace();
                                }
                            }
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_F2) {
                        File dir = new File(Utils.getUserDir()
                                + "GroupChat_XWC/ExecProgram/Exec_F2");
                        dir.mkdirs();
                        File[] files = dir.listFiles();
                        Desktop desktop = Desktop.getDesktop();
                        for (File file : files) {
                            if (file.isFile()) {
                                try {
                                    desktop.open(file);
                                } catch (IOException ee) {
                                    ee.printStackTrace();
                                }
                            }
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_F3) {
                        File dir = new File(Utils.getUserDir()
                                + "GroupChat_XWC/ExecProgram/Exec_F3");
                        dir.mkdirs();
                        File[] files = dir.listFiles();
                        Desktop desktop = Desktop.getDesktop();
                        for (File file : files) {
                            if (file.isFile()) {
                                try {
                                    desktop.open(file);
                                } catch (IOException ee) {
                                    ee.printStackTrace();
                                }
                            }
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_F4) {
                        File dir = new File(Utils.getUserDir()
                                + "GroupChat_XWC/ExecProgram/Exec_F4");
                        dir.mkdirs();
                        File[] files = dir.listFiles();
                        Desktop desktop = Desktop.getDesktop();
                        for (File file : files) {
                            if (file.isFile()) {
                                try {
                                    desktop.open(file);
                                } catch (IOException ee) {
                                    ee.printStackTrace();
                                }
                            }
                        }
                    }
                }
            });
            txtaSendContent.addMouseWheelListener(new MouseWheelListener() {

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    if (e.isControlDown()) {
                        e.consume();
                        Font dFont = txtaSendContent.getFont();
                        int size = dFont.getSize();
                        size -= e.getUnitsToScroll();
                        Font font = new Font(dFont.getFontName(), dFont
                                .getStyle(), size);
                        txtaSendContent.setFont(font);
                    }
                }
            });
        }
        return txtaSendContent;
    }

    public JButton getBtnSend() {
        if (btnSend == null) {
            btnSend = new JButton();
            btnSend.setFont(new Font("华文楷体", Font.PLAIN, 12));
            btnSend.setText("发送");
            btnSend.setBounds(new Rectangle(430, 390, 60, 25));
            btnSend.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    String sendMsg = txtaSendContent.getText();
                    if (sendMsg.trim().isEmpty()) {
                        return;
                    } else {
                        if (folderTransmit != null
                                && !txtaSendContent.isEditable()) {
                            if (isToMyself(userList.getSelectedItem()
                                    .toString())) {
                                // txtaChatContent.append("不能给自己发送文件!\n\n");
                                appendChatMsg("不能给自己发送文件!\n");
                                btnClear.doClick();
                                return;
                            }
                            folderTransmit.send();
                            btnRecv.setText("取消");
                            btnRecv.setVisible(true);
                            lblInfo.setText("等待中...");
                            lblInfo.setVisible(true);
                            txtaSendContent.setEditable(true);
                            txtaSendContent.setText("");
                            btnFolder.setEnabled(false);
                        } else {
                            String say = "";
                            String chatType = userList.getSelectedItem()
                                    .toString();
                            if ("组内群聊".equals(chatType) == false) {
                                say = buildSendMsg(sendMsg, "私聊信息");
                                if (client != null) {
                                    client.sendMsg(say);
                                }
                                if (say.startsWith("△imXwc▲")) {
                                    say = say.substring(7);
                                }
                                // txtaChatContent.append(say + "\n");
                                appendChatMsg(say);
                            } else {
                                if (!rbtnGroupRefuse.isSelected()) {
                                    say = buildSendMsg(sendMsg, "群发信息");
                                    sendGroupMsgToAll(say);
                                    if (say.startsWith("△imXwc▲")) {
                                        say = say.substring(7);
                                    }
                                    // txtaChatContent.append(say + "\n");
                                    appendChatMsg(say);
                                } else {
                                    return;
                                }
                            }
                            txtaSendContent.setText("");
                            txtaSendContent.setFocusable(true);
                            txtaSendContent.requestFocus();
                        }
                    }
                }

                private boolean isToMyself(String serMsg) {
                    if (serMsg.equals(serverMsg)) {
                        return true;
                    }
                    return false;
                }

                private String buildSendMsg(String sendMsg, String msgType) {
                    String[] sendMsgs = sendMsg.split("\n");
                    sendMsg = "\n";
                    for (String msg : sendMsgs) {
                        sendMsg += ("\n  " + msg);
                    }
                    String userName = txtUserName.getText();
                    StringBuilder sb = new StringBuilder();
                    try {
                        userName += ("("
                                + InetAddress.getLocalHost().getHostAddress()
                                + ") 【" + msgType + "】");
                    } catch (UnknownHostException e1) {
                        e1.printStackTrace();
                    }
                    String nowTime = getNowTime();
                    if (rbtnImMsg.isSelected()) {
                        sb.append("△imXwc▲");
                        rbtnImMsg.setSelected(false);
                    }
                    sb.append(userName).append(" 说：  ").append(nowTime).append(
                            sendMsg).append("\n");
                    return sb.toString();
                }

                private String getNowTime() {
                    Calendar now = Calendar.getInstance();
                    String nowTime = paddingTime(now.get(Calendar.HOUR), 0)
                            + ":" + paddingTime(now.get(Calendar.MINUTE), 1)
                            + ":" + paddingTime(now.get(Calendar.SECOND), 2);
                    return nowTime;
                }

                private String paddingTime(int value, int type) {
                    String ret = "" + value;
                    if (value < 10) {
                        ret = "0" + value;
                    }
                    if (ret.equals("00") && type == 0) {
                        ret = "12";
                    }
                    return ret;
                }
            });
        }
        return btnSend;
    }

    /**
     * This method initializes txtUserName
     *
     * @return javax.swing.JTextField
     */
    public JTextField getTxtUserName() {
        if (txtUserName == null) {
            txtUserName = new JTextField();
            txtUserName.setFont(new Font("华文楷体", Font.PLAIN, 14));
            txtUserName.setBounds(new Rectangle(282, 5, 70, 20));
            String userName = propertiesMgr.getValueByKey("username");
            if (userName != null) {
                txtUserName.setText(userName);
            } else {
                try {
                    String hostName = InetAddress.getLocalHost().getHostName();
                    txtUserName.setText(hostName);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }
        return txtUserName;
    }

    /**
     * This method initializes btnClear
     *
     * @return javax.swing.JButton
     */
    public JButton getBtnClear() {
        if (btnClear == null) {
            btnClear = new JButton();
            btnClear.setFont(new Font("华文楷体", Font.PLAIN, 12));
            btnClear.setBounds(new Rectangle(10, 390, 60, 25));
            btnClear.setText("共享");
            btnClear.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
//                    txtaSendContent.setText("");
//                    txtaSendContent.setEditable(true);
//                    txtaSendContent.setFocusable(true);
//                    txtaSendContent.requestFocus();
                	FileShareFrame frame = new FileShareFrame();
                	frame.setOnLineUsers(getOnLineUsers());
                	frame.setVisible(true);
                }
            });
        }
        return btnClear;
    }

    protected List<String> getOnLineUsers() {
    	List<String> onLineUserList = new ArrayList<String>();
    	synchronized (userList) {
    		for (int i = 1; i < this.userList.getItemCount(); i++) {
				onLineUserList.add(userList.getItemAt(i).toString());
			}
		}
		return onLineUserList;
	}

	private boolean isFirstTime = true;
    private JButton btnFolder = null;
    private JButton btnRecv = null;
    private JLabel lblInfo = null;
    private JRadioButton rbtnTop = null;
    private JRadioButton rbtnEnter = null;
    private JRadioButton rbtnGroupRefuse = null;
    private JButton btnSetNet = null;
    private JPopupMenu popupMenu = null; // @jve:decl-index=0:

    public JPopupMenu getPopupMenu() {
        if (popupMenu == null) {
            popupMenu = new JPopupMenu();
            JMenuItem item01 = new JMenuItem("组员选择发送");
            item01.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out
                            .println("DataPanel.getPopupMenu().new ActionListener() {...}.actionPerformed()");
                }
            });
            popupMenu.add(item01);
            popupMenu.setInvoker(getUserList());
        }
        return popupMenu;
    }

    public JComboBox getUserList() {
        if (userList == null) {
            userList = new JComboBox();
            userList.setFont(new Font("华文楷体", Font.PLAIN, 14));
            userList.setComponentPopupMenu(getPopupMenu());
            userList.setBounds(new Rectangle(75, 390, 220, 25));
            userList.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    if (isFirstTime == false) {
                        isFirstTime = true;
                        return;
                    }
                    String chatType = userList.getSelectedItem().toString();
                    scrollPaneSend
                            .setBorder(BorderFactory
                                    .createTitledBorder(border,
                                            "发送消息---------到---------["
                                                    + chatType + "]"));
                    if ("组内群聊".equals(chatType) == false) {
                        if (client != null) {
                            client.stop();
                            client = null;
                        }
                        if (!chatType.equals(serverMsg)) {
                            btnFolder.setEnabled(true);
                            client = new Client(chatType, DataPanel.this);
                            client.connect();
                        } else {
                            btnFolder.setEnabled(false);
                            // txtaChatContent.append("此时发送消息只限自己查看(可以保存在聊天记录中)!\n\n");
                            appendChatMsg("此时发送消息只限自己查看(可以保存在聊天记录中)!\n");
                        }
                    } else {
                        btnFolder.setEnabled(false);
                        if (client != null) {
                            client.stop();
                            client = null;
                        }
                    }
                    txtaSendContent.setFocusable(true);
                    txtaSendContent.requestFocus();
                    isFirstTime = false;
                }
            });
        }
        return userList;
    }

    public void close() {

        commFlg = false;
        if (btnConnect.getText().equals("断开")) {
            sendGroupMsgToAll("!#*$outLine$#*!" + serverMsg);
        }
        if (!txtaChatContent.getText().trim().isEmpty()
                && MainFrame.ChatRecordSaveFlag) {
            ChatRecordMgr chatRecordMgr = new ChatRecordMgr(txtaChatContent
                    .getText());
            chatRecordMgr.save();
        }

        Properties properties = new Properties();
        properties.setIp(txtIP.getText().trim());
        int port = 0;
        try {
            port = Integer.parseInt(txtPort.getText().trim());
        } catch (Exception e) {
            port = Integer.parseInt(DEFAULT_PORT);
        }
        properties.setPort(port);
        properties.setUserName(txtUserName.getText().trim());
        PropertiesMgr propertiesMgr = new PropertiesMgr(properties);
        propertiesMgr.save();
        propertiesMgr.saveObject(listListenMulIpAndPort);
    }

    /**
     * This method initializes btnFolder
     *
     * @return javax.swing.JButton
     */
    public JButton getBtnFolder() {
        if (btnFolder == null) {
            btnFolder = new JButton();
            btnFolder.setFont(new Font("华文楷体", Font.PLAIN, 12));
            btnFolder.setEnabled(false);
            btnFolder.setBounds(new Rectangle(365, 390, 60, 25));
            btnFolder.setText("文件");
            btnFolder.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (DataPanel.isSending) {
                        // txtaChatContent.append("一次只能给一个人发送文件,请等待...\n\n");
                        appendChatMsg("一次只能给一个人发送文件,请等待...\n");
                        return;
                    }
                    if (DataPanel.isRecving) {
                        // txtaChatContent.append("接收文件的同时不能发送文件,请等待...\n\n");
                        appendChatMsg("接收文件的同时不能发送文件,请等待...\n");
                        return;
                    }
                    JFileChooser chooser = new JFileChooser();
                    chooser
                            .setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    int ret = chooser.showOpenDialog(null);
                    if (ret == JFileChooser.OPEN_DIALOG) {
                        String path = chooser.getSelectedFile()
                                .getAbsolutePath();
                        String folderName = new File(path).getName();
                        if (folderName.trim().isEmpty()) {
                            // txtaChatContent.append("不能发送整个盘,请选择某一个文件夹.\n\n");
                            appendChatMsg("不能发送整个盘,请选择某一个文件夹.\n");
                            return;
                        }
                        txtaSendContent.setText(path);
                        txtaSendContent.setEditable(false);
                        folderTransmit = new FolderTransmit(path, userList
                                .getSelectedItem().toString(), DataPanel.this);
                    }
                }
            });
        }
        return btnFolder;
    }

    /**
     * This method initializes btnRecv
     *
     * @return javax.swing.JButton
     */
    public JButton getBtnRecv() {
        if (btnRecv == null) {
            btnRecv = new JButton();
            btnRecv.setFont(new Font("华文楷体", Font.PLAIN, 12));
            btnRecv.setVisible(false);
            btnRecv.setBounds(new Rectangle(430, 252, 60, 20));
            btnRecv.setText("接收");
            btnRecv.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    String btnText = btnRecv.getText();
                    if ("接收".equals(btnText)) {
                        btnRecv.setText("断开");
                        JFileChooser chooser = new JFileChooser();
                        chooser
                                .setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                        // chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                        int ret = chooser.showOpenDialog(null);
                        if (ret == JFileChooser.OPEN_DIALOG) {
                            File file = chooser.getSelectedFile();
                            String path = file.getAbsolutePath();
                            lblInfo.setVisible(true);
                            // if(file.isFile()){
                            // String fileName = file.getName();
                            // path = path.substring(0, path.length() -
                            // fileName.length() -1);
                            // }
                            // System.out.println(path);
                            server.receiveFolder(path);
                        }

                    } else if ("断开".equals(btnText)) {
                        btnRecv.setText("接收");
                        server.breakRecvFolder();
                    } else if ("取消".equals(btnText)) {
                        DataPanel.isSending = false;
                        btnRecv.setText("接收");
                        btnRecv.setVisible(false);
                        lblInfo.setVisible(false);
                        btnFolder.setEnabled(true);
                        if (folderTransmit != null) {
                            folderTransmit.stop();
                            folderTransmit = null;
                        }
                    }
                }
            });
        }
        return btnRecv;
    }

    public void displayMsg(String caption, String warning) {
        if (!frame.isVisible() && MainFrame.WarningFlag) {
            trayIcon.displayMessage(caption, warning, MessageType.INFO);
        }
    }

    public void RebackDefault() {
        txtIP.setText(DEFAULT_MUL_IP);
        txtPort.setText(DEFAULT_PORT);
        String hostName;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
            txtUserName.setText(hostName);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method initializes rbtnTop
     *
     * @return javax.swing.JRadioButton
     */
    public JRadioButton getRbtnTop() {
        if (rbtnTop == null) {
            rbtnTop = new JRadioButton();
            rbtnTop.setFont(new Font("华文楷体", Font.PLAIN, 14));
            rbtnTop.setBounds(new Rectangle(86, 254, 85, 20));
            rbtnTop.setText("窗口顶置");
            rbtnTop.addChangeListener(new javax.swing.event.ChangeListener() {
                public void stateChanged(javax.swing.event.ChangeEvent e) {
                    frame.setAlwaysOnTop(rbtnTop.isSelected());
                }
            });
            rbtnTop.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    txtaSendContent.setFocusable(true);
                    txtaSendContent.requestFocus();
                }
            });
        }
        return rbtnTop;
    }

    /**
     * This method initializes rbtnEnter
     *
     * @return javax.swing.JRadioButton
     */
    private JRadioButton getRbtnEnter() {
        if (rbtnEnter == null) {
            rbtnEnter = new JRadioButton();
            rbtnEnter.setFont(new Font("华文楷体", Font.PLAIN, 14));
            rbtnEnter.setBounds(new Rectangle(254, 254, 100, 20));
            rbtnEnter.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    txtaSendContent.setFocusable(true);
                    txtaSendContent.requestFocus();
                }
            });
            rbtnEnter.setText("按Enter发送");
        }
        return rbtnEnter;
    }

    public void setOnlineCount() {
        if (btnConnect.getText().equals("连接")) {
            this.frame.setTitle("群组聊天器  (离线状态)");
            this.trayIcon.setToolTip("群组聊天器_XWC\n(离线状态)");
        } else {
            int count = userList.getItemCount() - 1;
            this.frame.setTitle("群组聊天器  (在线人数: " + count + " )");
            this.trayIcon.setToolTip("群组聊天器_XWC\n(在线人数: " + count + " )");
        }
    }

    /**
     * This method initializes rbtnGroupRefuse
     *
     * @return javax.swing.JRadioButton
     */
    public JRadioButton getRbtnGroupRefuse() {
        if (rbtnGroupRefuse == null) {
            rbtnGroupRefuse = new JRadioButton();
            rbtnGroupRefuse.setFont(new Font("华文楷体", Font.PLAIN, 14));
            rbtnGroupRefuse.setBounds(new Rectangle(168, 254, 85, 20));
            rbtnGroupRefuse.setText("群发屏蔽");
            rbtnGroupRefuse
                    .addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            txtaSendContent.setFocusable(true);
                            txtaSendContent.requestFocus();
                        }
                    });
        }
        return rbtnGroupRefuse;
    }

    /**
     * This method initializes btnSetNet
     *
     * @return javax.swing.JButton
     */
    public JButton getBtnSetNet() {
        if (btnSetNet == null) {
            btnSetNet = new JButton();
            btnSetNet.setFont(new Font("华文楷体", Font.PLAIN, 12));
            btnSetNet.setBounds(new Rectangle(430, 5, 60, 20));
            btnSetNet.setText("设置");
            btnSetNet.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    NetSetFrame netSetFrame = new NetSetFrame(DataPanel.this);
                }
            });
        }
        return btnSetNet;
    }

    /**
     * WindowBuilder generated method.<br>
     * Please don't remove this method or its invocations.<br>
     * It used by WindowBuilder to associate the {@link javax.swing.JPopupMenu}
     * with parent.
     */
    private static void addPopup(Component component, final JPopupMenu popup) {
        component.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger())
                    showMenu(e);
            }

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger())
                    showMenu(e);
            }

            private void showMenu(MouseEvent e) {
                popup.show(e.getComponent(), e.getX(), e.getY());
                e.consume();
            }
        });
    }
}
