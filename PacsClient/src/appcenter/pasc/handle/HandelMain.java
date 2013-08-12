package appcenter.pasc.handle;

import appcenter.dicom.handleDicom.SendFileMain;
import appcenter.logHelper.LogForError;
import appcenter.logHelper.LogForInfo;
import appcenter.monitor.ProgramMonitorThread;
import appcenter.pasc.domain.SharedQueue;
import appcenter.pasc.util.BaseConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author mark.yang
 *         13-5-17
 *         pasc
 */
public class HandelMain extends JComponent implements ActionListener {

	private static final long serialVersionUID = 6866337202674539068L;
	/** 储存右键菜单* */
	private static MenuItem[] items = new MenuItem[4];
	/** 初始化图标* */
	private static Image[] images = new Image[2];
	/** 设置托盘图标* */
	private static TrayIcon trayIcon;

	private DataBaseHandel dataBaseHandel;

	private FileHandel fileHandel;

	static {
		images[0] = Toolkit.getDefaultToolkit()
				.getImage(HandelMain.class.getClassLoader().getResource("image/btn_play.png"));
		images[1] = Toolkit.getDefaultToolkit()
				.getImage(HandelMain.class.getClassLoader().getResource("image/btn_stop.png"));
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			LogForError.logError(e);
		}

		HandelMain tool = new HandelMain();
		SystemTray tray = SystemTray.getSystemTray();
		PopupMenu popup = new PopupMenu();

		items[1] = new MenuItem("start");
		items[1].addActionListener(tool);
		items[1].setActionCommand("run");
		popup.add(items[1]);

		items[2] = new MenuItem("exit");
		items[2].addActionListener(tool);
		items[2].setActionCommand("exit");
		popup.add(items[2]);

		trayIcon = new TrayIcon(images[1], "服务未启动", popup);
		trayIcon.setImageAutoSize(true);
		trayIcon.addActionListener(tool);
		trayIcon.setActionCommand("trayIcon");

		try {
			tray.add(trayIcon);
		} catch(AWTException e) {
			LogForError.logError(e);
		}

		BaseConfig.initServerConfig();
		if(BaseConfig.type.equals("dataBase")){
			BaseConfig.initDatabaseConfig();
		}else if(BaseConfig.type.equals("file")){
			BaseConfig.initFileConfig();
		}
		BaseConfig.initAnalyzeConfig();
		if(BaseConfig.runMonitor.equals("true")) {
			new ProgramMonitorThread().start();
		}
		tool.doHandel();
	}


	//处理右键菜单点击执行
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if(cmd.equals("run")) {
			BaseConfig.initServerConfig();
			if(BaseConfig.pacsType.equals("dataBase")){
				BaseConfig.initDatabaseConfig();
				BaseConfig.initAnalyzeConfig();
			}else if(BaseConfig.pacsType.equals("file")){
				BaseConfig.initFileConfig();
			}
			if(BaseConfig.runMonitor.equals("true")) {
				new ProgramMonitorThread().start();
			}
			doHandel();
		}
		if(cmd.equals("stop")) {
			stopHandel();
		} else if(cmd.equals("exit")) {
			this.exitHandel();
			System.exit(0);
		}
	}

	private void exitHandel(){
		LogForInfo.logInfo("操作员执行关闭程序命令!");
		BaseConfig.updateConfig();
	}

	private void doHandel() {
		items[1].setLabel("stop");
		items[1].setActionCommand("stop");
		if(BaseConfig.type.equalsIgnoreCase("dataBase")) {
			this.dataBaseHandel = new DataBaseHandel();
			this.dataBaseHandel.start();
		} else if(BaseConfig.type.equalsIgnoreCase("file")) {
			this.fileHandel = new FileHandel();
			this.fileHandel.start();
		}

        if("true".equalsIgnoreCase(BaseConfig.runDicomService)){
            Thread t0=new SendFileMain();
            t0.start();
        }

		trayIcon.setImage(images[0]);
		trayIcon.setToolTip("服务正在运行");
	}

	public void stopHandel(){
		if(BaseConfig.type.equalsIgnoreCase("dataBase")) {
			DataBaseHandel.runFlag = false;
			this.dataBaseHandel.interrupt();//使用中断方式终止线程运行
		} else if(BaseConfig.type.equalsIgnoreCase("file")) {
			FileHandel.runFlag = false;
			FileHandel.fileCopyHandel.interrupt();
			FileHandel.fileCheckUpdateHandel.interrupt();
			this.fileHandel.interrupt();//使用中断方式终止线程运行
			SharedQueue.sendFiles.clear();//清除共享队列中的数据
		}
		items[1].setLabel("start");
		items[1].setActionCommand("run");

		trayIcon.setImage(images[1]);
		trayIcon.setToolTip("服务未启动");
	}
}
