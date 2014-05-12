package org.sustudio.concise.core.concordance;

import java.io.IOException;
import java.io.Reader;
import java.util.StringTokenizer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.sustudio.concise.core.CCPrefs;
import org.sustudio.concise.core.Config;
import org.sustudio.concise.core.highlighter.Highlighter;
import org.sustudio.concise.core.wordlister.Lemma;

/**
 * 
 * <p>concordance 專用的 highlighter.<br />
 * 不更動文字內容，只靠 {@link Conc} 判斷要不要顯示 part-of-speech。</p>
 * <p>但是不處理 Lemma。</p>
 * 
 * @author Kuan-ming Su
 *
 */
public class ConcHighlighter extends Highlighter {

	protected final Conc conc;
	
	/**
	 * constructor
	 * @param conc
	 * @param docID Lucene的文件編號
	 */
	public ConcHighlighter(Conc conc, int docID) {
		super(conc.workspace, conc.query, docID);
		this.conc = conc;
	}
	
	/**
	 * 傳回指定跨距內的東西
	 * @return
	 * @throws IOException
	 */
	public String[] getHighlightSpans() throws IOException {
		String[] s = getHighlightTexts();
		for (int i = 0; i < s.length; i++) {
			int start = s[i].indexOf(Conc.preNodeTag);
			int end = s[i].indexOf(Conc.postNodeTag);
			String node = s[i].substring(start + Conc.preNodeTag.length(), end).trim();
			String left = getLeftSpan(s[i], start);
			String right = getRightSpan(s[i], end);
			s[i] = Conc.preNodeTag + node + Conc.postNodeTag;
			if (!left.isEmpty())
				s[i] = left + " " + s[i];
			if (!right.isEmpty()) 
				s[i] += " " + right;
		}
		return s;
	}
	
	@Override
	public Analyzer getAnalyzer() {
		return new Analyzer() {

			@Override
			protected TokenStreamComponents createComponents(String fieldName,
					Reader reader) {
				
				Tokenizer tokenizer = new LineAndWhitespaceTokenizer(Config.LUCENE_VERSION, reader);
				TokenStream result = new PartOfSpeechFilter(tokenizer, conc.showPartOfSpeech);
				if (CCPrefs.LEMMA_ENABLED && CCPrefs.LEMMA_LIST != null) {
					// show lemma in [ word ] 
					result = new TokenFilter(result) {

						CharTermAttribute termAttr = addAttribute(CharTermAttribute.class);
						
						@Override
						public boolean incrementToken() throws IOException {
							if (input.incrementToken()) {
								Lemma lemma = CCPrefs.LEMMA_LIST.getLemmaWithForm(termAttr.toString());
								if (lemma != null) {
									termAttr.append("[" + lemma.getWord() + "]");
								}
								return true;
							}
							return false;
						}
						
					};
				}
				if (CCPrefs.LOWERCASE_ENABLED) {
					result = new TermLowerCaseFilter(Config.LUCENE_VERSION, result);
				}
				if (!CCPrefs.POS_SEPARATOR.equals(Config.SYSTEM_POS_SEPERATOR)) {
					// apply custom POS separator
					result = new PartOfSpeechSeparatorFilter(result);
				}
				return new TokenStreamComponents(tokenizer, result);
			}
			
		};
	}
	
	
	/**
	 * 處理左跨距用的
	 * @param buffer
	 * @param offset
	 * @return
	 */
	protected String getLeftSpan(String buffer, int offset) {
		if (conc.left_span_size == 0) return "";
		int spaceCount = 0;
		int startOffset = 0;
		for (int i=offset; i>-1; i--) {
			if (buffer.charAt(i) == ' ') {
				// 一開使是空格的要扣掉
				if (i == offset-1) continue;
				
				spaceCount++;
				if (spaceCount == conc.left_span_size) {
					startOffset = i;
					break;
				}
			}
		}
		return removeNodeTagAndLineBreaker(buffer.substring(startOffset, offset));
	}
	
	/**
	 * 處理右跨距用的
	 * @param buffer
	 * @param offset
	 * @return
	 */
	protected String getRightSpan(String buffer, int offset) {
		if (conc.right_span_size == 0) return "";
		int count = 0;
		StringBuilder sb = new StringBuilder();
		StringTokenizer st = new StringTokenizer(buffer.substring(offset, buffer.length()), " \n");
		while (st.hasMoreTokens()) {
			sb.append(st.nextToken() + " ");
			count++;
			if (count == conc.right_span_size) {
				break;
			}
		}
		return removeNodeTagAndLineBreaker(sb.toString());
	}
	
	protected String removeNodeTagAndLineBreaker(String str) {
		return str.replace(Conc.preNodeTag, "")
				.replace(Conc.postNodeTag, "")
				.replace("\n", "")
				.trim();
	}
	
}
