package cho.xwc.chat.client;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import javax.swing.JFrame;

public class WarningFrame extends JFrame {
    
    private String msg = null;
    
    private DataPanelMsg dataPanelMsg = null;

    private WarningFrame(String msg) {
        this.msg = msg;
        initialize();
        setMsg();
        
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        int width = toolkit.getScreenSize().width;
        int height = toolkit.getScreenSize().height;
        int lx = (width - 371)/2;
        int ly = (height - 250)/2;
        
        this.setBounds(new Rectangle(lx,ly, 371, 255));
        this.setResizable(false);
        this.setMaximizedBounds(new Rectangle(lx,ly, 371, 250));
        this.setAlwaysOnTop(true);
        this.setVisible(true);
    }
    
    private void setMsg() {
        try {
            String[] msgs = msg.split("\n");
            int index = msgs[0].lastIndexOf(")");
            String name = msgs[0].substring(0, index + 1);
            int indexL = msgs[0].lastIndexOf("【");
            int indexR = msgs[0].lastIndexOf("】");
            String msgType = msgs[0].substring(indexL + 1, indexR);
            String time = msgs[0].substring(indexR + 4).trim();
            String title = "来自【" + name + "】的重要消息-----" + time;
            this.setTitle("【" + msgType + "】");
            StringBuffer sb = new StringBuffer();
            for (int i = 1; i < msgs.length; i++) {
                sb.append(msgs[i]).append("\n");
            }
            dataPanelMsg.getTxtaMsg().setText(sb.toString());
            dataPanelMsg.setTitleBorder(title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initialize(){
        this.setLayout(new BorderLayout());
        this.getContentPane().add(getDataPanelMsg(), BorderLayout.CENTER);
    }

    public DataPanelMsg getDataPanelMsg() {
        if(dataPanelMsg == null){
            dataPanelMsg = new DataPanelMsg(this);
            dataPanelMsg.setBounds(new Rectangle(0, 0, 366, 233));
        }
        return dataPanelMsg;
    }
    
    public static void showMsg(String msg){
        new WarningFrame(msg);
    }
}
