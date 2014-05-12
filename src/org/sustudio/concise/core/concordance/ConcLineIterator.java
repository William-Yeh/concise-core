package org.sustudio.concise.core.concordance;

import java.util.Iterator;

import org.apache.lucene.search.ScoreDoc;
import org.sustudio.concise.core.Config;
import org.sustudio.concise.core.corpus.importer.ConciseField;

public class ConcLineIterator implements Iterator<ConcLine>, Iterable<ConcLine> {
	
	private ConcLine nextLine;
	
	private final ConcHighlighter highlighter;
	private final Conc conc;
	private final int docId;
	private final StringBuilder sb;
	
	private int nodeEndOffset;
	private int nodeOffset;
	
	private int wordId = 0;
	
	public ConcLineIterator(Conc conc, ScoreDoc scoreDoc) throws Exception {
		this.conc = conc;
		this.docId = scoreDoc.doc;
		this.highlighter = new ConcHighlighter(conc, docId);
		
		String text = highlighter.getHighlightText();
		if (text == null) {
			sb = null;
		}
		else {
			sb = new StringBuilder(text);
			text = new String();
			text = null;
			nodeEndOffset = 0 - Conc.postNodeTag.length();
			
			nextLine = readNext();
		}
	}
	
	private ConcLine readNext() {
		if ( (nodeOffset = sb.indexOf(Conc.preNodeTag, nodeEndOffset)) != -1) 
		{
			nodeEndOffset = sb.indexOf(Conc.postNodeTag, nodeOffset);
			String node = sb.substring(nodeOffset + Conc.preNodeTag.length(), nodeEndOffset).trim();
			nodeEndOffset += Conc.postNodeTag.length();
			if (nodeEndOffset + Config.SYSTEM_POS_SEPERATOR.length() < sb.length()
				&& sb.substring(nodeEndOffset,  nodeEndOffset + Config.SYSTEM_POS_SEPERATOR.length())
					.equals(Config.SYSTEM_POS_SEPERATOR)) 
			{
				int end = sb.indexOf(" ", nodeEndOffset);
				if (end == -1)
					end = sb.length();
				node += sb.substring(nodeEndOffset, end);
				nodeEndOffset = end;
			}
			
			wordId++;
			
			ConcLine line = new ConcLine();
			line.setDocId(docId);
			line.setNode(node);
			line.setLeft(highlighter.getLeftSpan(sb.toString(), nodeOffset));
			line.setRight(highlighter.getRightSpan(sb.toString(), nodeEndOffset));
			line.setWordId(wordId);
			
			// read title and filepath of the Doc
			try {
				line.setDocTitle(conc.searcher.doc(docId).get(ConciseField.TITLE.field()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return line;
		}
		return null;
	}
	
	@Override
	public boolean hasNext() {
		return nextLine != null;
	}

	@Override
	public ConcLine next() {
		ConcLine concLine = nextLine;
		nextLine = readNext();
		return concLine;
	}

	@Override
	public void remove() {
		// Do nothing
	}

	@Override
	public Iterator<ConcLine> iterator() {
		return this;
	}
	
}
