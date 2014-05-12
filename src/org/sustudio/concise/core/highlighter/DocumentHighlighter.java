package org.sustudio.concise.core.highlighter;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.vectorhighlight.BaseFragmentsBuilder;
import org.sustudio.concise.core.CCPrefs;
import org.sustudio.concise.core.Config;
import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.concordance.LineAndWhitespaceTokenizer;
import org.sustudio.concise.core.concordance.PartOfSpeechFilter;
import org.sustudio.concise.core.concordance.PartOfSpeechSeparatorFilter;
import org.sustudio.concise.core.corpus.importer.ConciseField;

public class DocumentHighlighter extends Highlighter {
	
	public static String highlight(Workspace workspace, 
								   int docID, 
								   String queryStr, 
								   boolean showPartOfSpeech) throws Exception 
	{
		return highlight(workspace,
						 docID,
						 queryStr,
						 BaseFragmentsBuilder.COLORED_PRE_TAGS,
						 BaseFragmentsBuilder.COLORED_POST_TAGS,
						 showPartOfSpeech);
	}
	
	public static String highlight(Workspace workspace, 
								   int docID, 
								   String queryStr,
								   String[] preTags, 
								   String[] postTags, 
								   boolean showPartOfSpeech) throws IOException, ParseException
	{
		QueryParser parser = new QueryParser(Config.LUCENE_VERSION, 
											 ConciseField.CONTENT.field(), 
											 new WhitespaceAnalyzer(Config.LUCENE_VERSION)); 
		parser.setMultiTermRewriteMethod(MultiTermQuery.SCORING_BOOLEAN_QUERY_REWRITE);
		parser.setAllowLeadingWildcard(true);
		
		Query query = parser.parse(queryStr);
		query = query.rewrite(workspace.getIndexReader());
		
		DocumentHighlighter highlighter = new DocumentHighlighter(
														workspace, 
														query, 
														docID,
														preTags,
														postTags,
														showPartOfSpeech);
		return highlighter.getHighlightText();
	}
	
	protected final String[] preTags;
	protected final String[] postTags;
	protected final boolean showPartOfSpeech;
	
	public DocumentHighlighter(Workspace workspace, Query query, int docID, String[] preTags, String[] postTags, boolean showPartOfSpeech) {
		super(workspace, query, docID);
		this.preTags = preTags;
		this.postTags = postTags;
		this.showPartOfSpeech = showPartOfSpeech;
	}
	
	@Override
	public String[] getPreTags() {
		return preTags;
	}
	
	@Override
	public String[] getPostTags() {
		return postTags;
	}
	
	
	public Analyzer getAnalyzer() {
		return new Analyzer() {

			@Override
			protected TokenStreamComponents createComponents(String fieldName,
					Reader reader) {
				
				Tokenizer tokenizer = new LineAndWhitespaceTokenizer(Config.LUCENE_VERSION, reader);
				TokenStream result = new PartOfSpeechFilter(tokenizer, showPartOfSpeech);
				if (!CCPrefs.POS_SEPARATOR.equals(Config.SYSTEM_POS_SEPERATOR)) {
					result = new PartOfSpeechSeparatorFilter(result);
				}
				return new TokenStreamComponents(tokenizer, result);
			}
			
		};
	}
}
