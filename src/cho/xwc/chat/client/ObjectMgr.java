package cho.xwc.chat.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectMgr {
    
    private String savePath;
    private String saveFileName;
    
    public String getSaveFileName() {
        return saveFileName;
    }

    public void setSaveFileName(String saveFileName) {
        this.saveFileName = saveFileName;
    }

    public ObjectMgr() {
        this.savePath = Utils.getUserDir() + "GroupChat_XWC/UserInfo";
//        this.savePath = "D:/GroupChat_XWC/UserInfo";
        this.saveFileName = "UserInfo.dat";
    }

    public void save(Object obj){
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        File dir = new File(savePath);
        if (!dir.exists())
            dir.mkdirs();
        File file = new File(savePath + File.separator + saveFileName);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            fos = new FileOutputStream(file);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
            oos.flush();
            fos.flush();
            oos.close();
            fos.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public Object read(){
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        File dir = new File(savePath);
        if (!dir.exists())
            dir.mkdirs();
        File file = new File(savePath + File.separator + saveFileName);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            fis = new FileInputStream(file);
            ois = new ObjectInputStream(fis);
            Object obj = ois.readObject();
            ois.close();
            fis.close();
            return obj;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
//    public static void main(String[] args) {
//        UserInfo info = new UserInfo();
//        info.setIp("221.22.11.44");
//        info.setPort("12131");
//        info.setUserName("myself");
//        
//        ObjectMgr objectMgr = new ObjectMgr();
//        objectMgr.save(info);
//        
//        UserInfo userInfo = (UserInfo) objectMgr.read();
//        System.out.println(userInfo.getIp());
//        System.out.println(userInfo.getPort());
//        System.out.println(userInfo.getUserName());
//    }
}
