package cho.xwc.chat.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.TextArea;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class DataPanelMsg extends JPanel {
    
    private Border border = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLUE);  //  @jve:decl-index=0:

    private JFrame frame = null;
    private TextArea txtaMsg = null;
    public DataPanelMsg(JFrame frame) {

        this.frame = frame;
        initialize();
        this.setBorder(BorderFactory.createTitledBorder(border, "来自[XXX]的重要消息"));
    }
    private void initialize() {
        this.setLayout(new BorderLayout());
        this.add(getTxtaMsg(), BorderLayout.CENTER);
    }
    public TextArea getTxtaMsg() {
        if(txtaMsg == null){
            txtaMsg = new TextArea();
            txtaMsg.setFont(new Font("华文楷体", Font.PLAIN, 14));
            txtaMsg.setFont(new Font(getFont().getFontName(), getFont().getStyle(), getFont().getSize() + 3));
            txtaMsg.setEditable(false);
            txtaMsg.setBackground(Color.WHITE);
            txtaMsg.addMouseWheelListener(new MouseWheelListener() {
                
                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    if(e.isControlDown()){
                        e.consume();
                        Font dFont = txtaMsg.getFont();
                        int size = dFont.getSize();
                        size -= e.getUnitsToScroll();
                        Font font = new Font(dFont.getFontName(), dFont.getStyle(), size);
                        txtaMsg.setFont(font);
                    }
                }
            });
        }
        return txtaMsg;
    }
    public void setTitleBorder(String msg) {
        this.setBorder(BorderFactory.createTitledBorder(border, msg));
    }
} 