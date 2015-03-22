package cho.xwc.chat.client;

public class PropertiesMgr {
    
    private Properties properties;
    
    private ObjectMgr objectMgr = new ObjectMgr();
    
    public PropertiesMgr() {
        objectMgr.setSaveFileName("UserInfo.dat");
        properties = (Properties) objectMgr.read();
    }

    public PropertiesMgr(Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
    
    public void save() {
        objectMgr.setSaveFileName("UserInfo.dat");
        objectMgr.save(properties);
    }
    
    public String getValueByKey(String key){
        if(properties != null){
            if ("ip".equals(key)) {
                return properties.getIp();
            } else if ("port".equals(key)) {
                return properties.getPort() + "";
            } else if ("username".equals(key)) {
                return properties.getUserName();
            } else {
                return null;
            }
        }else{
            return null;
        }
    }
    
    public void saveObject(Object obj){ 
        objectMgr.setSaveFileName("ListenMulIpAndPort.dat");
        objectMgr.save(obj);
    }
    
    public Object readObject()
    {
        objectMgr.setSaveFileName("ListenMulIpAndPort.dat");
        return objectMgr.read();
    }
}
