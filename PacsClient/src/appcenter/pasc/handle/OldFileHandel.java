package appcenter.pasc.handle;

import appcenter.pasc.domain.ShareFile;
import appcenter.pasc.domain.SharedQueue;
import appcenter.pasc.util.BaseConfig;

import java.io.File;
import java.io.FileFilter;

/**
 * @author mark.yang
 *         13-5-21
 *         pasc
 */
public class OldFileHandel {

	public static void loadOldFileTOShare(){
		File file=new File(BaseConfig.fileToDir);
		if(file.exists()){
			File[] children=file.listFiles(new FileFilter() {
				@Override
				public boolean accept(File file) {
					if(BaseConfig.addOldFile.equals("true")){
						if(file.getAbsolutePath().endsWith("_sendError")){
							file.renameTo(new File(file.getAbsolutePath().replace("_sendError","")));
						}
						return file.isDirectory();
					}else{
						return file.isDirectory() && !file.getAbsolutePath().endsWith("_sendError");
					}
				}
			});

			if(children.length>0){
				for(File fi : children){
					File[] isFile=fi.listFiles();
					if(isFile.length==1){
						ShareFile shareFile=new ShareFile();
						shareFile.setFile(isFile[0]);
						shareFile.setPatientID(fi.getName());
						SharedQueue.sendFiles.add(shareFile);
					}

				}
			}
		}
	}

}
