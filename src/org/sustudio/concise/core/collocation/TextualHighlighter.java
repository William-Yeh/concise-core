package org.sustudio.concise.core.collocation;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.vectorhighlight.FragmentsBuilder;
import org.apache.lucene.search.vectorhighlight.SingleFragListBuilder;
import org.sustudio.concise.core.Config;
import org.sustudio.concise.core.concordance.Conc;
import org.sustudio.concise.core.concordance.ConcHighlighter;
import org.sustudio.concise.core.corpus.importer.ConciseField;

/**
 * 依據 {@link #getHighlightFragmentsBuilder()} 和 {@link #getAllTextsWithHighlightFragmentsBuilder()} 建出文本範圍，
 * 並根據 {@link ConciseTokenAnalyzer} 把裡頭的字詞換成 Token。
 * 
 * @author Kuan-ming Su
 */
public abstract class TextualHighlighter extends ConcHighlighter {

	public TextualHighlighter(Conc conc, int doc) {
		super(conc, doc);
	}
	
	/**
	 * 傳回highlight的結果，以 {@link #getHighlightFragmentsBuilder()} 設定的邊界為準。
	 * 當文本（如句子）裡面含有兩個以上的 node 時，例如「真的 真的 好棒」去找「真的」，只會傳回一個文本（句子）。 <br />
	 * 這點和 {@link ConcHighlighter} 不同。
	 * 
	 * @return
	 * @throws IOException
	 */
	public String[] getHighlightTexts() throws IOException {
		return getBestFragments(
						getFieldQuery(query),
						workspace.getIndexReader(),
						docID,
						ConciseField.CONTENT.field(),
						1,
						Integer.MAX_VALUE,  // this field is being hacked
						new SingleFragListBuilder(),
						getHighlightFragmentsBuilder(),
						new String[] { Conc.preNodeTag },
						new String[] { Conc.postNodeTag },
						getEncoder());
	}
	
	public String[] getAllTextsWithHighlight() throws IOException {
		return getBestFragments(
						getFieldQuery(query),
						workspace.getIndexReader(),
						docID,
						ConciseField.CONTENT.field(),
						1,
						Integer.MAX_VALUE,  // this field is being hacked
						new SingleFragListBuilder(),
						getAllTextsWithHighlightFragmentsBuilder(),
						new String[] { Conc.preNodeTag },
						new String[] { Conc.postNodeTag },
						getEncoder());
	}
	
	@Override
	public Analyzer getAnalyzer() {
		return new ConciseTokenAnalyzer(Config.LUCENE_VERSION, conc.showPartOfSpeech);
	}
	
	
	/**
	 * 僅有 highlight 字眼文本的 FragemntsBuilder
	 * @return 僅有 highlight 字眼文本的 FragmentsBuilder
	 */
	public abstract FragmentsBuilder getHighlightFragmentsBuilder();
	
	/**
	 * 所有文本的 FragmentsBuilder
	 * @return 所有文本的 FragmentsBuilder
	 */
	public abstract FragmentsBuilder getAllTextsWithHighlightFragmentsBuilder();
}
