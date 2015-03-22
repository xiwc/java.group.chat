package cho.xwc.chat.client;

import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import javax.swing.JComboBox;

public class UserClientMgr {

    private DataPanel dataPanel = null;

    public UserClientMgr(DataPanel dataPanel) {
        this.dataPanel = dataPanel;
    }

    public synchronized void  onLine(String userMsg, TrayIcon trayIcon) {
        JComboBox userList = dataPanel.getUserList();
        if (!isContains(userMsg)) {
            userList.addItem(userMsg);
            dataPanel.setOnlineCount();
            if (trayIcon != null && MainFrame.WarningFlag){
                trayIcon.displayMessage("上线提示", userMsg, MessageType.INFO);
            }
        }
    }

    public synchronized void outLine(String userMsg, TrayIcon trayIcon) {
        JComboBox userList = dataPanel.getUserList();
        if (isContains(userMsg)) {
            userList.removeItem(userMsg);
            dataPanel.setOnlineCount();
            if(MainFrame.WarningFlag){
                trayIcon.displayMessage("下线提示", userMsg, MessageType.INFO);
            }
        }
    }

    private boolean isContains(String msg) {
        JComboBox userList = dataPanel.getUserList();
        for (int i = 0; i < userList.getItemCount(); i++) {
            if (msg.equals(userList.getItemAt(i))) {
                return true;
            }
        }
        return false;
    }
}
