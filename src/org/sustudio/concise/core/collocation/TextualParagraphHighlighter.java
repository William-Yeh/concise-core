package org.sustudio.concise.core.collocation;

import org.apache.lucene.search.vectorhighlight.FragmentsBuilder;
import org.sustudio.concise.core.collocation.TextualParagraphFragmentsBuilder.SHOW;
import org.sustudio.concise.core.concordance.Conc;


/**
 * 應用 {@link TextualSentenceFragmentsBuilder} 建立起來的句子做的 Highlighter，
 * 並根據 {@link ConciseTokenAnalyzer} 把裡頭的字詞換成 Token。
 * 
 * @author Kuan-ming Su
 */
public class TextualParagraphHighlighter extends TextualHighlighter {

	public TextualParagraphHighlighter(Conc conc, int doc) {
		super(conc, doc);
	}

	@Override
	public FragmentsBuilder getHighlightFragmentsBuilder() {
		return new TextualParagraphFragmentsBuilder(SHOW.HIGHLIGH_SENTENCES);
	}

	@Override
	public FragmentsBuilder getAllTextsWithHighlightFragmentsBuilder() {
		return new TextualParagraphFragmentsBuilder(SHOW.ALL_SENTENCES);
	}
}
