package appcenter.pasc.domain;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author mark.yang
 *         13-5-20
 *         pasc
 */
public class SharedQueue {
	public static List<ShareFile> sendFiles = new CopyOnWriteArrayList<ShareFile>();
}
