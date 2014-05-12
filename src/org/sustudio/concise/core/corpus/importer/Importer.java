package org.sustudio.concise.core.corpus.importer;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.Field.Store;
import org.apache.tika.Tika;
import org.sustudio.concise.core.CCPrefs;
import org.sustudio.concise.core.Config;
import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.corpus.DocumentWriter;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * <p>輸入文件，並依據選擇的 POS tagger 模型進行詞性標注。</p>
 * <p>運用 <a href="http://nlp.stanford.edu/software/tagger.shtml">Stanford Log-linear Part-Of-Speech Tagger</a> 進行
 * 詞性標注，預設的模型是中文 chinese-distsim.tagger 。若要使用其他的模型，可以從 Stanford 那邊下載。</p>
 * <p>該模型是利用 Penn Chinese Treebank 訓練的，詞性說明 <a href="http://www.cis.upenn.edu/~chinese/posguide.3rd.ch.pdf">http://www.cis.upenn.edu/~chinese/posguide.3rd.ch.pdf</a>
 *  。</p> 
 *
 * 
 * @author Kuan-ming Su
 *
 */
public class Importer extends DocumentWriter {
	
	//public IndexWriter writer;
	private MaxentTagger posTagger;
	private Analyzer indexAnalyzer;
	
	
	/**
	 * default constructor.
	 * @param workspace
	 * @throws IOException
	 */
	public Importer(Workspace workspace) throws IOException {
		this(workspace, workspace.getIndexDir());
	}
	
	/**
	 * use this constructor only to import reference corpus. 
	 * @param workspace
	 * @param indexDir	reference corpus 的路徑，使用 {@link Workspace#getIndexDirRef()}
	 * @throws IOException
	 */
	public Importer(Workspace workspace, File indexDir) throws IOException {
		super(workspace, indexDir);
	}

	/**
	 * close Importer.
	 * @throws IOException
	 */
	public void close() throws IOException {
		super.close();
		posTagger = null;
		indexAnalyzer = null;
	}
	
	public void indexFile(File file, boolean isTokenized) throws Exception {
		if (isTokenized) {
			posTagger = null;
			if ( indexAnalyzer == null || !(indexAnalyzer instanceof WhitespaceAnalyzer) ) 
			{
				indexAnalyzer = new WhitespaceAnalyzer(Config.LUCENE_VERSION); 
			}
		}
		else {
			if (posTagger == null) {
				posTagger = new MaxentTagger(CCPrefs.POS_TAGGER_MODEL);
			}
			indexAnalyzer = CCPrefs.rawDocAnalyzer.getAnalyzer();
		}
		
		StringBuilder buffer = new StringBuilder();
		StringBuilder paraBuffer = new StringBuilder();
		int countParas = 0;
		
		Reader reader = new Tika().parse(file);
		ParagraphIterator iter = new ParagraphIterator(reader);
		while (iter.hasMoreParagraphs()) {
			String para = iter.nextParagraph();
			
			TokenStream tokenStream = indexAnalyzer.tokenStream(null, new StringReader(para));
			CharTermAttribute termAttr = tokenStream.addAttribute(CharTermAttribute.class);
			tokenStream.reset();
			paraBuffer.setLength(0);
			while (tokenStream.incrementToken()) {
				paraBuffer.append(termAttr.toString() + " ");
			}
			
			String taggedParagraph = paraBuffer.toString();
			if (posTagger != null) {
				taggedParagraph = posTagger.tagTokenizedString(taggedParagraph);
				// 轉回半形
				taggedParagraph = toAscii(taggedParagraph);
			}
			taggedParagraph = taggedParagraph.replace(CCPrefs.POS_TAGGER_SEPARATOR, Config.SYSTEM_POS_SEPERATOR);
			buffer.append(taggedParagraph + "\n");
			countParas++;
			tokenStream.close();
		}
		paraBuffer.setLength(0);
		paraBuffer = null;
		
		writeDocument(file, isTokenized, buffer.toString(), countParas);
		
		buffer.setLength(0);
		buffer = null;
		
	}
	
	/**
	 * 把全形轉回半形（逗號不管）
	 * @param text
	 * @return
	 */
	private String toAscii(String text) {  
		  
		String asciiTable = ". !\"#$%&'()*+-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
		String fullTable = "．　！”＃＄％＆’（）＊＋－‧／０１２３４５６７８９：；＜＝＞？＠ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ〔＼〕︿＿｀ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ｛｜｝～";
		
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			int fullChar = fullTable.indexOf(c);
			if (fullChar > -1) {
				c = asciiTable.charAt(fullChar);
			}
			buffer.append(c);
		  }
		  
	return buffer.toString();
		 
	}
	
	
	protected Document writeDocument(File file, boolean isTokenized, String content, int numParas) throws IOException {
		Document doc = new Document();
		doc.add(new StringField(ConciseField.TITLE.field(), file.getName(), Store.YES));
		doc.add(new ContentField(ConciseField.CONTENT.field(), content, Store.YES));
		doc.add(new StringField(ConciseField.FILEPATH.field(), file.getPath(), Store.YES));
		doc.add(new LongField(ConciseField.NUM_PARAGRAPHS.field(), numParas, Store.YES));
		doc.add(new IntField(ConciseField.IS_TOKENIZED.field(), isTokenized ? 1 : 0, Store.YES));
		
		addDocument(doc);
		return doc;
	}
}
