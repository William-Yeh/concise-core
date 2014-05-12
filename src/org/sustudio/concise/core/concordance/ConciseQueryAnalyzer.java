package org.sustudio.concise.core.concordance;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.sustudio.concise.core.CCPrefs;
import org.sustudio.concise.core.Config;
import org.sustudio.concise.core.wordlister.CustomPartOfSpeechDecoder;
import org.sustudio.concise.core.wordlister.Lemma;

/**
 * 把搜尋的字轉成系統的格式. 把詞性標籤改過去，然後還要列舉 lemma
 * 
 * @author Kuan-ming Su
 *
 */
public class ConciseQueryAnalyzer extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(String fieldName,
			Reader reader) {
		
		Tokenizer source = new WhitespaceTokenizer(Config.LUCENE_VERSION, reader);
		TokenStream result = new TokenFilter(source) {

			CharTermAttribute termAttr = addAttribute(CharTermAttribute.class);
			LinkedList<String> bufferedWords = new LinkedList<String>();
			
			@Override
			public boolean incrementToken() throws IOException {
				
				while (bufferedWords.size() > 0) {
					// create new termAttr
					termAttr.setEmpty();
					termAttr.append(bufferedWords.remove());
					return true;
				}
				
				if (input.incrementToken()) {
					
					String word = termAttr.toString();
					
					// handle lemma search
					if (CCPrefs.LEMMA_ENABLED && CCPrefs.LEMMA_LIST != null) {
						Lemma lemma = CCPrefs.LEMMA_LIST.get(word);
						if (lemma != null) {
							bufferedWords.addAll(lemma.getForms());
							return true;
						}
					}
					
					// decode part-of-speech
					if (!CCPrefs.POS_SEPARATOR.equals(Config.SYSTEM_POS_SEPERATOR)) {
						termAttr.setEmpty();
						word = CustomPartOfSpeechDecoder.decode(word);
						termAttr.append(word);
					}
					return true;
				}
				return false;
			}
			
		};
		return new TokenStreamComponents(source, result);
	}

}
