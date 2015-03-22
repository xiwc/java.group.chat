package cho.xwc.chat.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;

public class FolderTransmit {
    
    private String folderPath;
    private DataPanel dataPanel;
    private String ip;
    private int port;
    private UserClient sendClient;
    private String folderName;
    private UserClient recvClient;
    private long totalLen = 0L;
    
    public FolderTransmit(Socket socket, DataPanel dataPanel){
        this.recvClient = new UserClient(socket);
        this.dataPanel = dataPanel;
    }
    
    public FolderTransmit(String folderPath, String serverMsg, DataPanel dataPanel) {
        this.folderPath = folderPath;
        this.dataPanel = dataPanel;
        int indexAt = serverMsg.indexOf("@");
        int indexMh = serverMsg.lastIndexOf(":");
        this.ip = serverMsg.substring(indexAt + 1, indexMh);
        this.port = Integer.parseInt(serverMsg.substring(indexMh + 1));
        this.folderName = new File(folderPath).getName();
    }
    
    private void getFolderTotalLen(String path) {
        this.totalLen = 0L;
        File folder = new File(path);
        getFileLen(folder);
    }
    private void getFileLen(File folder) {
        File[] files = folder.listFiles();
        for (File file : files) {
            if(file.isFile()){
                this.totalLen += file.length();
            }else if(file.isDirectory()){
                getFileLen(file);
            }
        }
    }

