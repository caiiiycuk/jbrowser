package ru.atomation.xulnative;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.atomation.utils.sevenzip.LZMAUtils;

/**
 * Распаковывает xulrunner в указанную директорию, если xullrunner присудствует в сборке
 * <P>
 * Class for unpack xulrunner to directory
 * @author caiiiycuk
 *
 */
public class XulExtractor {
	
    private static Log logger = LogFactory.getLog(XulExtractor.class);
	
	private final static String ARCHIVE = "xulrunner.tar.lzma";
	
	public XulExtractor() {}
	
	/**
	 * Извлечь xulrunner // Unpack xulrunner
	 * @param directory директория для распаковки // extract directory
	 * @return true если успешно // true on success
	 */
	public boolean extract(File directory) {
		if (!directory.exists() && !directory.mkdirs()) {
			logger.error("Directory doesn`t exsists [" + directory + "]");
			return false;
		}
		
		if (!directory.isDirectory()) {
			logger.error("Not a directory [" + directory + "]");
			return false;
		}
		
		InputStream archive = this.getClass().getResourceAsStream(ARCHIVE);

		if (archive == null) {
			logger.error("Xulrunner archive not found in jar");
			return false;
		}
		
		try {
			LZMAUtils.unpack(archive, directory);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return false;
		}
		
		return true;
	}

}
