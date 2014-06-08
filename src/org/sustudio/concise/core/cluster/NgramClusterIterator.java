package org.sustudio.concise.core.cluster;

import java.util.StringTokenizer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.sustudio.concise.core.Config;
import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.Workspace.INDEX;
import org.sustudio.concise.core.collocation.ConciseTokenAnalyzer;
import org.sustudio.concise.core.corpus.importer.ConciseField;

public class NgramClusterIterator extends ClusterIterator {

	private IndexReader ireader;
	private TermsEnum termsEnum;
	private final Analyzer analyzer;
	
	public NgramClusterIterator(final Workspace workspace, final int n, boolean showPartOfSpeech) throws Exception {
		super(workspace);
		analyzer = new ShingleAnalyzerWrapper(new ConciseTokenAnalyzer(Config.LUCENE_VERSION, showPartOfSpeech),
											  n,
											  n,
											  ShingleFilter.DEFAULT_TOKEN_SEPARATOR,
											  false,
											  false,
											  ShingleFilter.DEFAULT_FILLER_TOKEN);
		
		TemporaryClusterIndexer ci = new TemporaryClusterIndexer(temporaryDirectory);
		
		for (int i = 0; i < workspace.getIndexReader(INDEX.DOCUMENT).maxDoc(); i++) {
			Document doc = workspace.getIndexReader(INDEX.DOCUMENT).document(i);
			if (doc == null) continue;
			
			TokenStream tokenStream = doc.getField(ConciseField.CONTENT.field()).tokenStream(analyzer);
			CharTermAttribute termAttr = tokenStream.addAttribute(CharTermAttribute.class);
			
			tokenStream.reset();
			while (tokenStream.incrementToken()) {
				// 遇到有跳字的時候總會出現 _ (under score) 符號（這個是 ShingleFilter.DEFAULT_FILTER_TOKEN 設定的）
				if (ngramContainsStopWords(termAttr.toString())) {
					continue;
				}
				ci.addDocumentWithString(termAttr.toString());
			}
			tokenStream.end();
			tokenStream.close();
			ci.commit();
		}
		ci.closeWriter();
		ci = null;
		
		// dump temporary 
		ireader = DirectoryReader.open(temporaryDirectory);
		Terms terms = MultiFields.getTerms(ireader, TemporaryClusterIndexer.FIELD);
		if (terms != null) {
			termsEnum = terms.iterator(null);
			nextCluster = readNextCluster();
		}
	}
	
	private boolean ngramContainsStopWords(final String ngram) {
		final StringTokenizer st = new StringTokenizer(ngram, ShingleFilter.DEFAULT_TOKEN_SEPARATOR);
		while (st.hasMoreTokens()) {
			final String wordToTest = st.nextToken();
			if (wordToTest.equals(ShingleFilter.DEFAULT_FILLER_TOKEN)) {
				return true;
			}
		}
		return false;
	}
	
	private Cluster readNextCluster() throws Exception {
		while (termsEnum.next() != null) {
			return new Cluster(termsEnum.term().utf8ToString(), termsEnum.docFreq());
		}
		termsEnum = null;
		ireader.close();
		temporaryDirectory.close();
		return null;
	}
	
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
