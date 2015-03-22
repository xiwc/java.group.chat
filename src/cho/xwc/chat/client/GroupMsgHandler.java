package cho.xwc.chat.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;

public class GroupMsgHandler {
    
    private InetAddress groupAddress;
    private MulticastSocket multicastSocket;
    private SocketAddress socketAddress;
    public GroupMsgHandler(String ip, int port) throws IOException {
        this.groupAddress = InetAddress.getByName(ip);
        this.multicastSocket = new MulticastSocket(port);
        this.multicastSocket.joinGroup(groupAddress);
        this.socketAddress = new InetSocketAddress(groupAddress, port);
    }
    
    public boolean sendMsg(String msg){
        byte[] bs = null;
        try {
            bs = msg.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        DatagramPacket hi = new DatagramPacket(bs, bs.length);
        hi.setSocketAddress(socketAddress);
        try {
            this.multicastSocket.send(hi);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public String receiveMsg(){
        String recMsg = "";
        byte[] buf = new byte[102400];
        DatagramPacket recv = new DatagramPacket(buf, buf.length);
        try {
            multicastSocket.receive(recv);
            recMsg = new String(recv.getData(), recv.getOffset(), recv
                    .getLength(), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return recMsg;
    }
    
    public void leaveGroup(){
        try {
            multicastSocket.leaveGroup(groupAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
