package appcenter.pasc.util;

import appcenter.logHelper.LogForError;
import appcenter.logHelper.LogForInfo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mark.yang
 *         13-5-19
 *         pasc
 */
public class CommonUtil {

	public static void copyFile(File sourceFile, File targetFile) throws IOException {
		InputStream inBuff = null;
		OutputStream outBuff = null;
		try {
			inBuff = new FileInputStream(sourceFile);
			outBuff = new FileOutputStream(targetFile);
			byte[] b = new byte[1024];
			int len;
			while((len = inBuff.read(b)) != -1) {
				outBuff.write(b, 0, len);
			}
			outBuff.flush();
		} finally {
			if(outBuff != null)
				outBuff.close();
			if(inBuff != null)
				inBuff.close();
		}
	}

	public static void pauseTime(long pauseTime) {
		try {
			Thread.sleep(pauseTime); //读取配置文件数据暂停线程执行
		} catch(InterruptedException e) {
			LogForError.logError(e);
		}
	}

	public static void pauseTime(long pauseTime, boolean isHandelMainThread, String msg) {
		try {
			Thread.sleep(pauseTime); //读取配置文件数据暂停线程执行
		} catch(InterruptedException e) {
			LogForError.logError(e);
			if(isHandelMainThread) {
				LogForInfo.logInfo(msg);
			}
		}
	}

	/**
	 * 递归查找文件
	 *
	 * @param baseFile 查找的文件夹路径
	 * @param targetFileName 需要查找的文件名
	 * @param fileList 查找到的文件集合
	 */
	public static void findFiles(File baseFile, String targetFileName, List<File> fileList) {
		/**
		 * 算法简述：
		 * 从某个给定的需查找的文件夹出发，搜索该文件夹的所有子文件夹及文件，
		 * 若为文件，则进行匹配，匹配成功则加入结果集，若为子文件夹，则进队列。
		 * 队列不空，重复上述操作，队列为空，程序结束，返回结果。
		 */
		File[] children = baseFile.listFiles();
		if(children != null) {
			for(int i = 0;i < children.length;i++) {
				File file = children[i];
				if(!file.isDirectory()) {
					if(CommonUtil.wildcardMatch(targetFileName, file.getName())) {
						fileList.add(file.getAbsoluteFile());
					}
				} else if(file.isDirectory()) {
					findFiles(file, targetFileName, fileList);
				}
			}
		}
	}

	/**
	 * 通配符匹配
	 *
	 * @param pattern 通配符模式
	 * @param str 待匹配的字符串
	 * @return 匹配成功则返回true，否则返回false
	 */
	private static boolean wildcardMatch(String pattern, String str) {
		int patternLength = pattern.length();
		int strLength = str.length();
		int strIndex = 0;
		char ch;
		for(int patternIndex = 0;patternIndex < patternLength;patternIndex++) {
			ch = pattern.charAt(patternIndex);
			if(ch == '*') {
				//通配符星号*表示可以匹配任意多个字符
				while(strIndex < strLength) {
					if(wildcardMatch(pattern.substring(patternIndex + 1), str.substring(strIndex))) {
						return true;
					}
					strIndex++;
				}
			} else if(ch == '?') {
				//通配符问号?表示匹配任意一个字符
				strIndex++;
				if(strIndex > strLength) {
					//表示str中已经没有字符匹配?了。
					return false;
				}
			} else {
				if((strIndex >= strLength) || (ch != str.charAt(strIndex))) {
					return false;
				}
				strIndex++;
			}
		}
		return (strIndex == strLength);
	}

