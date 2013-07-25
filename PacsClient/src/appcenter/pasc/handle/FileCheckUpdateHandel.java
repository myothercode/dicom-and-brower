package appcenter.pasc.handle;

import appcenter.logHelper.LogForError;
import appcenter.logHelper.LogForInfo;
import appcenter.pasc.domain.FileValidError;
import appcenter.pasc.util.BaseConfig;
import appcenter.pasc.util.CommonUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mark.yang
 *         13-6-23
 *         pasc
 */
public class FileCheckUpdateHandel extends Thread {
	@Override
	public void run() {
		LogForInfo.logInfo("提取1天内被修改的报告,并复制文件到" + BaseConfig.fileToDir + "文件夹线程启动!");
		this.loadLog();
		while(true) {//根据file处理主线程的标记判断时候需要继续执行文件copy
			this.doHandel();
			CommonUtil.pauseTime(Integer.parseInt(BaseConfig.checkUpdateTime) * 60 * 1000, true,
					"终止文件复制线程运行!");//通过时间配置BaseConfig.runTime分钟执行一次copy线程。
		}
	}

	private void doHandel() {
		if(!FileValidError.strings.isEmpty() && FileValidError.strings.size() > 10) {
			List<String> strings = new ArrayList<String>();
			for(int i = 0;i < 10;i++) {
				strings.add(FileValidError.strings.get(i));
			}
			for(String fi : strings) {
				File file = new File(BaseConfig.fileDir + "/" + fi);
				new FileValidThread(file).start();
				FileValidError.strings.remove(fi);
			}
		} else if(!FileValidError.strings.isEmpty() && FileValidError.strings.size() <= 10) {
			for(String fi : FileValidError.strings) {
				File file = new File(BaseConfig.fileDir + "/" + fi);
				new FileValidThread(file).start();
				FileValidError.strings.remove(fi);
			}
		}
	}

	private void loadLog() {
		File log = new File("log/validError.log");
		try {
			FileReader fileReader = new FileReader(log);
			BufferedReader reader = new BufferedReader(fileReader);
			String temp = null;
			while((temp = reader.readLine()) != null) {
				File file = new File(BaseConfig.fileDir + "/" + temp);
				List<File> fileList = new ArrayList<File>();
				CommonUtil.findFiles(file, "*.doc", fileList);
				if(fileList.isEmpty()) {
					continue;
				}
				boolean validRepeat = false;
				for(int i = 0;i < FileValidError.strings.size();i++) {
					if(FileValidError.strings.get(i).equals(file.getName())) {
						validRepeat = true;
					}
				}
				if(validRepeat) {
					continue;
				}
				FileValidError.strings.add(file.getName());
			}
			reader.close();
			fileReader.close();
		} catch(FileNotFoundException e) {
			LogForError.logError(e);
		} catch(IOException e) {
			LogForError.logError(e);
		}
	}
}
