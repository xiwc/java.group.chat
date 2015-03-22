package cho.xwc.chat.client;

import java.io.Serializable;

public class Properties implements Serializable {
    
    private String ip;
    
    private int port;
    
    private String userName;

    public Properties() {
    }

    public Properties(String ip, int port, String userName) {
        this.ip = ip;
        this.port = port;
        this.userName = userName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}
