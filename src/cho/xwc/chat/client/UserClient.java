package cho.xwc.chat.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

public class UserClient {
    
    private String userName;
    private String password;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    
    public UserClient(Socket socket) {
        this.socket = socket;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public DataInputStream getIn() {
        return in;
    }

    public void setIn(DataInputStream in) {
        this.in = in;
    }

    public DataOutputStream getOut() {
        return out;
    }

    public void setOut(DataOutputStream out) {
        this.out = out;
    }
    
    public String receiveMsg() throws IOException{
        int count = in.readInt();
        byte[] bs = new byte[count];
        in.readFully(bs);
        String msg = new String(bs, "UTF-8");
        return msg;
    }

    public void sendMsg(String msg){
        try {
            byte[] bs = msg.getBytes("UTF-8");
            out.writeInt(bs.length);
            out.write(bs);
            out.flush();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendByte(byte[] buf, int len) throws IOException {
        out.writeInt(len);
        out.write(buf, 0, len);
        out.flush();
    }

    public void sendLong(long totalLen) throws IOException {
        out.writeLong(totalLen);
        out.flush();
    }
}
