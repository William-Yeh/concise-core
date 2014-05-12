package org.sustudio.concise.core.wordlister;

import java.io.IOException;

import org.apache.lucene.index.FilteredTermsEnum;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;
import org.sustudio.concise.core.CCPrefs;
import org.sustudio.concise.core.Config;

/**
 * 由 {@link WordFilter} 來獲得過濾後的 {@link TermsEnum}. 如果使用 Lemma 的話，則過濾掉 form
 * 
 * @author Kuan-ming Su
 *
 */
public class ConciseTermsEnum extends FilteredTermsEnum {

	private final WordFilter filter = new WordFilter();
	private final boolean showPartOfSpeech;
	private final boolean lemmatize;
	
	public ConciseTermsEnum(TermsEnum tenum, boolean showPartOfSpeech, boolean lemmatize) throws IOException {
		super(tenum);
		this.showPartOfSpeech = showPartOfSpeech;
		this.lemmatize = lemmatize;
		setInitialSeekTerm(tenum.next());
	}

	@Override
	protected AcceptStatus accept(BytesRef term) throws IOException {
		String word = term.utf8ToString();
		if (lemmatize && CCPrefs.LEMMA_LIST != null) {
			String encodedWord = CustomPartOfSpeechEncoder.encode(word);
			for (Lemma lemma : CCPrefs.LEMMA_LIST) {
				if (lemma.containsForm(encodedWord))
					return AcceptStatus.NO;
			}
		}
		
		if (showPartOfSpeech && 
			word.contains(Config.SYSTEM_POS_SEPERATOR) &&
			filter.accept(word))
		{
			return AcceptStatus.YES;
		}
		if (!showPartOfSpeech && 
			!word.contains(Config.SYSTEM_POS_SEPERATOR) &&
			filter.accept(word))
		{
			return AcceptStatus.YES;
		}
		return AcceptStatus.NO;
	}

}
