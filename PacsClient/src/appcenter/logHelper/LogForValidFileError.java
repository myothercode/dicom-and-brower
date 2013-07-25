package appcenter.logHelper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author mark.yang
 *         13-6-24
 *         pasc
 */
public class LogForValidFileError {
	private static final Log logger = LogFactory.getLog(LogForValidFileError.class);

	public static void logInfo(String info) {
		logger.info(info);
	}
}
