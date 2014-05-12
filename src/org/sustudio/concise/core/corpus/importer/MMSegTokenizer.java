package org.sustudio.concise.core.corpus.importer;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import com.chenlb.mmseg4j.MMSeg;
import com.chenlb.mmseg4j.Seg;
import com.chenlb.mmseg4j.Word;

/**
 * Tokenizer implements MMSeg
 * 
 * @author Kuan-ming Su
 *
 */
public class MMSegTokenizer extends Tokenizer {

	private MMSeg mmSeg;
	
	private CharTermAttribute termAtt;
	private OffsetAttribute offsetAtt;
	private TypeAttribute typeAtt;
	
	public MMSegTokenizer(Seg seg, Reader input) {
		super(input);
		mmSeg = new MMSeg(input, seg);
		
		termAtt = (CharTermAttribute)addAttribute(CharTermAttribute.class);
		offsetAtt = (OffsetAttribute)addAttribute(OffsetAttribute.class);
		typeAtt = (TypeAttribute)addAttribute(TypeAttribute.class);
	}
	
	public void reset(Reader input) throws IOException {
		super.reset();
		mmSeg.reset(input);
	}
	
	//lucene 4
	// 參考 http://wxf4150.blog.163.com/blog/static/111380836201292911234802/
	// 解決不能存 index 的問題
	@Override
	public boolean incrementToken() throws IOException {
		clearAttributes();
		if (mmSeg.ReaderStatus==1) {
			mmSeg.reset(input);
			mmSeg.ReaderStatus = 0;
		}
		Word word = mmSeg.next();
		if(word != null) {
			termAtt.copyBuffer(word.getSen(), word.getWordOffset(), word.getLength());
			offsetAtt.setOffset(word.getStartOffset(), word.getEndOffset());
			typeAtt.setType(word.getType());
			return true;
		} else {
			end();
			mmSeg.ReaderStatus=1;
			return false;
		}
	}
}
