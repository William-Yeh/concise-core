package org.sustudio.concise.core.corpus.importer;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.util.Version;

/**
 * 匯入時用的 analyzer. 用 {@link WhitespaceTokenizer} 分開，
 * 再用 {@link ImportPOSFilter} 把 token 弄成有 part-of-speech 的和去掉 part-of-speech 兩種。
 * 
 * @author Kuan-ming Su
 *
 */
public class ImportPOSAnalyzer extends Analyzer {
	
	private Version matchVersion;
	
	public ImportPOSAnalyzer(Version matchVersion) {
		this.matchVersion = matchVersion;
	}
	
	@Override
	protected TokenStreamComponents createComponents(String fieldName,
			Reader reader) {
		
		Tokenizer source = new WhitespaceTokenizer(matchVersion, reader);
		
		TokenStream stream = new ImportPOSFilter(source);
		
		return new TokenStreamComponents(source, stream);
	}

}