	/**
	 * 将多个图片流拼装成一个流
	 *
	 * @param streams
	 * @return
	 * @throws IOException
	 */
	public static ByteArrayInputStream mergeInPutStream(ByteArrayOutputStream[] streams) throws IOException {
		BufferedImage[] bufferedImages = new BufferedImage[streams.length];

		for(int i = 0;i < bufferedImages.length;i++) {
			bufferedImages[i] = ImageIO.read(new ByteArrayInputStream(streams[i].toByteArray()));
		}

		int totalHeight = 0;
		for(BufferedImage bufferedImage : bufferedImages) {
			totalHeight += bufferedImage.getHeight();
		}

		int width = bufferedImages[0].getWidth();
		BufferedImage finalImage = new BufferedImage(width, totalHeight, bufferedImages[0].getType());

		Graphics graphics = finalImage.getGraphics();
		int drawHeight = 0;
		for(BufferedImage bufferedImage : bufferedImages) {
			graphics.drawImage(bufferedImage, 0, drawHeight, width, bufferedImage.getHeight(), null);
			drawHeight += bufferedImage.getHeight();
		}
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ImageIO.write(finalImage, "jpg", byteArrayOutputStream);

		for(ByteArrayOutputStream arrayOutputStream : streams) {
			if(arrayOutputStream != null) {
				arrayOutputStream.reset();
			}
		}

		return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
	}

	public static ByteArrayOutputStream mergeToOutPutStream(ByteArrayOutputStream[] streams) throws IOException {
		BufferedImage[] bufferedImages = new BufferedImage[streams.length];

		for(int i = 0;i < bufferedImages.length;i++) {
			bufferedImages[i] = ImageIO.read(new ByteArrayInputStream(streams[i].toByteArray()));
		}

		int totalHeight = 0;
		for(BufferedImage bufferedImage : bufferedImages) {
			totalHeight += bufferedImage.getHeight();
		}

		int width = bufferedImages[0].getWidth();
		BufferedImage finalImage = new BufferedImage(width, totalHeight, bufferedImages[0].getType());

		Graphics graphics = finalImage.getGraphics();
		int drawHeight = 0;
		for(BufferedImage bufferedImage : bufferedImages) {
			graphics.drawImage(bufferedImage, 0, drawHeight, width, bufferedImage.getHeight(), null);
			drawHeight += bufferedImage.getHeight();
		}
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ImageIO.write(finalImage, "jpg", byteArrayOutputStream);

		for(ByteArrayOutputStream arrayOutputStream : streams) {
			if(arrayOutputStream != null) {
				arrayOutputStream.reset();
			}
		}

		return byteArrayOutputStream;
	}

	public static void saveIoToFile(String source, File file) {
		if(source == null || source.length() == 0) {
			return;
		}
		try {
			FileOutputStream out = new FileOutputStream(file);
			byte[] bytes = source.getBytes();
			for(int i = 0;i < bytes.length;i += 2) {
				out.write(charToInt(bytes[i]) * 16 + charToInt(bytes[i + 1]));
			}
			out.close();
		} catch(Exception e) {
			LogForError.logError(e);
		}
	}

	private static int charToInt(byte ch) {
		int val = 0;
		if(ch >= 0x30 && ch <= 0x39) {
			val = ch - 0x30;
		} else if(ch >= 0x41 && ch <= 0x46) {
			val = ch - 0x41 + 10;
		}
		return val;
	}

	/**
	 * word文档判断最后一次修改的文件 并且排除临时文件
	 * @param files
	 * @return
	 */
	public static File getTheLastCreateFile(List<File> files) {
		File returnFile = null;
		if(files.size() == 1 && !files.get(0).getName().startsWith("~$")) {
			returnFile = files.get(0);
		} else {
			for(int i = 0;i < files.size();i++) {
				if(returnFile == null && !files.get(i).getName().startsWith("~$")) {
					returnFile = files.get(i);
					continue;
				}
				if(returnFile.lastModified() < files.get(i).lastModified() && !files.get(i).getName()
						.startsWith("~$")) {
					returnFile = files.get(i);
				}
			}
		}
		return returnFile;
	}

	//进行对象深度克隆
	public final static Object objectCopy(ByteArrayInputStream oldObj) {
		ByteArrayInputStream newObj = null;
		try {
			ByteArrayInputStream bi = oldObj;
			ObjectInputStream oi = new ObjectInputStream(bi);
			newObj = (ByteArrayInputStream) oi.readObject();//目标对象
		} catch(IOException e) {
			LogForError.logError(e);
		} catch(ClassNotFoundException e) {
			LogForError.logError(e);
		}
		return newObj;
	}

	/** I\O读取图片为数组 */
	public static byte[] image2Bytes(String imagePath) throws Exception {
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(imagePath));
		ByteArrayOutputStream out = new ByteArrayOutputStream(in.available());

