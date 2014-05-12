package org.sustudio.concise.core.concordance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;

/**
 * 收集所有的文件
 * 
 * @author Kuan-ming Su
 *
 */
public class AllDocsCollector extends Collector {

	private List<ScoreDoc> docs = new ArrayList<ScoreDoc>();
	private Scorer scorer;
	private int docBase;
	
	public boolean acceptsDocsOutOfOrder() {
		return false;
	}
	
	public void collect(int doc) throws IOException {
		docs.add(new ScoreDoc(doc + docBase, scorer.score()));
	}

	public void setNextReader(AtomicReaderContext context) throws IOException {
		docBase = context.docBase;
	}

	public void setScorer(Scorer scorer) throws IOException {
		this.scorer = scorer;
	}
	
	public ScoreDoc[] allDocs() {
		return docs.toArray(new ScoreDoc[0]);
	}
	
	public int getTotalHits() {
		return docs.size();
	}
	
}
