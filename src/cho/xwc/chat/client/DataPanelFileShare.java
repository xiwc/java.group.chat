package cho.xwc.chat.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class DataPanelFileShare extends JPanel {

	private JTree tree;
	private JTable table;
	private JButton btnDownload;
	private JButton btnAddShareFile;
	private JButton btnRemoveShareFile;
	private JButton btnRefresh;

	/**
	 * Create the panel
	 */
	public DataPanelFileShare() {
		super();
		setLayout(new BorderLayout());

		final JToolBar toolBar = new JToolBar();
		add(toolBar, BorderLayout.NORTH);

		final JSplitPane splitPane = new JSplitPane();
		splitPane.setDividerSize(6);
		splitPane.setFont(new Font("", Font.PLAIN, 16));
		splitPane.setDividerLocation(200);
		add(splitPane);

		final JPanel panel_1 = new JPanel();
		panel_1.setPreferredSize(new Dimension(0, 0));
		splitPane.setLeftComponent(panel_1);
		panel_1.setLayout(new BorderLayout());

		tree = new JTree();
		tree.setFont(new Font("华文楷体", Font.PLAIN, 14));
		tree.setPreferredSize(new Dimension(0, 0));
		panel_1.add(tree);

		final JPanel panel = new JPanel();
		splitPane.setRightComponent(panel);
		panel.setLayout(new BorderLayout());

		final Vector<String> colNames = new Vector<String>();
		colNames.add("文件名");
		colNames.add("文件大小");
		colNames.add("共享时间");
		colNames.add("下载次数");
		colNames.add("文件路径");
		colNames.add("文件说明");
		Vector rowsData = new Vector();

		final JToolBar toolBar_1 = new JToolBar();
		toolBar_1.setFloatable(false);
		panel.add(toolBar_1, BorderLayout.NORTH);

		btnDownload = new JButton();
		btnDownload.setFont(new Font("华文楷体", Font.PLAIN, 14));
		btnDownload.setEnabled(false);
		btnDownload.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				TreePath treePath = tree.getSelectionPath();
				if (treePath != null) {
					int selRow = table.getSelectedRow();
					if (selRow >= 0) {
						String filePath = table.getValueAt(selRow, 4)
								.toString();
						ShareFileMgr.getInstance().download(
								treePath.getPath()[1].toString(), filePath);
					}
				}
			}
		});
		btnDownload.setText("下载共享文件");
		toolBar_1.add(btnDownload);

		btnAddShareFile = new JButton();
		btnAddShareFile.setFont(new Font("华文楷体", Font.PLAIN, 14));
		btnAddShareFile.addActionListener(new ActionListener() {
			// 添加共享文件
			public void actionPerformed(final ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser
						.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				int ret = chooser.showOpenDialog(null);
				if (ret == JFileChooser.OPEN_DIALOG) {
					File selFile = chooser.getSelectedFile();
					if (!selFile.getName().trim().isEmpty()) {
						ShareFileMgr.getInstance().appendShareFile(selFile, "");
						initShareFileList();
					}
				}

			}
		});
		btnAddShareFile.setText("添加共享文件");
		toolBar_1.add(btnAddShareFile);

		btnRemoveShareFile = new JButton();
		btnRemoveShareFile.setFont(new Font("华文楷体", Font.PLAIN, 14));
		btnRemoveShareFile.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				int selRowIndex = table.getSelectedRow();
				if (selRowIndex >= 0) {
					DefaultTableModel model = (DefaultTableModel) table
							.getModel();
					String selFilePath = model.getValueAt(selRowIndex, 4)
							.toString();
					ShareFileMgr.getInstance().removeShareFile(selFilePath);
					initShareFileList();
				}
			}
		});
		btnRemoveShareFile.setText("删除共享文件");
		toolBar_1.add(btnRemoveShareFile);

		btnRefresh = new JButton();
		btnRefresh.setFont(new Font("华文楷体", Font.PLAIN, 14));
		btnRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				TreePath treePath = tree.getSelectionPath();
				if (treePath != null) {
					ShareFileMgr.getInstance().getRemoteUserShareFiles(
							treePath.getPath()[1].toString());

					initRemoteShareFileList();
				}
			}
		});
		btnRefresh.setEnabled(false);
		btnRefresh.setText("刷新共享列表");
		toolBar_1.add(btnRefresh);

		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setFont(new Font("华文楷体", Font.PLAIN, 14));
		panel.add(scrollPane);
		table = new JTable(rowsData, colNames);
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setFont(new Font("华文楷体", Font.PLAIN, 14));
		table.getColumnModel().getColumn(5).setMinWidth(0);
		table.getColumnModel().getColumn(5).setMaxWidth(0);
		scrollPane.setViewportView(table);

		initialize();
		//
	}

	protected void initShareFileList() {
		List<ShareFile> shareFiles = ShareFileMgr.getInstance().getShareFiles();
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		for (int i = model.getRowCount() - 1; i >= 0; i--) {
			model.removeRow(i);
		}
		for (ShareFile shareFile : shareFiles) {
			model.addRow(new Object[] { shareFile.getName(),
					shareFile.getSize(), shareFile.getShareDateTime(),
					shareFile.getDownloadTimes(), shareFile.getFilePath(),
					shareFile.getIntroduce() });
		}
	}

	private void initialize() {

	}

	// 初始化右侧的用户列表
	public void initUserListControl(List<String> onLineUsers) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("在线用户列表");
		root.add(new DefaultMutableTreeNode("我的共享"));
		for (String user : onLineUsers) {
			DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(user);
			root.add(childNode);
		}
		DefaultTreeModel treeModel = new DefaultTreeModel(root);
		this.tree.setModel(treeModel);
		this.tree.addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				// JOptionPane.showMessageDialog(null, e.getPath());
				if (e.getPath().getPathCount() == 2) {
					String path = e.getPath().toString();
					// System.out.println(path);
					if ("[在线用户列表, 我的共享]".equals(path)) {
						DataPanelFileShare.this.btnDownload.setEnabled(false);
						DataPanelFileShare.this.btnAddShareFile
								.setEnabled(true);
						DataPanelFileShare.this.btnRemoveShareFile
								.setEnabled(true);
						DataPanelFileShare.this.btnRefresh.setEnabled(false);

					} else {
						DataPanelFileShare.this.btnDownload.setEnabled(true);
						DataPanelFileShare.this.btnAddShareFile
								.setEnabled(false);
						DataPanelFileShare.this.btnRemoveShareFile
								.setEnabled(false);
						DataPanelFileShare.this.btnRefresh.setEnabled(true);

						ShareFileMgr.getInstance().getRemoteUserShareFiles(
								e.getPath().getPath()[1].toString());

						initRemoteShareFileList();
					}
				}
			}
		});
		// this.tree.setSelectionPath(new TreePath(new Object[]{"在线用户列表",
		// "我的共享"}));
		initShareFileList();
	}

	protected void initRemoteShareFileList() {
		List<ShareFile> remoteShareFiles = ShareFileMgr.getInstance()
				.getRemoteShareFiles();
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		for (int i = model.getRowCount() - 1; i >= 0; i--) {
			model.removeRow(i);
		}
		for (ShareFile shareFile : remoteShareFiles) {
			model.addRow(new Object[] { shareFile.getName(),
					shareFile.getSize(), shareFile.getShareDateTime(),
					shareFile.getDownloadTimes(), shareFile.getFilePath(),
					shareFile.getIntroduce() });
		}
	}

}
