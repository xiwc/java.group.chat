package cho.xwc.chat.client;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;

public class ChatRecordMgr {

    private String chatMsg;

    private String savePath;
    private String saveFileName;
    private String saveMemoFileName;

    private String lineSeparator;

    private static final long FILE_LEN = 1024 * 50;

    public ChatRecordMgr(String chatMsg) {
        this.chatMsg = chatMsg;

        this.savePath = Utils.getUserDir() + "GroupChat_XWC\\ChatRecord";
        this.saveFileName = "ChatRecord.txt";
        this.saveMemoFileName = "Memo.txt";
        this.lineSeparator = System.getProperty("line.separator");
    }

    public void save(){
        FileOutputStream fos = null;
        DataOutputStream dos = null;
        boolean isCreateNewFile = false;
        String newFileName = "";
        File dir = new File(savePath);
        if (!dir.exists())
            dir.mkdirs();
        File file = new File(savePath + File.separator + saveFileName);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }else{
            if(file.length() > FILE_LEN){
                Calendar now = Calendar.getInstance();
                StringBuffer sb = new StringBuffer();
                sb.append("ChatRecord_")
                .append(now.get(Calendar.YEAR)).append("-")
                .append(now.get(Calendar.MONDAY) + 1).append("-")
                .append(now.get(Calendar.DATE)).append("_")
                .append(now.get(Calendar.HOUR)).append("-")
                .append(now.get(Calendar.MINUTE)).append("-")
                .append(now.get(Calendar.SECOND)).append(".txt");
                newFileName = sb.toString();

                file.renameTo(new File(savePath + File.separator + newFileName));
                file = new File(savePath + File.separator + saveFileName);
                isCreateNewFile = true;
                try {
                    file.createNewFile();
                    fos = new FileOutputStream(file, true);
                    dos = new DataOutputStream(fos);
                    writeUTF8(lineSeparator, dos);
                    writeUTF8(lineSeparator, dos);
                    writeUTF8("<< " + savePath + " >>", dos);
                    writeUTF8(lineSeparator, dos);
                    writeUTF8(lineSeparator, dos);
                    dos.flush();
                    fos.flush();
                    dos.close();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            fos = new FileOutputStream(file, true);
            dos = new DataOutputStream(fos);
            if(isCreateNewFile){
                writeUTF8(lineSeparator, dos);//【" + getRequestCmd(socket) + "】
                String wmsg = "由于原来文件过大,所以创建了新的聊天记录文件,若要查看原来的聊天记录,";
                writeUTF8(wmsg, dos);
                wmsg = "请直接到目录 【" + savePath + "】 查看文件 【" + newFileName + "】";
                writeUTF8(lineSeparator, dos);
                writeUTF8(wmsg, dos);
            }
            writeUTF8(lineSeparator, dos);
            writeUTF8(lineSeparator, dos);
            writeUTF8(">>>>>>>>>>>>>> " +  Utils.formatDate(new Date()) + " <<<<<<<<<<<<<<", dos);
            writeUTF8(lineSeparator, dos);
            writeUTF8(lineSeparator, dos);
            String[] msgs = chatMsg.split("\n");
            for (String msg : msgs) {
                writeUTF8(msg, dos);
                writeUTF8(lineSeparator, dos);
            }
            dos.flush();
            fos.flush();

            dos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeUTF8(String str, DataOutputStream dos) throws IOException{
        try {
            byte[] bs = str.getBytes("UTF-8");
            dos.write(bs);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    public void saveChatRecord() {
        FileWriter fw = null;
        BufferedWriter bw = null;
        File dir = new File(savePath);
        if (!dir.exists())
            dir.mkdirs();
        File file = new File(savePath + File.separator + saveFileName);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        try {
            fw = new FileWriter(file, true);
            fw.getEncoding();
            bw = new BufferedWriter(fw);
            bw.newLine();
            bw.newLine();
            bw.append(">>>>>>>>>>>>>> " +  Utils.formatDate(new Date()) + " <<<<<<<<<<<<<<");
            bw.newLine();
            bw.newLine();
            String[] msgs = chatMsg.split("\n");
            for (String msg : msgs) {
                bw.append(msg);
                bw.newLine();
            }
            bw.flush();
            fw.flush();

            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void openChatRecord() {
        String filePath = savePath + File.separator + saveFileName;
        File file = new File(filePath);
//        if (file.exists()) {
//            try {
//                Runtime.getRuntime().exec("cmd /c start notepad " + filePath);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.open(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveMemo() {
        FileOutputStream fos = null;
        DataOutputStream dos = null;
        boolean isCreateNewFile = false;
        String newFileName = "";
        File dir = new File(savePath);
        if (!dir.exists())
            dir.mkdirs();
        File file = new File(savePath + File.separator + saveMemoFileName);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }else{
            if(file.length() > FILE_LEN){
                Calendar now = Calendar.getInstance();
                StringBuffer sb = new StringBuffer();
                sb.append("Memo_")
                .append(now.get(Calendar.YEAR)).append("-")
                .append(now.get(Calendar.MONDAY) + 1).append("-")
                .append(now.get(Calendar.DATE)).append("_")
                .append(now.get(Calendar.HOUR)).append("-")
                .append(now.get(Calendar.MINUTE)).append("-")
                .append(now.get(Calendar.SECOND)).append(".txt");
                newFileName = sb.toString();

                file.renameTo(new File(savePath + File.separator + newFileName));
                file = new File(savePath + File.separator + saveMemoFileName);
                isCreateNewFile = true;
                try {
                    file.createNewFile();
                    fos = new FileOutputStream(file, true);
                    dos = new DataOutputStream(fos);
                    writeUTF8(lineSeparator, dos);
                    writeUTF8(lineSeparator, dos);
                    writeUTF8("<< " + savePath + " >>", dos);
                    writeUTF8(lineSeparator, dos);
                    writeUTF8(lineSeparator, dos);
                    dos.flush();
                    fos.flush();
                    dos.close();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            fos = new FileOutputStream(file, true);
            dos = new DataOutputStream(fos);
            if(isCreateNewFile){
                writeUTF8(lineSeparator, dos);//【" + getRequestCmd(socket) + "】
                String wmsg = "由于原来文件过大,所以创建了新的聊天记录文件,若要查看原来的聊天记录,";
                writeUTF8(wmsg, dos);
                wmsg = "请直接到目录 【" + savePath + "】 查看文件 【" + newFileName + "】";
                writeUTF8(lineSeparator, dos);
                writeUTF8(wmsg, dos);
            }
            writeUTF8(lineSeparator, dos);
            writeUTF8(lineSeparator, dos);
            writeUTF8(">>>>>>>>>>>>>> " +  Utils.formatDate(new Date()) + " <<<<<<<<<<<<<<", dos);
            writeUTF8(lineSeparator, dos);
            writeUTF8(lineSeparator, dos);
            String[] msgs = chatMsg.split("\n");
            for (String msg : msgs) {
                writeUTF8(msg, dos);
                writeUTF8(lineSeparator, dos);
            }
            dos.flush();
            fos.flush();

            dos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openMemo() {
        String filePath = savePath + File.separator + saveMemoFileName;
        File file = new File(filePath);
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.open(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openRecordDir() {
        File file = new File(savePath);
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.open(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
