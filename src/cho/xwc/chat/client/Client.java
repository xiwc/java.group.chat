package cho.xwc.chat.client;

import java.awt.TextArea;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    
     private UserClient userClient;
     
     private String ip;
     private int port;
     private String serverMsg;
     private Socket socket;
     private DataPanel dataPanel = null;
     
     public Client(String serverMsg, DataPanel dataPanel) {
        this.serverMsg = serverMsg;
        this.dataPanel = dataPanel;
        int indexAt = serverMsg.indexOf("@");
        int indexMh = serverMsg.lastIndexOf(":");
        this.ip = serverMsg.substring(indexAt + 1, indexMh);
        this.port = Integer.parseInt(serverMsg.substring(indexMh + 1));
    }


    public boolean connect(){
         try {
            socket = new Socket(ip, port);
            userClient = new UserClient(socket);
            userClient.sendMsg("ChatRequest");
            new ReceiveMsg(userClient).start();
            return true;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private class ReceiveMsg extends Thread{
        private UserClient userClient;
      
        public ReceiveMsg(UserClient userClient) {
            this.userClient = userClient;
        }

        @Override
        public void run() {
            while(true){
                try {
                    String msg = userClient.receiveMsg();
                    TextArea textArea = dataPanel.getTxtaChatContent();
                    //textArea.append(msg);
                    //textArea.append("\n");
                    dataPanel.appendChatMsg(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
        
    }

    public void stop() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void sendMsg(String sendMsg) {
        userClient.sendMsg(sendMsg);
    }

}
