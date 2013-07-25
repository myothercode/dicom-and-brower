package appcenter.logHelper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author mark.yang
 *         13-6-4
 *         pasc
 */
public class LogForError {
	private static final Log logger = LogFactory.getLog(LogForError.class);

	public static void logError(Throwable throwable) {
		logger.error(throwable.getMessage(), throwable);
	}
}
