package appcenter.pasc.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author mark.yang
 *         13-6-27
 *         pasc
 */
public class ShareDBResource {
	public static List<DBResource> dbResources=new CopyOnWriteArrayList<DBResource>();
	public static List<DBResource> errorDbResources=new ArrayList<DBResource>();
}
