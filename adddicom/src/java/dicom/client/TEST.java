package dicom.client;

import dicom.server.AddDicomService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created with IntelliJ IDEA.
 * User: Chace.Cai
 * Date: 13-8-6
 * Time: 下午3:31
 * To change this template use File | Settings | File Templates.
 */
public class TEST {
    public static void main(String[] args){
        ApplicationContext context=new ClassPathXmlApplicationContext("applicationContext-client.xml");
        AddDicomService a= (AddDicomService)context.getBean("client");
        String b=a.addDicom("d");
        System.out.println(b);
    }
}
