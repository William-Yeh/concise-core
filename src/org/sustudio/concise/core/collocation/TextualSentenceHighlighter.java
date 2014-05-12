package org.sustudio.concise.core.collocation;

import org.apache.lucene.search.vectorhighlight.FragmentsBuilder;
import org.sustudio.concise.core.collocation.TextualSentenceFragmentsBuilder.SHOW;
import org.sustudio.concise.core.concordance.Conc;

/**
 * 應用 {@link TextualSentenceFragmentsBuilder} 建立起來的句子做的 Highlighter，
 * 並根據 {@link ConciseTokenAnalyzer} 把裡頭的字詞換成 Token。
 * 
 * @author Kuan-ming Su
 */
public class TextualSentenceHighlighter extends TextualHighlighter {

	public TextualSentenceHighlighter(Conc conc, int doc) {
		super(conc, doc);
	}

	@Override
	public FragmentsBuilder getHighlightFragmentsBuilder() {
		return new TextualSentenceFragmentsBuilder(SHOW.HIGHLIGH_SENTENCES);
	}

	@Override
	public FragmentsBuilder getAllTextsWithHighlightFragmentsBuilder() {
		return new TextualSentenceFragmentsBuilder(SHOW.ALL_SENTENCES);
	}
}
