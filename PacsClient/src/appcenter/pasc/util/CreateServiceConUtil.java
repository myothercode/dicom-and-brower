package appcenter.pasc.util;

import appcenter.logHelper.LogForError;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author mark.yang
 *         13-5-18
 *         pasc
 */
public class CreateServiceConUtil {

	//webService申明集合
	private static Map<String, Object> jaxWsProxyFactoryBeans = new HashMap<String, Object>();

	public static boolean createUtilServiceObj() {
		if(!BaseConfig.serviceClass.isEmpty()) {
			Iterator<String> it = BaseConfig.serviceClass.keySet().iterator();
			while(it.hasNext()) {
				try {
					String serviceUrlKey = it.next();
					JaxWsProxyFactoryBean svr = new JaxWsProxyFactoryBean();
					svr.setServiceClass(BaseConfig.serviceClass.get(serviceUrlKey));
					svr.setAddress(serviceUrlKey);
					LoggingInInterceptor loggingInInterceptor=new LoggingInInterceptor();
					loggingInInterceptor.setPrettyLogging(false);
					loggingInInterceptor.setShowBinaryContent(false);
					svr.getInInterceptors().add(loggingInInterceptor);
					LoggingOutInterceptor loggingOutInterceptor=new LoggingOutInterceptor();
					loggingOutInterceptor.setPrettyLogging(false);
					loggingOutInterceptor.setShowBinaryContent(false);
					svr.getOutInterceptors().add(loggingOutInterceptor);
					jaxWsProxyFactoryBeans.put(serviceUrlKey, svr.create());
				} catch(RuntimeException e) {
					LogForError.logError(e);
					return false;
				}
			}
		}


		return true;
	}

	public static Object getJaxWsProxyFactoryBean(String serviceUrl) {
		return jaxWsProxyFactoryBeans.get(serviceUrl);
	}

}
