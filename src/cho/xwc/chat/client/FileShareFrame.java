package cho.xwc.chat.client;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.List;

import javax.swing.JFrame;
import cho.xwc.chat.client.DataPanelFileShare;

public class FileShareFrame extends JFrame {

	/**
	 * Launch the application
	 * @param args
	 */
	public static void main(String args[]) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FileShareFrame frame = new FileShareFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private final DataPanelFileShare dataPanelFileShare;
	/**
	 * Create the frame
	 */
	public FileShareFrame() {
		super();
		setTitle("文件共享管理器");
		setBounds(100, 100, 649, 402);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		dataPanelFileShare = new DataPanelFileShare();
		getContentPane().add(dataPanelFileShare, BorderLayout.CENTER);
		//
	}

	public void setOnLineUsers(List<String> onLineUsers) {
		dataPanelFileShare.initUserListControl(onLineUsers);
	}

}
