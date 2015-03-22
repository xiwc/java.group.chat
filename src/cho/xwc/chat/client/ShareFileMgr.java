package cho.xwc.chat.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class ShareFileMgr {
	private static ShareFileMgr instance = new ShareFileMgr();
	private List<ShareFile> shareFiles = new ArrayList<ShareFile>();
	private List<ShareFile> remoteShareFiles;

	public List<ShareFile> getRemoteShareFiles() {
		return remoteShareFiles;
	}

	public void setRemoteShareFiles(List<ShareFile> remoteShareFiles) {
		this.remoteShareFiles = remoteShareFiles;
	}

	public List<ShareFile> getShareFiles() {
		return shareFiles;
	}

	public void setShareFiles(List<ShareFile> shareFiles) {
		this.shareFiles = shareFiles;
	}

	final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");

	private ShareFileMgr() {
	}

	public static ShareFileMgr getInstance() {
		return instance;
	}

	// 添加共享文件
	public boolean appendShareFile(File file, String introduce) {
		ShareFile shareFile = new ShareFile();
		shareFile.setName(file.getName());
		shareFile.setSize(file.length());
		shareFile.setShareDateTime(format.format(new Date()));
		shareFile.setFilePath(file.getPath());
		shareFile.setIntroduce(introduce);
		shareFiles.add(shareFile);
		return true;
	}

	// 删除共享文件
	public boolean removeShareFile(String filePath) {
		ShareFile willDel = null;
		for (ShareFile file : shareFiles) {
			if (file.getFilePath().equals(filePath)) {
				willDel = file;
				break;
			}
		}
		if (willDel != null) {
			shareFiles.remove(willDel);
		}
		return true;
	}

	private UserClient shareFileClient;

	public List<ShareFile> getRemoteUserShareFiles(String user) {
		String host = Utils.getIp(user);
		int port = Integer.parseInt(Utils.getPort(user));
		remoteShareFiles = null;
		try {
			shareFileClient = new UserClient(new Socket(host, port));
			shareFileClient.sendMsg("FileShare");
			shareFileClient.sendMsg("ListAllShareFiles");
			ObjectInputStream ois = new ObjectInputStream(shareFileClient
					.getIn());
			try {
				this.remoteShareFiles = (List<ShareFile>) ois.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				shareFileClient.getSocket().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return this.remoteShareFiles;
	}

	// 向请求者发送共享的文件列表
	public void sendAllShareFiles(UserClient userClient) {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(userClient.getOut());
			oos.writeObject(shareFiles);
			oos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				oos.close();
				userClient.getSocket().close();
			} catch (Exception e2) {
			}
		}
	}

	public void download(String user, String filePath) {
		String host = Utils.getIp(user);
		int port = Integer.parseInt(Utils.getPort(user));
		try {
			UserClient downloadClient = new UserClient(new Socket(host, port));
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int ret = chooser.showOpenDialog(null);
			if (ret == JFileChooser.OPEN_DIALOG) {
				File file = chooser.getSelectedFile();
				String path = file.getAbsolutePath();
				receiveShareFile(downloadClient, path, filePath);
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				shareFileClient.getSocket().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	//接收下载的共享文件
	private void receiveShareFile(final UserClient recvClient, final String path, final String filePath) {
		Runnable recv = new Runnable() {
			private String folderPath = "";
			private boolean isFirstTime = true;
			private String folderName = "";

			@Override
			public void run() {
				System.out.println("文件接收开始");
				recvClient.sendMsg("FileShare");
				recvClient.sendMsg("Download");
				recvClient.sendMsg(filePath);
				long haveRecvLen = 0;// 已经接收的文件长度
				// get folder total length
				long folderLen = 0;
				try {
					folderLen = recvClient.getIn().readLong();
				} catch (IOException e2) {
					e2.printStackTrace();
				}

				while (true) {
					FileOutputStream fos = null;
					try {
						String recvCmd = recvClient.receiveMsg();
						if ("BeginFolderT".equals(recvCmd)) {
							String subFolder = recvClient.receiveMsg();
							if (isFirstTime) {
								folderName = subFolder;
								isFirstTime = false;
							}
							if (path.endsWith(File.separator)) {
								folderPath = path + subFolder;
							} else {
								folderPath = path + File.separator + subFolder;
							}
							File file = new File(folderPath);
							file.mkdirs();
						} else if ("BeginFileT".equals(recvCmd)) {
							String fileName = recvClient.receiveMsg();
							String filePath = folderPath + File.separator
									+ fileName;
							fos = new FileOutputStream(filePath);

							byte[] bs = new byte[BUF_LEN];
							int count = recvClient.getIn().readInt();
							while (count != -1) {
								recvClient.getIn().readFully(bs, 0, count);
								fos.write(bs, 0, count);
								fos.flush();
								haveRecvLen += count;
								count = recvClient.getIn().readInt();
							}
							fos.close();
						} else if ("EndFolderT".equals(recvCmd)) {
							 recvClient.getSocket().close();
							 JOptionPane.showMessageDialog(null, "共享文件"+ new File(filePath).getName() + "下载完毕!");
							break;
						} else if ("OnlyFile".equals(recvCmd)) {
							if (path.endsWith(File.separator)) {
								folderPath = path.substring(0,
										path.length() - 1);
							} else {
								folderPath = path;
							}
							new File(folderPath).mkdirs();
							folderName = recvClient.receiveMsg();
						}
					} catch (IOException e) {
						e.printStackTrace();
						if (fos != null) {
							try {
								fos.close();
								fos = null;
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
						break;
					}
				}
			}
		};
		new Thread(recv).start();
	}

	private static final int BUF_LEN = 102400;

	// 响应远程用户的文件下载命令,发送共享文件给请求方.
	public void sendFile(final String folderPath, final UserClient sendClient) {
		Runnable conn = new Runnable() {
			private long totalLen = 0L;
			private String folderName;
			private String rootPath = null;
			private long haveSendLen = 0L;

			@Override
			public void run() {
				System.out.println("文件发送开始");
				this.folderName = new File(folderPath).getName();
				File folder = new File(folderPath);
				if (folder.isFile()) {
					totalLen = folder.length();
					try {
						sendClient.sendLong(totalLen);
						sendClient.sendMsg("OnlyFile");
						sendClient.sendMsg(folder.getName());
						sendFile(folder);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					rootPath = folder.getAbsolutePath();
					getFolderTotalLen(folderPath);
					try {
						sendClient.sendLong(totalLen);
						sendFolder(folder);
					} catch (IOException e) {
						e.printStackTrace();
						return;
					}
				}
				sendClient.sendMsg("EndFolderT");
				downloadCount(folderPath);
				try {
					sendClient.getSocket().close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			private void sendFolder(File folder) {
				sendClient.sendMsg("BeginFolderT");
				String path = folder.getAbsolutePath();
				int index = rootPath.length() - folderName.length();
				String fPath = path.substring(index);
				sendClient.sendMsg(fPath);
				File[] files = folder.listFiles();
				List<File> listFile = new ArrayList<File>();
				List<File> listFolder = new ArrayList<File>();
				for (File file : files) {
					if (file.isFile()) {
						listFile.add(file);
					} else if (file.isDirectory()) {
						listFolder.add(file);
					}
				}
				for (File file : listFile) {
					sendFile(file);
				}
				for (File file : listFolder) {
					sendFolder(file);
				}
			}

			private boolean sendFile(File file) {
				sendClient.sendMsg("BeginFileT");
				sendClient.sendMsg(file.getName());
				FileInputStream fis = null;
				try {
					fis = new FileInputStream(file);
					byte[] buf = new byte[BUF_LEN];
					int len = fis.read(buf);
					while (len != -1) {
						haveSendLen += len;
						sendClient.sendByte(buf, len);
						len = fis.read(buf);
					}
					sendClient.getOut().writeInt(len);
					fis.close();
					return true;
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
					if (fis != null) {
						try {
							fis.close();
							file = null;
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
				return false;
			}

			private void getFolderTotalLen(String path) {
				this.totalLen = 0L;
				File folder = new File(path);
				getFileLen(folder);
			}

			private void getFileLen(File folder) {
				File[] files = folder.listFiles();
				for (File file : files) {
					if (file.isFile()) {
						this.totalLen += file.length();
					} else if (file.isDirectory()) {
						getFileLen(file);
					}
				}
			}

		};
		new Thread(conn).start();
	}

	protected void downloadCount(String folderPath) {
		for (ShareFile shareFile : shareFiles) {
			if(shareFile.getFilePath().equals(folderPath)){
				shareFile.setDownloadTimes(shareFile.getDownloadTimes()+1);
				break;
			}
		}
	}
}

class ShareFile implements Serializable {
	String name;
	long size;
	String shareDateTime;
	int downloadTimes;
	String introduce;
	String filePath;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getShareDateTime() {
		return shareDateTime;
	}

	public void setShareDateTime(String shareDateTime) {
		this.shareDateTime = shareDateTime;
	}

	public int getDownloadTimes() {
		return downloadTimes;
	}

	public void setDownloadTimes(int downloadTimes) {
		this.downloadTimes = downloadTimes;
	}

	public String getIntroduce() {
		return introduce;
	}

	public void setIntroduce(String introduce) {
		this.introduce = introduce;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

}
