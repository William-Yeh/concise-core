package org.sustudio.concise.core.cluster;

import java.util.StringTokenizer;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.ScoreDoc;
import org.sustudio.concise.core.collocation.TokenSentenceHighlighter;
import org.sustudio.concise.core.concordance.Conc;

/**
 * 以 concordance 為基礎的 cluster，使用時需自 {@link Conc}設定左右跨距
 * 
 * @author Kuan-ming
 * 
 */
public class ConcClusterIterator extends ClusterIterator {
	
	private IndexReader reader;
	private TermsEnum termsEnum;
	
	public ConcClusterIterator(Conc conc) throws Exception 
	{	
		super(conc.workspace);
		TemporaryClusterIndexer ci = new TemporaryClusterIndexer(temporaryDirectory);
		for (ScoreDoc doc : conc.hitDocs()) {
			
			TokenSentenceHighlighter highlighter = new TokenSentenceHighlighter(conc, doc.doc);
			for (String sentence : highlighter.getHighlightSpans()) {
				StringBuilder sb = new StringBuilder();
				int nodeStart = sentence.indexOf(Conc.preNodeTag);
				int nodeEnd = sentence.indexOf(Conc.postNodeTag);
				sb.append(sentence.substring(0, nodeStart));
				sb.append(sentence.substring(nodeStart + Conc.preNodeTag.length(), nodeEnd));
				sb.append(sentence.substring(nodeEnd + Conc.postNodeTag.length(), sentence.length()));
				
				StringTokenizer st = new StringTokenizer(sb.toString(), " ");
				if (st.countTokens() == conc.left_span_size + conc.right_span_size + 1) {
					ci.addDocumentWithString(sb.toString().trim());
				}
				sb.setLength(0);
			}
			ci.commit();
		}
		ci.closeWriter();
		ci = null;
		
		reader = DirectoryReader.open(temporaryDirectory);
		Terms terms = MultiFields.getTerms(reader, TemporaryClusterIndexer.FIELD);
		if (terms != null) {
			termsEnum = terms.iterator(null);
			nextCluster = readNextCluster();
		}
	}
	
	private Cluster readNextCluster() throws Exception {
		while (termsEnum.next() != null) {
			return new Cluster(termsEnum.term().utf8ToString(), termsEnum.docFreq());
		}
		termsEnum = null;
		reader.close();
		temporaryDirectory.close();
		return null;
	}
	
	@Override
	public Cluster next() {
		Cluster cluster = nextCluster;
		try {
			nextCluster = readNextCluster();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cluster;
	}
	
}
