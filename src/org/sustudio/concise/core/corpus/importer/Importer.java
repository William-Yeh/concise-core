package org.sustudio.concise.core.corpus.importer;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.Field.Store;
import org.apache.tika.Tika;
import org.sustudio.concise.core.CCPrefs;
import org.sustudio.concise.core.Config;
import org.sustudio.concise.core.Workspace;
import org.sustudio.concise.core.Workspace.INDEX;
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
	
	private MaxentTagger posTagger;
	private Analyzer indexAnalyzer;
	private int fileCount = 0;
	
	/**
	 * default constructor.
	 * @param workspace
	 * @throws IOException
	 */
	public Importer(Workspace workspace) throws IOException {
		this(workspace, INDEX.DOCUMENT);
	}
	
	
	public Importer(Workspace workspace, INDEX indexType) throws IOException {
		super(workspace, indexType);
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
		
		StringTokenizer st = new StringTokenizer(buffer.toString(), " \n");
		int numWords = st.countTokens();
		
		writeDocument(file, isTokenized, buffer.toString(), numWords, countParas);
		
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
	
	
	protected Document writeDocument(File sourceFile, boolean isTokenized, String content, int numWords, int numParas) throws IOException {
		// copy sourceFile to ORIGINAL folder
		File targetFile = ConciseFileUtils.getUniqueFile(
				new File(originalFolder, sourceFile.getName()));
		FileUtils.copyFile(sourceFile, targetFile);
		
		Document doc = new Document();
		doc.add(new StringField(
						ConciseField.TITLE.field(), 
						sourceFile.getName(), 
						Store.YES));
		doc.add(new ContentField(
						ConciseField.CONTENT.field(), 
						content, 
						Store.YES));
		doc.add(new StringField(
						ConciseField.FILENAME.field(),
						targetFile.getName(),
						Store.YES));
		doc.add(new IntField(
						ConciseField.NUM_WORDS.field(),
						numWords, 
						Store.YES));
		doc.add(new IntField(
						ConciseField.NUM_PARAGRAPHS.field(), 
						numParas, 
						Store.YES));
		doc.add(new IntField(
						ConciseField.IS_TOKENIZED.field(),
						isTokenized ? 1 : 0,
						Store.YES));
		
		addDocument(doc);
		fileCount++;
		if (fileCount % 100 == 0)
			commit();
		return doc;
	}
}
