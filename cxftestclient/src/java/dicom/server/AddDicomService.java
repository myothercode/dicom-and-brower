package dicom.server;

import javax.jws.WebService;

/**
 * Created with IntelliJ IDEA.
 * User: Chace.Cai
 * Date: 13-8-6
 * Time: 上午11:33
 * To change this template use File | Settings | File Templates.
 */
@WebService
public interface AddDicomService {
    public String AddDicom(String str);
}
