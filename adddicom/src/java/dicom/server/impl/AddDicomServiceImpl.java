package dicom.server.impl;

import dicom.server.AddDicomService;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * Created with IntelliJ IDEA.
 * User: Chace.Cai
 * Date: 13-8-6
 * Time: 上午11:42
 * To change this template use File | Settings | File Templates.
 */
@WebService(serviceName = "addDicomService",portName = "AddDicomServicePort",targetNamespace = "http://server.dicom/")
public class AddDicomServiceImpl implements AddDicomService {

    @Override
    @WebMethod(operationName = "addDicom")
    public String addDicom(@WebParam(name = "str") String str) {
        return "hello, " + str;
    }
}
