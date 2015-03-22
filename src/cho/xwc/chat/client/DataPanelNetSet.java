package cho.xwc.chat.client;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class DataPanelNetSet extends JPanel {

    private DataPanel dataPanel = null;
    
    /** JList */
    private JList listPart = null;

    /** JList */
    private JList listAll = null;  
    
    /** 刷新 */
    private JButton btnRefresh = null;

    /** JButton */
    private JButton btnAdd = null;

    /** JButton */
    private JButton btnRemove = null;
    
    /** JScrollPane */
    private JScrollPane scrollPanelAll = null;

    /** JScrollPane */
    private JScrollPane scrollPanelPart = null;

    private Border border = BorderFactory.createLineBorder(Color.BLACK, 1);

    public DataPanelNetSet(DataPanel dataPanel){
        this.dataPanel = dataPanel;
        initialize();
        initListAllAndPart();
    }
       
    private void initListAllAndPart() {
        listPart.setListData(dataPanel.getListListenMulIpAndPort().toArray());

        dataPanel.getListMulIpAndPort().clear();
        dataPanel.getCommonMsgHandler().sendMsg("request@" + dataPanel.getServerMsg());
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        listAll.setListData(dataPanel.getListMulIpAndPort().toArray());
    }
    
    private void initialize() {
        this.setLayout(null);
        
        this.setSize(new Dimension(420, 233));
        this.add(getListPart(), null);
        this.add(getBtnRemove(), null);
        this.add(getScrollPanelPart(), null);
        this.add(getScrollPanelAll(), null);
        this.add(getBtnAdd(), null);
        this.add(getBtnRefresh(), null);
    }
    
    /**
     * JListJListを取得する
     *
     * @return JListJList
     */
    public JList getListPart() {

        if (listPart == null) {
            listPart = new JList();
        }
        return listPart;
    }

    /**
     * JListJListを取得する
     *
     * @return JListJList
     */
    public JList getListAll() {

        if (listAll == null) {
            listAll = new JList();
        }
        return listAll;
    }
    
    /**
     * JScrollPaneJScrollPaneを取得する
     *
     * @return JScrollPaneJScrollPane
     */
    public JScrollPane getScrollPanelAll() {

        if (scrollPanelAll == null) {
            scrollPanelAll = new JScrollPane(getListAll());
            scrollPanelAll.setBounds(new Rectangle(15, 15, 150, 200));
            scrollPanelAll.setBorder(BorderFactory.createTitledBorder(border, "网络上所有的组播段"));
        }
        return scrollPanelAll;
    }

    /**
     * JScrollPaneJScrollPaneを取得する
     *
     * @return JScrollPaneJScrollPane
     */
    public JScrollPane getScrollPanelPart() {

        if (scrollPanelPart == null) {
            scrollPanelPart = new JScrollPane(getListPart());
            scrollPanelPart.setBounds(new Rectangle(255, 15, 150, 200));
            scrollPanelPart.setBorder(BorderFactory.createTitledBorder(border, "要加入监听的组播段"));
        }
        return scrollPanelPart;
    }
    
    /**
     * 刷新JButtonを取得する
     *
     * @return 刷新JButton
     */
    public JButton getBtnRefresh() {

        if (btnRefresh == null) {
            btnRefresh = new JButton();
            btnRefresh.setFont(new Font("华文楷体", Font.PLAIN, 12));
            btnRefresh.setText("刷新");
            btnRefresh.setBounds(new Rectangle(180, 39, 60, 25));
            btnRefresh.addActionListener(new ActionListener() {
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    dataPanel.getListMulIpAndPort().clear();
                    dataPanel.getCommonMsgHandler().sendMsg("request@" + dataPanel.getServerMsg());
                    
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ee) {
                        ee.printStackTrace();
                    }
                    listAll.setListData(dataPanel.getListMulIpAndPort().toArray());
                }
            });
        }
        return btnRefresh;
    }

    /**
     * JButtonJButtonを取得する
     *
     * @return JButtonJButton
     */
    public JButton getBtnAdd() {

        if (btnAdd == null) {
            btnAdd = new JButton();
            btnAdd.setText(">>");
            btnAdd.setBounds(new Rectangle(180, 87, 60, 25));
            btnAdd.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    Object[] objs = listAll.getSelectedValues();
                    addItemsToListPart(objs);
                }

                private void addItemsToListPart(Object[] objs) {
                    List<String> list = dataPanel.getListListenMulIpAndPort();
                    for (Object object : objs) {
                        if(!list.contains(object)){
                            list.add(object.toString());
                        }
                    }
                    listPart.setListData(list.toArray());
                }
            });
        }
        return btnAdd;
    }

    /**
     * JButtonJButtonを取得する
     *
     * @return JButtonJButton
     */
    public JButton getBtnRemove() {

        if (btnRemove == null) {
            btnRemove = new JButton();
            btnRemove.setText("<<");
            btnRemove.setBounds(new Rectangle(180, 135, 60, 25));
            btnRemove.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    Object[] objs = listPart.getSelectedValues();
                    removeItemsFromListPart(objs);
                }

                private void removeItemsFromListPart(Object[] objs) {
                    List<String> list = dataPanel.getListListenMulIpAndPort();
                    for (Object object : objs) {
                        if(list.contains(object)){
                            list.remove(object);
                        }
                    }
                    listPart.setListData(list.toArray());
                }
            });
        }
        return btnRemove;
    }


}  //  @jve:decl-index=0:visual-constraint="9,4"