    public void stop() {
        try {
            isStopSend = true;
            sendClient.getSocket().shutdownOutput();
            sendClient.getSocket().shutdownInput();
            sendClient.getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String getSpeed(long time, long totalLen){
        String speed = (totalLen * 1000D) / (1024D * 1024D * time) + "";
        int indexP = speed.indexOf(".");
        if(indexP != -1){
            speed = speed.substring(0, indexP + 3);
        }
        return (speed + " MB/S");
    }
    private String getUseTime(long time){
        String useTime = "";
        if (time / 1000D / 60D >= 1) {
            useTime = time / 1000 / 60 + " 分钟";
        }else{
            if(time / 1000 == 0){
                useTime = "1 秒钟";
            }else{
                useTime = time / 1000 + " 秒钟";
            }
        }
        return useTime;
    }
    private static final int BUF_LEN = 102400;
    private boolean isStopSend = false;
    public void send() {
        Runnable conn = new Runnable() {
            
            @Override
            public void run() {
                sendRunnable();
            }
            
            private void sendRunnable(){
                if(connect()){
                    Runnable sendRu = new Runnable() {
                        private String rootPath = null;
                        private long haveSendLen = 0L;
                        @Override
                        public void run() {
                            long beginTime = System.currentTimeMillis();
                            
                            File folder = new File(folderPath);
                            
                            if(folder.isFile()){
                                totalLen = folder.length();
                                try {
                                    sendClient.sendLong(totalLen);
                                    sendClient.sendMsg("OnlyFile");
                                    sendClient.sendMsg(folder.getName());
                                    sendFile(folder);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }else{
                                rootPath = folder.getAbsolutePath();
                                getFolderTotalLen(folderPath);
                                try {
                                    sendClient.sendLong(totalLen);
                                    sendFolder(folder);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    return;
                                }
                            }
                           
                            sendClient.sendMsg("EndFolderT");
                            DataPanel.isSending = false;
                            
                            long endTime = System.currentTimeMillis();
                            
                            dataPanel.getLblInfo().setVisible(false);
                            dataPanel.getBtnFolder().setEnabled(true);
                            if(!isStopSend){
                                String speed = getSpeed(endTime - beginTime,
                                        totalLen);
                                String useTime = getUseTime(endTime - beginTime);
                                dataPanel.appendChatMsg("文件【" + folderName + "】发送完毕, 传送用时: "
                                                + useTime + ",速度: " + speed
                                                + " !\n");
                                JButton btn = dataPanel.getBtnRecv();
                                btn.doClick();
                            }else{
                                isStopSend = false;
                                JButton btnRecv = dataPanel.getBtnRecv();
                                btnRecv.setText("接收");
                                btnRecv.setVisible(false);
                                dataPanel.getLblInfo().setVisible(false);
                                dataPanel.getBtnFolder().setEnabled(true);
                                dataPanel.appendChatMsg("文件【" + folderName + "】发送中断!\n");
                            }
                        }

                        private void sendFolder(File folder) {
                            sendClient.sendMsg("BeginFolderT");
                            String path = folder.getAbsolutePath();
                            int index = rootPath.length() - folderName.length();
                            String fPath = path.substring(index);
                            sendClient.sendMsg(fPath);
                            File[] files = folder.listFiles();
                            List<File> listFile = new ArrayList<File>();
                            List<File> listFolder = new ArrayList<File>();
                            for (File file : files) {
                                if(file.isFile()){
                                    listFile.add(file);
                                }else if(file.isDirectory()){
                                    listFolder.add(file);
                                }
                            }
                            for (File file : listFile) {
                                sendFile(file);
                            }
                            for (File file : listFolder) {
                                sendFolder(file);
                            }
                        }

                        private boolean sendFile(File file) {
                            sendClient.sendMsg("BeginFileT");
                            sendClient.sendMsg(file.getName());
                            FileInputStream fis = null;
                            try {
                                fis = new FileInputStream(file);
                                byte[] buf = new byte[BUF_LEN];
                                int len = fis.read(buf);
                                while (len != -1) {
                                    haveSendLen += len;
                                    setTransferRate(haveSendLen, totalLen);
                                    
                                    sendClient.sendByte(buf, len);
                                    len = fis.read(buf);
                                }
                                sendClient.getOut().writeInt(len);
                                fis.close();
                                return true;
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                                isStopSend = true;
                                if(fis != null){
                                    try {
                                        fis.close();
                                        file = null;
                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                            return false;
                        }
                        
                        private void setTransferRate(long haveRecvLen, long folderLen) {
                            long rate = ((haveRecvLen * 100) / folderLen);
                            dataPanel.getLblInfo().setText("完成:  " + rate +"%");
                        }
                    };
                    DataPanel.isSending = true;
                    new Thread(sendRu).start();
                }
            }
        };
        new Thread(conn).start();
    }
    
    private boolean connect() {
        try {
            Socket socket = new Socket(ip, port);
            sendClient = new UserClient(socket);
            sendClient.sendMsg("FolderTransmit");
            sendClient.sendMsg(dataPanel.getServerMsg());
            sendClient.sendMsg(folderName);
            String retCmd = sendClient.receiveMsg();
            if("Agree".equals(retCmd)){
                Runnable recvCMD = new Runnable() {
                    
                    @Override
                    public void run() {
                        while(true){
                            try {
                                String cmd = sendClient.receiveMsg();
                                if("Break".equals(cmd)){
                                    isStopSend = true;
                                    break;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                break;
                            }
                        }
                    }
                };
                new Thread(recvCMD).start();
                return true;
            }else{
                dataPanel.getTxtaChatContent().append(retCmd);
                return false;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
   
    private boolean isFirstTime = true;
    private boolean isStopRecv = false;
    public void receive(final String path) {
        recvClient.sendMsg("Agree");
        DataPanel.isRecving = true;
        Runnable recv = new Runnable() {
            private String folderPath = "";
            @Override
            public void run() {
                long beginTime = System.currentTimeMillis();
                
                long haveRecvLen = 0;// 已经接收的文件长度
                // get folder total length
                long folderLen = 0;
                try {
                    folderLen = recvClient.getIn().readLong();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                
                while(true){
                    FileOutputStream fos = null;
                    try {
                        String recvCmd = recvClient.receiveMsg();
                        if("BeginFolderT".equals(recvCmd)){
                            String subFolder = recvClient.receiveMsg();
                            if(isFirstTime){
                                folderName = subFolder;
                                isFirstTime = false;
                            }
                            if(path.endsWith(File.separator)){
                                folderPath = path + subFolder;
                            }else{
                                folderPath = path + File.separator + subFolder;
                            }
                            File file = new File(folderPath);
                            file.mkdirs();
                        }else if("BeginFileT".equals(recvCmd)){
                            String fileName = recvClient.receiveMsg();
                            String filePath = folderPath + File.separator + fileName;
                            fos = new FileOutputStream(filePath);
                            
                            byte[] bs = new byte[BUF_LEN];
                            int count = recvClient.getIn().readInt();
                            while(count != -1){
                                recvClient.getIn().readFully(bs, 0, count);
                                fos.write(bs, 0, count);
                                fos.flush();
                                haveRecvLen += count;
                                setTransferRate(haveRecvLen, folderLen);
                                count = recvClient.getIn().readInt();
                            }
                            fos.close();
                        }else if("EndFolderT".equals(recvCmd)){
                            long endTime = System.currentTimeMillis();
                            
                            String speed = getSpeed(endTime - beginTime, folderLen);
                            String useTime = getUseTime(endTime - beginTime);
                            dataPanel.appendChatMsg("文件【" + folderName + "】接收完毕, 传送用时: "
                                            + useTime + ",速度: " + speed
                                            + " !\n");
                            JButton btn = dataPanel.getBtnRecv();
                            btn.setText("接收");
                            btn.setVisible(false);
                            dataPanel.getLblInfo().setVisible(false);
                            break;
                        }else if("OnlyFile".equals(recvCmd)){
                            if(path.endsWith(File.separator)){
                                folderPath = path.substring(0, path.length()-1);
                            }else{
                                folderPath = path;
                            }
                            new File(folderPath).mkdirs();
                            folderName = recvClient.receiveMsg();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        if(!isStopRecv){
                            dataPanel.appendChatMsg("对方终止了文件【" + folderName + "】的传送!\n");
                        }else{
                            isStopRecv = false;
                        }
                        JButton btn = dataPanel.getBtnRecv();
                        btn.setText("接收");
                        btn.setVisible(false);
                        dataPanel.getLblInfo().setVisible(false);
                        if(fos != null){
                            try {
                                fos.close();
                                fos = null;
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                        break;
                    }
                }
                DataPanel.isRecving = false;
            }
            private void setTransferRate(long haveRecvLen, long folderLen) {
                double val = ((double)haveRecvLen * 100) / ((double)folderLen);
                int rate = (int)val;
                dataPanel.getLblInfo().setText("完成:  " + rate +"%");
            }
        };
        new Thread(recv).start();
    }

    public void breakRecvFolder() {
        try {
            recvClient.sendMsg("Break");
            isStopRecv = true;
            recvClient.getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

}
