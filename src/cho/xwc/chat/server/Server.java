package cho.xwc.chat.server;

import java.awt.TextArea;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import cho.xwc.chat.client.DataPanel;
import cho.xwc.chat.client.FolderTransmit;
import cho.xwc.chat.client.ShareFileMgr;
import cho.xwc.chat.client.UserClient;
import cho.xwc.chat.client.WarningFrame;

public class Server {
    
    private int port;
    private ServerSocket serverSocket;
    private boolean flag = true;
    private DataPanel dataPanel = null;
    private FolderTransmit folderTransmit = null;

    public Server(DataPanel dataPanel) {
        this.dataPanel =dataPanel;
    }
    
    public String listen(){
        Runnable listener = new Runnable() {
            @Override
            public void run() {
                Random random = new Random();
                while(true){
                    try {
                        port = random.nextInt(65535);
                        serverSocket = new ServerSocket(port);
                        break;
                    } catch (IOException e) {}
                }
                while(flag){
                    try {
                        Socket socket = serverSocket.accept();
                        String cmd = getRequestCmd(socket);
                        if("FolderTransmit".equals(cmd)){//文件传输连接处理
                            if(DataPanel.isRecving){
                                new UserClient(socket).sendMsg("对方接收文件中,请稍候再尝试...\n\n");
                                continue;
                            }
                            if(DataPanel.isSending){
                                new UserClient(socket).sendMsg("对方发送文件中,请稍候再尝试...\n\n");
                                continue;
                            }
                            String warning = "【" + getRequestCmd(socket) + "】 发来文件【" + getRequestCmd(socket) + "】请接收.\n";
                            dataPanel.appendChatMsg(warning);
                            dataPanel.displayMsg("文件接收", warning);
                            dataPanel.getBtnRecv().setVisible(true);
                            folderTransmit = new FolderTransmit(socket, dataPanel);
                        }else if("FileShare".equals(cmd)){//文件共享连接处理
                        	String subCmd = getRequestCmd(socket);
                        	if("ListAllShareFiles".equals(subCmd)){
                        		ShareFileMgr.getInstance().sendAllShareFiles(new UserClient(socket));
                        	}else if("Download".equals(subCmd)){
                        		String filePath = getRequestCmd(socket);
                        		UserClient sendClient = new UserClient(socket);
                        		new SendShareFile(sendClient, filePath).start();
                        	}
                        }else{//用户私聊连接处理
                            UserClient client = new UserClient(socket);
                            new ReceiveMsg(client).start();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }

            private String getRequestCmd(Socket socket) {
                String cmd = "";
                try {
                    cmd = new UserClient(socket).receiveMsg();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return cmd;
            }
        };
        new Thread(listener).start();
        String endpoint = "";
        try {
            endpoint = InetAddress.getLocalHost().getHostAddress() + ":" + port;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return endpoint;
    }

    public int getPort() {
        return port;
    }
    
    private class SendShareFile extends Thread{
        private UserClient client;
        private String filePath;
      
        public SendShareFile(UserClient userClient, String filePath) {
            this.client = userClient;
            this.filePath = filePath;
        }

        @Override
        public void run() {
        	ShareFileMgr.getInstance().sendFile(filePath, client);
        }
        
    }
    private class ReceiveMsg extends Thread{
        private UserClient client;
      
        public ReceiveMsg(UserClient userClient) {
            this.client = userClient;
        }

        @Override
        public void run() {
            while(true){
                try {
                    String msg = client.receiveMsg();
                    if(msg.startsWith("△imXwc▲")){
                        msg = msg.substring(7);
                        WarningFrame.showMsg(msg);
                    }
                    dataPanel.appendChatMsg(msg);
                    dataPanel.displayMsg("私聊消息", msg);
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
						client.getSocket().close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
                    client = null;
                    dataPanel.getUserList().setVisible(true);
                    break;
                }
            }
        }
        
    }

    public void stop() {
        try {
            flag = false;
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveFolder(String path) {
        folderTransmit.receive(path);
    }

    public void breakRecvFolder() {
        folderTransmit.breakRecvFolder();
    }
}
