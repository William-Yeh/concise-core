package org.sustudio.concise.core.wordlister;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;
import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.corpus.ConciseDocument;
import org.sustudio.concise.core.corpus.importer.ConciseField;

public class WordUtils {

	/**
	 * 傳回該詞在每一個文件中的出現頻率
	 * @param workspace
	 * @param word
	 * @param docs
	 * @return
	 * @throws Exception
	 */
	public static Map<ConciseDocument, Integer> wordFreqByDocs(Workspace workspace, String word, List<ConciseDocument> docs) throws Exception {
		Map<ConciseDocument, Integer> map = new HashMap<ConciseDocument, Integer>();
		Terms terms = MultiFields.getTerms(workspace.getIndexReader(), ConciseField.CONTENT.field());
		TermsEnum te = terms.iterator(null);
		if (te.seekExact(new BytesRef(word))) 
		{	
			for (ConciseDocument doc : docs) 
			{
				int freq = 0;
				DocsEnum de = te.docs(MultiFields.getLiveDocs(workspace.getIndexReader()), null);
				if (de.advance(doc.docID) != DocsEnum.NO_MORE_DOCS &&
					doc.docID == de.docID())
				{
						freq = de.freq();
				}
				map.put(doc, Integer.valueOf(freq));
			}
		}
		return map;
	}
	
}
