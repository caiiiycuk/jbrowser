package ru.atomation.utils.sevenzip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;

import ru.atomation.utils.sevenzip.compression.lzma.Decoder;

/**
 * Пакет для работы с lzma архивами //
 * Package for work with lzma archives
 * @author caiiiycuk
 */
public class LZMAUtils {
	
	/**
	 * Распаковать lzma архив // Unpack lzma archive
	 * @param src файл архива // archive file
	 * @param dst директория распаковки // directory to extract
	 * @throws IOException
	 */
	public static void unpack(File src, File dst) throws IOException {
		if (!src.isFile() || dst.isFile())
			return;
		if (!dst.exists())
			dst.mkdir();
		unpack(new BufferedInputStream(new FileInputStream(src)), dst);
	}

	/**
	 * Распаковать lzma архив // Unpack lzma archive
	 * @param src архива // archive
	 * @param dst директория распаковки // directory to extract
	 * @throws IOException
	 */
	public static void unpack(InputStream src, File dst) throws IOException {
		src = new BufferedInputStream(src);
		File buffFile = new File(dst, "temp-file.tar");
		OutputStream buffer = new BufferedOutputStream(new FileOutputStream(
				buffFile));
		TarArchiveInputStream inTar = null;
		try {
			int propertiesSize = 5;
			byte[] properties = new byte[propertiesSize];
			if (src.read(properties, 0, propertiesSize) != propertiesSize)
				throw new RuntimeException("input .lzma file is too short");
			Decoder decoder = new Decoder();
			if (!decoder.SetDecoderProperties(properties))
				throw new RuntimeException("Incorrect stream properties");
			long outSize = 0;
			for (int i = 0; i < 8; i++) {
				int v = src.read();
				if (v < 0)
					throw new RuntimeException("Can't read stream size");
				outSize |= ((long) v) << (8 * i);
			}
			if (!decoder.Code(src, buffer, outSize))
				throw new RuntimeException("Error in data stream");

			buffer.flush();
			IOUtils.closeQuietly(src);
			IOUtils.closeQuietly(buffer);

			inTar = new TarArchiveInputStream(new FileInputStream(buffFile));
			TarArchiveEntry entry = null;
			while ((entry = inTar.getNextTarEntry()) != null) {
				File newFile = new File(dst, entry.getName());
				if (entry.isDirectory())
					newFile.mkdir();
				else {
					OutputStream output = null;
					try {
						output = new BufferedOutputStream(new FileOutputStream(
								newFile));
						IOUtils.copy(inTar, output);
					} finally {
						IOUtils.closeQuietly(output);
					}
				}
			}
		} finally {
			IOUtils.closeQuietly(src);
			IOUtils.closeQuietly(buffer);
			IOUtils.closeQuietly(inTar);
			
			//same as: FileUtils.deleteQuietly(buffFile);
			try {
				buffFile.delete();
			} catch (Exception e) { 
				//ignoring
			}
		}
	}
}
