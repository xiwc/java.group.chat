package cho.xwc.chat.client;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 常用辅助工具类
 * @author weicheng.xi
 *
 */
public class Utils {

    private Utils() {
    }

    /**
     * 获取当前工程的根目录
     * @return
     */
    public static String getUserDir() {
        String userDir = System.getProperty("user.dir");
        if (!userDir.endsWith(File.separator)) {
            userDir = userDir + File.separator;
        }
        return userDir;
    }

    /**
     * yyyy年MM月dd日 HH:mm:ss a EEE格式的日期格式化对象
     */
    public static SimpleDateFormat format = new SimpleDateFormat(
            "yyyy年MM月dd日 HH:mm:ss a EEE");

    /**
     * 以yyyy年MM月dd日 HH:mm:ss a EEE格式格式化日期
     * @param date
     * @return
     */
    public static String formatDate(Date date) {
        return format.format(date);
    }

    /**
     * 删除字符串开始的空白字符
     * @param txt
     * @return
     */
    public static String trimStart(String txt) {
        return txt.substring(txt.indexOf(txt.trim()));
    }

    /**
     * 删除字符串末尾的空白字符
     * @param txt
     * @return
     */
    public static String trimEnd(String txt) {
        String s = txt.trim();
        int index = txt.indexOf(s);
        return txt.substring(0, index + s.length());
    }

    /**
     * 删除所有行前的所有空白字符
     * @param text
     * @return
     */
    public static String trimLinesStart(String text) {

        StringBuffer sb = new StringBuffer();
        String[] lines = text.split("\r\n");
        for (String line : lines) {
            sb.append(trimStart(line)).append("\r\n");
        }
        return sb.toString();
    }

    /**
     * 删除所有行前的空白字符根据第一行的空白字符数目删除之. 即删除的空白字符数不多于第一行的空白字符数
     * @param txt
     * @return
     */
    public static String trimLinesStartByFirstLine(String txt) {

        StringBuffer sb = new StringBuffer();
        String[] lines = txt.split("\r\n");
        if (lines != null && lines.length > 0) {
            int trimLen = lines[0].length() - trimStart(lines[0]).length();
            for (String line : lines) {
                String newLine = line;
                int blankLen = line.length() - trimStart(line).length();
                if (blankLen >= trimLen) {
                    newLine = line.substring(trimLen);
                } else {
                    newLine = trimStart(line);
                }
                sb.append(newLine).append("\r\n");
            }
        }
        return sb.toString();
    }
    //截取用户信息中的ip地址
    public static String getIp(String user){
    	int indexAt = user.lastIndexOf("@");
    	int indexColon = user.lastIndexOf(":");
    	String ip = user.substring(indexAt + 1, indexColon);
    	return ip;
    }
    //截取用户信息中的Port
    public static String getPort(String user){
    	int indexColon = user.lastIndexOf(":");
    	String port = user.substring(indexColon + 1);
    	return port;
    }
    //截取用户信息中的用户名
    public static String getUserName(String user){
    	int indexAt = user.lastIndexOf("@");
    	String userName = user.substring(0, indexAt);
    	return userName;
    }
}
