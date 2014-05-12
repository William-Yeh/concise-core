package org.sustudio.concise.core.corpus.importer;

import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.sustudio.concise.core.CCPrefs;
import org.sustudio.concise.core.Config;
import org.sustudio.concise.core.corpus.importer.MMSegAnalyzer.MMSeg;

public enum AnalyzerEnum {

	MMSegComplex ("MMSeg Complex"),
	MMSegSimple	 ("MMSeg Simple"),
	SmartChinese ("Smart Chinese (Simplified Chinese)"),
	Whitespace   ("Whitespace");
	
	private String label;
	AnalyzerEnum(String label) {
		this.label = label;
	}
	
	public String label() {
		return label;
	}
	
	public Analyzer getAnalyzer() {
		switch(this) {
		case MMSegComplex:	
			if (analyzer == null || 
				!(analyzer instanceof MMSegAnalyzer) || 
				((MMSegAnalyzer) analyzer).getSegEnum() != MMSeg.Complex ||
				((MMSegAnalyzer) analyzer).getUserDictFiles() != CCPrefs.userDictionaries)
			{
				analyzer = new MMSegAnalyzer(MMSeg.Complex, CCPrefs.userDictionaries);
			}
			return analyzer;
			
		case MMSegSimple:		
			if (analyzer == null || 
				!(analyzer instanceof MMSegAnalyzer) ||
				((MMSegAnalyzer) analyzer).getSegEnum() != MMSeg.Simple ||
				((MMSegAnalyzer) analyzer).getUserDictFiles() != CCPrefs.userDictionaries)
			{
				analyzer = new MMSegAnalyzer(MMSeg.Simple, CCPrefs.userDictionaries);
			}
			return analyzer;
		
		case SmartChinese:		
			if (analyzer == null ||
				!(analyzer instanceof SmartChineseAnalyzer))
			{
				analyzer = new SmartChineseAnalyzer(Config.LUCENE_VERSION);
			}
			return analyzer;
		
		case Whitespace:		
			if (analyzer == null ||
				!(analyzer instanceof WhitespaceAnalyzer))
			{
				analyzer = new WhitespaceAnalyzer(Config.LUCENE_VERSION);
			}
			return analyzer;
		}
		return null;
	}
	
	private static Analyzer analyzer;
		
	public static String[] labels() {
		ArrayList<String> labelList = new ArrayList<String>();
		for (AnalyzerEnum analyzer : values()) {
			labelList.add(analyzer.label());
		}
		return labelList.toArray(new String[0]);
	}
	
	public static AnalyzerEnum valueOfLabel(String label) {
		for (AnalyzerEnum analyzer : values()) {
			if (analyzer.label().equals(label)) {
				return analyzer;
			}
		}
		return null;
	}
}
