package appcenter.logHelper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author mark.yang
 *         13-6-4
 *         pasc
 */
public class LogForInfo {
	private static final Log logger = LogFactory.getLog(LogForInfo.class);

	public static void logInfo(String info) {
		logger.info(info);
	}
}
