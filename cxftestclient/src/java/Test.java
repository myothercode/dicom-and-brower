import dicom.server.AddDicomService;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created with IntelliJ IDEA.
 * User: Chace.Cai
 * Date: 13-8-6
 * Time: 下午4:14
 * To change this template use File | Settings | File Templates.
 */
public class Test {
    public static void main(String[] args) throws Exception {
        /*ApplicationContext context=new ClassPathXmlApplicationContext("applicationContext-client.xml");
        AddDicomService a= (AddDicomService)context.getBean("client");
        String b=a.AddDicom("d");
        System.out.println(b);*/
        JaxWsDynamicClientFactory jw=JaxWsDynamicClientFactory.newInstance();
        Client c=jw.createClient("http://localhost:8080/adddicom/ws/addDicomService?wsdl");
        Object[] a=c.invoke("addDicom","ss");

        System.out.println(a[0].toString());

    }
}