		byte[] temp = new byte[in.available()];
		int size = 0;
		while((size = in.read(temp)) != -1) {
			out.write(temp, 0, size);
		}
		in.close();

		byte[] content = out.toByteArray();
		out.close();
		return content;
	}

	/**
	 * 用于超长字符串截取
	 *
	 * @param resource 字符串源
	 * @param splitStr 截取标识
	 * @param count 每行字符数
	 * @return
	 */
	public static List<String> cutString2Wrap(String resource, String splitStr, int count) {
		List<String> itemList = new ArrayList<String>();
		String[] item = resource.split(splitStr);
		if(null != item) {
			for(String value : item) {
				//跳过空字符串
				if(null == value || value.trim().length() == 0) {
					continue;
				}
				//暂定每行储存40个字符
				if(value.length() <= count) {
					itemList.add(value);
				} else {
					int length = (int) Math.ceil(value.length() / count);//获取总换行数，向上取整
					for(int i = 0;i < length;i++) {
						String temp = null;
						if(i == length - 1) {
							temp = value.substring(i * count, value.length());
						} else {
							temp = value.substring(i * count, (i + 1) * count);
						}
						itemList.add(temp);
					}
				}
			}
		} else {
			int length = (int) Math.ceil(resource.length() / count);
			for(int i = 0;i < length;i++) {
				String temp = null;
				if(i == length - 1) {
					temp = resource.substring(i * count, resource.length());
				} else {
					temp = resource.substring(i * count, (i + 1) * count);
				}
				itemList.add(temp);
			}
		}
		return itemList;
	}

	/**
	 * 在图片上写长字符串
	 * @param graphics2D
	 * @param resource 源数据
	 * @param lineLength 一行数据的长度
	 * @param wordHigh		单字的高度
	 * @param baseHeight	基础高度
	 * @param firstWordLeft 每行第一个字与左边的距离
	 * @return
	 */
	public static void drawLongWord(Graphics2D graphics2D,String resource,int lineLength,int wordHigh,int baseHeight,int firstWordLeft){
		double base1=resource.length()/lineLength;
		int  base2=(int)(resource.length()/lineLength);
		int row= (int)(base1*10)>base2?base2+1:base2;
		for(int i=0;i<row;i++){
			String temp;
			if(i+1==row){
				temp=resource.substring(i*lineLength,resource.length());
			}else{
				temp=resource.substring(i*lineLength,(i+1)*lineLength);
			}
			graphics2D.drawString(temp, firstWordLeft, baseHeight+i*wordHigh);
		}
	}

	/**
	 * 将图片缩放到指定大小并画到页面上
	 * @param graphics2D
	 * @param imagePath		图片路径
	 * @param x 			X坐标
	 * @param y				Y坐标
	 * @param pWidth		缩放到宽度
	 * @param pHeight		缩放到高度
	 * @param count 		第几个图片
	 */
	public static void drawImage(Graphics2D graphics2D,String imagePath,int x,int y,int pWidth,int pHeight,int count) {
		try {
			BufferedImage bufferedImage=ImageIO.read(new File(imagePath));
			BufferedImage target = new BufferedImage(pWidth,pHeight, BufferedImage.TYPE_INT_RGB);
			target.getGraphics().drawImage(bufferedImage.getScaledInstance(pWidth,pHeight,bufferedImage.SCALE_SMOOTH), 0, 0, null);
			ByteArrayOutputStream  byteArrayOutputStream=new ByteArrayOutputStream();
			ImageIO.write(target, "JPG", byteArrayOutputStream);

			graphics2D.drawImage(target,new BufferedImageOp() {
				@Override
				public BufferedImage filter(BufferedImage src, BufferedImage dest) {
					return src;
				}
				@Override
				public Rectangle2D getBounds2D(BufferedImage src) {
					return null;
				}
				@Override
				public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
					return null;
				}
				@Override
				public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
					return null;
				}
				@Override
				public RenderingHints getRenderingHints() {
					return null;
				}
			},x+(count==1?(count-1)*pWidth:(count-1)*pWidth+50),y);
		} catch(IOException e) {
			LogForError.logError(e);
		}
	}

}
