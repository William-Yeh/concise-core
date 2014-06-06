package org.sustudio.concise.core.corpus.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * 處理檔案的名稱、MD5驗證碼
 * 
 * @author Kuan-ming Su
 *
 */
public class ConciseFileUtils {

	/**
	 * Returns unique filename by appending (1) or (2) or (n) to filename
	 * @param file
	 * @return 唯一的檔案名稱，如果重複會在後面加上(1)或(2)或...
	 */
	public static File getUniqueFile(File file) {
		while (file.exists()) {
			String filePath = file.getPath();
			String fileExtension = FilenameUtils.getExtension(filePath);
			String fileNoExtension = FilenameUtils.removeExtension(filePath);
			if (fileNoExtension.matches(".*\\(\\d\\)$")) {
				int num = Integer.valueOf(fileNoExtension.replaceAll(".*\\((\\d+)\\)$", "$1"));
				num++;
				fileNoExtension = fileNoExtension.substring(0, fileNoExtension.lastIndexOf('(')) 
									+ "(" + String.valueOf(num) + ")";
			}
			else {
				fileNoExtension += "(1)";
			}
			file = new File(fileNoExtension + "." + fileExtension);
			return getUniqueFile(file);
		}
		return file;
	}
	
	
	/**
	 * 傳回檔案的MD5驗證碼，用來比較兩個檔案是否相同
	 * @param file
	 * @return 檔案的MD5驗證碼
	 * @throws IOException
	 */
	public static String getMD5(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		String md5 = DigestUtils.md5Hex(fis);
		fis.close();
		return md5;
	}
}
