package org.sustudio.concise.core.concordance;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.sustudio.concise.core.Config;
import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.corpus.importer.ConciseField;

/**
 * 找出 search words 出現在哪些 document 裡頭
 * 
 * @author Kuan-ming
 *
 */
public class Conc {
	
	public static final int DEFAULT_LEFT_SPAN = 10;
	public static final int DEFAULT_RIGHT_SPAN = 10;
	
	public static final String preNodeTag = "<node>";
	public static final String postNodeTag = "</node>";
	
	public final Workspace workspace;
	public final String queryStr;
	public final boolean showPartOfSpeech;
	public final IndexReader reader;
	public final IndexSearcher searcher;
	public Query query;
	
	public int left_span_size = DEFAULT_LEFT_SPAN;
	public int right_span_size = DEFAULT_RIGHT_SPAN;
	
	public Conc(Workspace workspace, final String queryStr, boolean showPartOfSpeech) throws ParseException, IOException {
		
		this.workspace = workspace;
		this.queryStr = queryStr;
		this.showPartOfSpeech = showPartOfSpeech;
		
		reader = workspace.getIndexReader();
		searcher = new IndexSearcher(reader);
		
		QueryParser parser = new QueryParser(Config.LUCENE_VERSION, 
				 							 ConciseField.CONTENT.field(),
				 							 new ConciseQueryAnalyzer());
		parser.setMultiTermRewriteMethod(MultiTermQuery.SCORING_BOOLEAN_QUERY_REWRITE);
		parser.setAllowLeadingWildcard(true);
		
		query = parser.parse(queryStr);
		query = searcher.rewrite(query);
	}
	
	/**
	 * 設定左右跨距（單位是詞）
	 * @param left
	 * @param right
	 */
	public void setSpanSize(int left, int right) {
		this.left_span_size = left;
		this.right_span_size = right;
	}
	
	/**
	 * 傳回 Query 物件
	 * @return
	 */
	public Query getQuery() {
		return query;
	}
	
	public ScoreDoc[] hitDocs() throws Exception {
		AllDocsCollector collector = new AllDocsCollector();
		searcher.search(query, collector);
		
		return collector.allDocs();
	}
	
	/**
	 * 傳回搜尋的詞
	 * @return
	 */
	public Set<String> getSearchWords() {
		HashSet<Term> terms = new HashSet<Term>();
		query.extractTerms(terms);
		HashSet<String> words = new HashSet<String>();
		for (Term term : terms) {
			words.add(term.bytes().utf8ToString());
		}
		return words;
	}
	
}
