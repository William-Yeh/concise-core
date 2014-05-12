package org.sustudio.concise.core.collocation;

import org.apache.lucene.analysis.Analyzer;
import org.sustudio.concise.core.Config;
import org.sustudio.concise.core.concordance.Conc;
import org.sustudio.concise.core.concordance.ConcHighlighter;
import org.sustudio.concise.core.concordance.NodeSentenceFragmentsBuilder;

/**
 * 應用 {@link NodeSentenceFragmentsBuilder} 建立起來的句子做的 Highlighter，
 * 會根據 {@link ConciseTokenAnalyzer} 把裡頭的字詞換成 Token。 應用在 surface collocation 的模式中。
 * 
 * @author Kuan-ming Su
 * 
 */
public class TokenSentenceHighlighter extends ConcHighlighter {

	public TokenSentenceHighlighter(Conc conc, int doc) {
		super(conc, doc);
	}
	
	@Override
	public Analyzer getAnalyzer() {
		return new ConciseTokenAnalyzer(Config.LUCENE_VERSION, conc.showPartOfSpeech);
	}
}
