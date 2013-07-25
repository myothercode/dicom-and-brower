package appcenter.monitor;

import appcenter.pasc.util.BaseConfig;
import appcenter.pasc.util.CommonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author mark.yang
 *         13-5-27
 *         pasc
 */
public class ProgramMonitorThread extends Thread  {

	public static final Log logger= LogFactory.getLog(ProgramMonitorThread.class);

	@Override
	public void run() {
		while(true) {
			try {
				int  processCount=0;
				Process process = Runtime.getRuntime().exec("cmd.exe /c tasklist | find /i \""+ BaseConfig.monitorName +"\"");
				BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line = "";
				logger.info("监控线程监控程序运行情况:");
				while ((line = input.readLine()) != null){
					processCount++;
					logger.info(line);
				}
				if(processCount>1){
					logger.info("有多个任务在运行，本次运行将会停止");
					System.exit(0);
				}
				input.close();
			} catch(IOException e) {
				logger.error(e.getMessage(),e);
			}
			CommonUtil.pauseTime(Integer.parseInt(BaseConfig.monitorTime) * 60 * 1000);
		}
	}
}
