package org.sustudio.concise.core.corpus.importer;

import java.io.File;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import com.chenlb.mmseg4j.ComplexSeg;
import com.chenlb.mmseg4j.Dictionary;
import com.chenlb.mmseg4j.MaxWordSeg;
import com.chenlb.mmseg4j.Seg;
import com.chenlb.mmseg4j.SimpleSeg;

/**
 * Analyzer implements MMSeg
 * 
 * @author Kuan-ming Su
 *
 */
public class MMSegAnalyzer extends Analyzer {

	public enum MMSeg { MaxWord, Complex, Simple; }
	
	private final MMSeg mmSeg;
	private final Seg seg;
	
	/**
	 * @see Dictionary#getInstance()
	 */
	public MMSegAnalyzer() {
		this(MMSeg.Complex, null);
	}
	
	public MMSegAnalyzer(MMSeg segEnum, File[] userDictFiles) {
		// default dictionaries
		Dictionary.clear(Dictionary.getDefalutPath());
		Dictionary.userDict = userDictFiles;
		Dictionary dic = Dictionary.getInstance();
		this.mmSeg = segEnum;
		switch (segEnum) {
			default:		
			case Complex:	seg = new ComplexSeg(dic);	break;
			case Simple:	seg = new SimpleSeg(dic);	break;
			case MaxWord:	seg = new MaxWordSeg(dic);	break;
		}
	}
	
	public File[] getUserDictFiles() {
		return Dictionary.userDict;
	}
	
	public MMSeg getSegEnum() {
		return mmSeg;
	}
	
	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
		return new TokenStreamComponents(new MMSegTokenizer(seg, reader));
	}
	
}
