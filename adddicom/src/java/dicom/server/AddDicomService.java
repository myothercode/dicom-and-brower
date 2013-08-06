package dicom.server;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * Created with IntelliJ IDEA.
 * User: Chace.Cai
 * Date: 13-8-6
 * Time: 上午11:33
 * To change this template use File | Settings | File Templates.
 */
@WebService(serviceName = "addDicomService")
public interface AddDicomService {
    @WebMethod(operationName = "addDicom")
    public String addDicom(@WebParam(name = "str") String str);
}
