package org.sustudio.concise.core.corpus.importer;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.zip.GZIPInputStream;

public class GZipUtil {

	/**
	 * Checks the first two bytes to determine if the InputStream is gzipped. 
	 * @param input		InputStream to decompress.
	 * @return			GZIPInputStream or InputStream
	 */
	public static InputStream decompressStream(InputStream input) throws IOException {
		PushbackInputStream pb = new PushbackInputStream(input, 2);	//we need a pushbackstream to look ahead
		byte[] signature = new byte[2];
		pb.read(signature);	// read the signature
		pb.unread(signature);	// push back the signature to the stream
	
		//check if matches standard gzip magic number
		//	int head = ((int) signature[0] & 0xff) | ((signature[1] << 8) & 0xff00);
		//	if (GZIPInputStream.GZIP_MAGIC == head)
		
		if( signature[ 0 ] == (byte) 0x1f && signature[ 1 ] == (byte) 0x8b )
			return new GZIPInputStream(pb);
		else
			return pb;
	}
}
