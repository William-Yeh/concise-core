package org.sustudio.concise.core.collocation;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.util.Version;
import org.sustudio.concise.core.CCPrefs;
import org.sustudio.concise.core.Config;
import org.sustudio.concise.core.concordance.PartOfSpeechFilter;
import org.sustudio.concise.core.concordance.PartOfSpeechSeparatorFilter;
import org.sustudio.concise.core.concordance.TermLowerCaseFilter;


/**
 * 把字詞都轉成 ConciseToken <br />
 * 用了 {@link WhitespaceTokenizer}，然後做 {@link PartOfSpeechFilter}、{@link ConciseTokenFilter}
 * ，並且依據 {@link CCPrefs} 裡的設定，看要不要對 term 做 lowercase <br />
 * 
 * @author Kuan-ming Su
 * 
 */
public class ConciseTokenAnalyzer extends Analyzer {
	
	public final boolean showPartOfSpeech;
	
	private final Version matchVersion;
	
	public ConciseTokenAnalyzer(Version matchVersion, boolean showPartOfSpeech) {
		this.matchVersion = matchVersion;
		this.showPartOfSpeech = showPartOfSpeech;
	}
	
	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {

		final Tokenizer source = new WhitespaceTokenizer(matchVersion, reader);
		
		TokenStream result = new PartOfSpeechFilter(source, showPartOfSpeech);
		result = new ConciseTokenFilter(result);
		if (CCPrefs.LOWERCASE_ENABLED) {
			result = new TermLowerCaseFilter(matchVersion, result);
		}
		if (!CCPrefs.POS_SEPARATOR.equals(Config.SYSTEM_POS_SEPERATOR)) {
			result = new PartOfSpeechSeparatorFilter(result);
		}
		if (CCPrefs.LEMMA_ENABLED) {
			result = new LemmaFilter(result);
		}
		
		return new TokenStreamComponents(source, result);
	}
	
}
