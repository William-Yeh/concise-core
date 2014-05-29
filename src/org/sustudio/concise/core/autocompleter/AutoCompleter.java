package org.sustudio.concise.core.autocompleter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.tst.TSTLookup;
import org.sustudio.concise.core.corpus.importer.ConciseField;
import org.sustudio.concise.core.wordlister.CustomPartOfSpeechEncoder;
import org.sustudio.concise.core.wordlister.Word;

/**
 * 根據輸入的字，會自動給出建議搜尋
 * 
 * @author Kuan-ming Su
 *
 */
public class AutoCompleter {

	private static HashMap<IndexReader, AutoCompleter> map = new HashMap<IndexReader, AutoCompleter>();
	
	public static AutoCompleter getInstanceFor(final IndexReader reader, boolean showPartOfSpeech) throws IOException {
		AutoCompleter completer = map.get(reader);
		if (completer == null) {
			completer = new AutoCompleter(reader, showPartOfSpeech);
			map.put(reader, completer);
		}
		return completer;
	}
	
	public static void removeInstanceFor(final IndexReader reader) throws IOException {
		AutoCompleter completer = map.remove(reader);
		if (completer != null) {
			completer.close();
		}
	}
	
	
	private final IndexReader reader;
	private Lookup autoCompleter;
	private Dictionary dict;
	private boolean showPartOfSpeech;
	
	public AutoCompleter(final IndexReader reader, boolean showPartOfSpeech) throws IOException {
		this.reader = reader;
		this.showPartOfSpeech = showPartOfSpeech;
		init();
	}
	
	public void setShowPartOfSpeech(boolean show) throws IOException {
		if (showPartOfSpeech != show) {
			close();
			init();
		}
	}
	
	public List<Word> lookup(CharSequence text, int number) throws IOException {
		
		init();
		
		List<Word> result = new ArrayList<Word>();
		for (Lookup.LookupResult r : autoCompleter.lookup(text, true, number)) 
		{
			String key = r.key.toString();
			Word word = new Word(CustomPartOfSpeechEncoder.encode(key), 0, 0);
			result.add(word);
			Term term = new Term(ConciseField.CONTENT.field(), key);
			word.docFreq = reader.docFreq(term);
			word.totalTermFreq = reader.totalTermFreq(term);
		}
		
		Collections.sort(result, new Comparator<Word>() {
			public int compare(Word w1, Word w2) {
				long t1 = w1.totalTermFreq;
				long t2 = w2.totalTermFreq;
				if (t1 > t2)		return -1;
				else if (t1 < t2)	return 1;
				else				return 0;
			}
			
		});
		
		return result;
	}
	
	
	private void init() throws IOException {
		
		if (dict == null) 
		{
			if (reader != null) 
			{
				dict = new ConciseDictionary(reader, ConciseField.CONTENT.field(), showPartOfSpeech);
				
				if (autoCompleter == null) {
					autoCompleter = new TSTLookup();
				}
				autoCompleter.build(dict);
			}
		}
		
	}

	
	public void close() {
		autoCompleter = null;
		dict = null;
	}
	
}
