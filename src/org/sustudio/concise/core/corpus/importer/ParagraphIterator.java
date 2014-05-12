package org.sustudio.concise.core.corpus.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * iterate paragraph (\n)
 * 
 * @author Kuan-ming Su
 *
 */
public class ParagraphIterator implements Iterator<String>, Iterable<String> {
	
	private final BufferedReader br;
	private String nextPara;
	private boolean hasNext = false;
	
	/**
	 * 
	 * @param txtFile plain text file or gzipped plain text file
	 * @param charset 
	 * @throws IOException
	 */
	public ParagraphIterator(File txtFile, Charset charset) throws IOException {
		this(new InputStreamReader(
				GZipUtil.decompressStream(
						new FileInputStream(txtFile)), 
				charset));
	}
	
	public ParagraphIterator(String str) throws IOException {
		this(new StringReader(str));
	}
	
	public ParagraphIterator(Reader reader) throws IOException {
		br = new BufferedReader(reader);
		readNext();
	}
	
	void readNext() throws IOException {
		String para = null;
		while ((para = br.readLine()) != null) {
			if (!para.trim().isEmpty()) {
				break;
			}
		}
		nextPara = para;
		hasNext = nextPara != null;
	}
		
	public boolean hasMoreParagraphs() {
		return hasNext;
	}

	public String nextParagraph() throws IOException {
		String para = nextPara;
		readNext();
		return para;
	}
	
	public void close() throws IOException {
		br.close();
	}
	
	
	/**
	 * Same as {@link #nextParagraph()}
	 */
	public String next() {
		try {
			return nextParagraph();
		} catch (Exception e) {}
		return null;
	}
	
	
	/**
	 * Same as {@link #hasMoreParagraphs()}
	 */
	public boolean hasNext() {
		return hasMoreParagraphs();
	}
	
	
	/**
	 * Do nothing
	 */
	public void remove() {
		// Do nothing
	}
	
	
	@Override
	public Iterator<String> iterator() {
		return this;
	}
	
}
