package org.sustudio.concise.core.collocation;

import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.highlight.Encoder;
import org.apache.lucene.search.vectorhighlight.FieldFragList;
import org.apache.lucene.search.vectorhighlight.SimpleFragmentsBuilder;
import org.apache.lucene.search.vectorhighlight.FieldFragList.WeightedFragInfo;
import org.apache.lucene.search.vectorhighlight.FieldFragList.WeightedFragInfo.SubInfo;
import org.apache.lucene.search.vectorhighlight.FieldPhraseList.WeightedPhraseInfo.Toffs;
import org.sustudio.concise.core.Config;
import org.sustudio.concise.core.concordance.Conc;

/**
 * 有包含 node word 的每一句都分別拉出來，做成 highlight 的句子
 * 
 * @author Kuan-ming Su
 *
 */
public class TextualSentenceFragmentsBuilder extends SimpleFragmentsBuilder {
	
	/** 要顯示的部分 */
	public enum SHOW {
		/** 顯示所有句子 */
		ALL_SENTENCES,
		/** 只顯示有 highlight 的句子 */
		HIGHLIGH_SENTENCES;
	}
	
	private final SHOW show;
	private final BreakIterator breakIterator;
	
	public TextualSentenceFragmentsBuilder(SHOW show) {
		super(new String[] { Conc.preNodeTag }, new String[] { Conc.postNodeTag });
		this.show = show;
		this.breakIterator = BreakIterator.getSentenceInstance();
	}
    
    @Override
    public String[] createFragments( IndexReader reader, int docId,
        String fieldName, FieldFragList fieldFragList, int maxNumFragments,
        String[] preTags, String[] postTags, Encoder encoder ) throws IOException {
    	
    	// read the source string back from the index
        Document doc = reader.document(docId);
        String source = doc.get(fieldName);
        if (source.isEmpty()) {
        	return new String[] { source };
        }
    	
    	List<WeightedFragInfo> fragInfos = fieldFragList.getFragInfos();
    	Field[] values = getFields( reader, docId, fieldName );
    	if( values.length == 0 ) {
    		return null;
    	}
    	
    	if (isDiscreteMultiValueHighlighting() && values.length > 1) {
    		fragInfos = discreteMultiValueHighlighting(fragInfos, values);
    	}
    	
    	fragInfos = getWeightedFragInfoList(fragInfos);
    	List<String> fragments = new ArrayList<String>( fragInfos.size() );
    	
        // build fragments
    	breakIterator.setText(source);
    	
    	StringBuilder buffer = new StringBuilder();
    	for( int n = 0; n < fragInfos.size(); n++ ){
    		WeightedFragInfo fragInfo = fragInfos.get( n );
    		
    		
    		int startOffset = fragInfo.getStartOffset();
    		int sentenceStart = breakIterator.first();
    		ArrayList<Toffs> builds = new ArrayList<Toffs>();
    		for (int sentenceEnd = breakIterator.next();
    			 sentenceEnd != BreakIterator.DONE;
    			 sentenceStart = sentenceEnd, sentenceEnd = breakIterator.next())
    		{
    			for ( SubInfo subInfo : fragInfo.getSubInfos() ) {
        			for ( Toffs to : subInfo.getTermsOffsets() ) {
        				if (to.getStartOffset() - startOffset >= sentenceStart &&
        					to.getEndOffset() - startOffset <= sentenceEnd) 
        				{
        					builds.add(to);
        				}
        			}
    			}
    			
    			// build sentence
    			switch (show) {
				case ALL_SENTENCES:
					break;
				case HIGHLIGH_SENTENCES:
				default:
					if (builds.isEmpty()) continue;
					break;
    			}
    			
    			// 如果有顯示詞性的話，得要跳過
				if (sentenceStart + Config.SYSTEM_POS_SEPERATOR.length() < source.length() &&
					source.substring(sentenceStart, sentenceStart + Config.SYSTEM_POS_SEPERATOR.length()).equals(Config.SYSTEM_POS_SEPERATOR)) 
				{
					sentenceStart = source.indexOf(" ", sentenceStart);
				}
				
				for (Toffs to : builds) {
					// 檢查 node 後面有沒有接詞性，有的話需要加上偏移
					int posOffset = 0;
					if (to.getEndOffset() - startOffset + Config.SYSTEM_POS_SEPERATOR.length() < source.length() &&
    					source.substring(to.getEndOffset() - startOffset, to.getEndOffset() - startOffset + Config.SYSTEM_POS_SEPERATOR.length()).equals(Config.SYSTEM_POS_SEPERATOR) )
    				{
    					posOffset = source.indexOf(" ", to.getEndOffset() - startOffset);
    					if (posOffset != -1) posOffset -= (to.getEndOffset() - startOffset);
    					else posOffset = 0;
    				}
					buffer
					.append( encoder.encodeText( source.substring(sentenceStart, to.getStartOffset() - startOffset) ) )
					.append( getPreTag( preTags, 0 ) )
		    		.append( encoder.encodeText( source.substring( to.getStartOffset() - startOffset, to.getEndOffset() - startOffset + posOffset ) ) )
		    		.append( getPreTag( postTags, 0 ) );
					sentenceStart = to.getEndOffset() - startOffset + posOffset;
				}
	    		
	    		// 為句子結尾的標點符號加上 part-of-speech
	    		if (sentenceEnd != source.length() && 
	    			source.substring(sentenceEnd, sentenceEnd + Config.SYSTEM_POS_SEPERATOR.length()).equals(Config.SYSTEM_POS_SEPERATOR))
	    		{
	    			int end = source.indexOf(" ", sentenceEnd);
	    			if (end == -1)
	    				end = source.length();
	    			sentenceEnd = end;
	    		}
	    		buffer.append( encoder.encodeText( source.substring( sentenceStart, sentenceEnd)));
	    		
	    		fragments.add( buffer.toString() );
	    		buffer.setLength(0);
    			builds.clear();
    		}
    		
    	}
    	return fragments.toArray( new String[fragments.size()] );
    }
    
}
