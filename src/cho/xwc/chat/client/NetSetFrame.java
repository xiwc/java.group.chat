package cho.xwc.chat.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.JDialog;

public class NetSetFrame extends JDialog {
    
    private DataPanel dataPanel = null;
    
    private DataPanelNetSet dataPanelNetSet = null;
    
    public NetSetFrame(DataPanel dataPanel){
        super(dataPanel.getFrame(), true);
        this.dataPanel = dataPanel;
        initialize();
    }

    private void initialize() {
        this.setLayout(new BorderLayout());
        int width = (dataPanel.getFrame().getWidth() - 425)/2;
        int height = (dataPanel.getFrame().getHeight() - 270) /2;
        Point point = dataPanel.getFrame().getLocation();
        this.setLocation(new Point(point.x + width, point.y + height));
        this.setSize(new Dimension(425, 270));
        this.setResizable(false);
        this.setTitle("网络监听设置");
        this.add(getDataPanelNetSet(), BorderLayout.CENTER);
        
        this.setVisible(true);
    }
    
    public DataPanelNetSet getDataPanelNetSet(){
        if(dataPanelNetSet == null){
            dataPanelNetSet = new DataPanelNetSet(dataPanel);
        }
        return dataPanelNetSet;
    }
    
//    public static void main(String[] args) {
//        new NetSetFrame(null);
//    }
}
