package appcenter.pasc.handle;

import appcenter.logHelper.LogForError;
import appcenter.pasc.util.CommonUtil;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ReportImageHandle {

	private static final String TEMPPATH = "reportImage/template/";


	/** I\O读取图片为数组 */
	private static byte[] image2Bytes(String imagePath) throws Exception {
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

	/** 往图片中写入数据 */
	public ByteArrayOutputStream writeInfo2Image(String r, String id) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		File newFile = new File(TEMPPATH + id + ".jpg");
		try {
			newFile.createNewFile();
		} catch(IOException e2) {
			LogForError.logError(e2);
		}
		CommonUtil.saveIoToFile(r, newFile);
		byte[] bz = null;
		String data = null;
		try {
			bz = image2Bytes(TEMPPATH + id + ".jpg");
			data = new String(bz, "GBK");
		} catch(Exception e1) {
			LogForError.logError(e1);
		}
		String info = data.trim().substring(data.indexOf("Picture="), data.trim().length());// 去除无用数据
		String item[] = info.split("Item");
		try {
			FileImageInputStream fiis = new FileImageInputStream(new File("reportImage/template/X.jpg"));//获取报告模板
			byte[] image = new byte[(int) fiis.length()];
			fiis.read(image);
			ByteArrayInputStream bais = new ByteArrayInputStream(image);
			MemoryCacheImageInputStream mciis = new MemoryCacheImageInputStream(bais);
			BufferedImage bi = ImageIO.read(mciis);//获取BufferedImage对象，准备绘图
			Graphics2D g = (Graphics2D) bi.getGraphics();// 获取到Graphics2D之后就可以对图像进行绘图操作
			g.setColor(Color.BLACK);//字体颜色
			g.setFont(new Font("宋体", Font.PLAIN, 47));//字体样式
			g.drawString(cutString(item[3]), 1950, 453);// ID号
			g.drawString(cutString(item[6]), 270, 583);// 姓名
			g.drawString(cutString(item[7]), 848, 583);// 性别
			g.drawString(cutString(item[8]), 1440, 580);// 年龄
			g.drawString(cutString(item[15]), 1990, 580);// 病床号
			g.drawString(cutString(item[14]), 310, 678);// 住院号
			g.drawString(cutString(item[22]), 890, 678);// 门诊号
			g.drawString(cutString(item[9]), 1530, 675);// 检查日期
			g.drawString(cutString(item[16]), 270, 775);// 科别
			g.drawString(cutString(item[17]), 945, 778);// 检查部位
			if(cutString(item[26]).length() > 40 || cutString(item[26]).contains("\r\n")) {
				List<String> showList = new ArrayList<String>();
				showList = cutString2Wrap(cutString(item[26]));
				for(int i = 0;i < showList.size();i++) {
					g.drawString(showList.get(i), 240, 1150 + i * 80);// 检查所见
				}
			} else {
				g.drawString(cutString(item[26]), 240, 1150);// 检查所见
			}
			if(cutString(item[27]).length() > 40 || cutString(item[27]).contains("\\r\\n") || cutString(item[27])
					.startsWith("\\r\\n")) {
				List<String> resultList = new ArrayList<String>();
				resultList = cutString2Wrap(cutString(item[27]));
				for(int i = 0;i < resultList.size();i++) {
					g.drawString(resultList.get(i), 240, 2200 + i * 80);// 检查所得
				}
			} else {
				g.drawString(cutString(item[27]), 240, 2200);// 检查所得
			}
			g.drawString(cutString(item[30]), 360, 3070);// 报告医生
			g.drawString(cutString(item[31]), 890, 3070);// 诊断医生
			g.drawString(cutString(item[32]), 1400, 3070);// 报告日期
			g.dispose();
			ImageIO.write(bi, "JPG", byteArrayOutputStream);
		} catch(FileNotFoundException e) {
			LogForError.logError(e);
		} catch(IOException e) {
			LogForError.logError(e);
		}
		newFile.delete();
		return byteArrayOutputStream;
	}

	/** 切割字符串，获取值 */
	private String cutString(String info) {
		String value = null;
		if(info.contains("Text='")) {
			value = info.trim().substring(info.trim().indexOf("Text='") + 6, info.trim().lastIndexOf("'"));
		} else {
			value = "";
		}

		return value;
	}

	/** 因为drawString不支持自动换行，所以需要手动切割，循环调用drawString换行 */
	private List<String> cutString2Wrap(String date) {
		List<String> itemList = new ArrayList<String>();
		String item[] = null;
		//如存在换行符，则按照换行符换行
		if(date.contains("\\r\\n")) {
			item = date.split("\\\\r\\\\n");
		}
		if(null != item) {
			for(String value : item) {
				//跳过空字符串
				if(null == value || value.trim().length() == 0) {
					continue;
				}
				//暂定每行储存40个字符
				if(value.length() <= 40) {
					itemList.add(value);
				} else {
					int length = (int) Math.ceil(value.length() / 40.0);//获取总换行数，向上取整
					for(int i = 0;i < length;i++) {
						String temp = null;
						if(i == length - 1) {
							temp = value.substring(i * 40, value.length());
						} else {
							temp = value.substring(i * 40, (i + 1) * 40);
						}
						itemList.add(temp);
					}
				}
			}
		} else {
			int length = (int) Math.ceil(date.length() / 40.0);
			for(int i = 0;i < length;i++) {
				String temp = null;
				if(i == length - 1) {
					temp = date.substring(i * 40, date.length());
				} else {
					temp = date.substring(i * 40, (i + 1) * 40);
				}
				itemList.add(temp);
			}
		}

		return itemList;
	}

}
