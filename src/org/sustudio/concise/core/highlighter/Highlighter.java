package org.sustudio.concise.core.highlighter;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Encoder;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;
import org.apache.lucene.search.vectorhighlight.SingleFragListBuilder;
import org.sustudio.concise.core.Config;
import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.Workspace.INDEX;
import org.sustudio.concise.core.concordance.Conc;
import org.sustudio.concise.core.concordance.LineAndWhitespaceTokenizer;
import org.sustudio.concise.core.concordance.NodeSentenceFragmentsBuilder;
import org.sustudio.concise.core.corpus.importer.ConciseField;

/**
 * 
 * 文件的highlighter，不更動文字內容
 * 
 * @author Kuan-ming Su
 *
 */
public class Highlighter extends FastVectorHighlighter {

	protected final Workspace workspace;
	protected final Query query;
	protected final int docID;
	
	/**
	 * constructor
	 * @param workspace
	 * @param query
	 * @param docID
	 */
	public Highlighter(Workspace workspace, Query query, int docID) {
		this.workspace = workspace;
		this.query = query;
		this.docID = docID;
	}
	
	/**
	 * 可以更動加載於前面的標簽，如加上顏色
	 * @return
	 */
	public String[] getPreTags() {
		return new String[] { Conc.preNodeTag };
	}
	
	/**
	 * 可以更動加載於後面的標簽
	 * @return
	 */
	public String[] getPostTags() {
		return new String[] { Conc.postNodeTag };
	}
	
	/**
	 * 傳回highlight的結果，也就是增加自定tag的結果。如果沒有任何 highlight 的話，則會傳回 null。
	 * @return
	 * @throws IOException
	 */
	public String getHighlightText() throws IOException {
		return getBestFragment(getFieldQuery(query),
				  			   workspace.getIndexReader(INDEX.DOCUMENT),
				  			   docID, 
				  			   ConciseField.CONTENT.field(),
				  			   1,
				  			   new SingleFragListBuilder(),
				  			   new DocumentFragmentsBuilder(),
				  			   getPreTags(),
				  			   getPostTags(),
				  			   getEncoder());
	}
	
	/**
	 * 傳回highlight的結果，以句子為邊界。每找到一個字，就會成為一句。<br />
	 * 但是有個問題，例如「真的 真的 好棒」去找「真的」，就會變成兩句。 <br />
	 * 會傳回 [&lt;node&gt;真的&lt;/node&gt; 真的 好棒] 和 [真的 &lt;node&gt;真的&lt;/node&gt; 好棒]
	 * 
	 * @return
	 * @throws IOException
	 */
	public String[] getHighlightTexts() throws IOException {
		return getBestFragments(
						getFieldQuery(query),
						workspace.getIndexReader(INDEX.DOCUMENT),
						docID,
						ConciseField.CONTENT.field(),
						1,
						Integer.MAX_VALUE,  // this field is being hacked
						new SingleFragListBuilder(),
						new NodeSentenceFragmentsBuilder(),
						new String[] { Conc.preNodeTag },
						new String[] { Conc.postNodeTag },
						getEncoder());
	}
	
	/**
	 * 可以自定內容的 Encoder
	 * @return
	 */
	public Encoder getEncoder() {
		return new Encoder() {

			@Override
			public String encodeText(String originalText) {
				StringBuilder text = new StringBuilder();
				try {
					Analyzer analyzer = getAnalyzer();
					TokenStream s = analyzer.tokenStream(null, originalText);
					CharTermAttribute t = s.addAttribute(CharTermAttribute.class);
					s.reset();
					while (s.incrementToken()) {
						text.append(t.toString() + " ");
					}
					s.close();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				return text.toString();
			}
		};
	}
	
	/**
	 * 如果 Encoder 也是用 Analyzer 為基礎的，可以只更動這邊的內容
	 * @return
	 */
	public Analyzer getAnalyzer() {
		return new Analyzer() {

			@Override
			protected TokenStreamComponents createComponents(String fieldName,
					Reader reader) {
				
				Tokenizer tokenizer = new LineAndWhitespaceTokenizer(Config.LUCENE_VERSION, reader);
				//TokenStream result = new PartOfSpeechFilter(tokenizer, conc.showPartOfSpeech);
				return new TokenStreamComponents(tokenizer, tokenizer);
			}
			
		};
	}
}
