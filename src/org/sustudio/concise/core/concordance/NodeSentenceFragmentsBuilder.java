package org.sustudio.concise.core.concordance;

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

/**
 * 有包含 node word 的每一句都分別拉出來，做成 highlight 的句子
 * 
 * @author Kuan-ming Su
 *
 */
public class NodeSentenceFragmentsBuilder extends SimpleFragmentsBuilder {
	
	private final BreakIterator breakIterator;
	
	public NodeSentenceFragmentsBuilder() {
		super();
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
    		for ( SubInfo subInfo : fragInfo.getSubInfos() ) {
    			for ( Toffs to : subInfo.getTermsOffsets() ) {
    				breakIterator.following(to.getStartOffset() - startOffset);
    				int prev = breakIterator.previous();
    				if (prev == BreakIterator.DONE) {
    					prev = 0;
    				}
    				// 如果有顯示詞性的話，得要跳過
    				if (source.substring(prev, prev + Config.SYSTEM_POS_SEPERATOR.length()).equals(Config.SYSTEM_POS_SEPERATOR)) 
    				{
    					prev = source.indexOf(" ", prev);
    				}
    				buffer.append( encoder.encodeText( source.substring(prev, to.getStartOffset() - startOffset) ) );
    				
    				// 檢查 node 後面有沒有接詞性，有的話需要加上偏移
    				int posOffset = 0;
    				if (to.getEndOffset() - startOffset + Config.SYSTEM_POS_SEPERATOR.length() < source.length() &&
    					source.substring(to.getEndOffset() - startOffset, to.getEndOffset() - startOffset + Config.SYSTEM_POS_SEPERATOR.length()).equals(Config.SYSTEM_POS_SEPERATOR) )
    				{
    					posOffset = source.indexOf(" ", to.getEndOffset() - startOffset);
    					if (posOffset != -1) posOffset -= (to.getEndOffset() - startOffset);
    					else posOffset = 0;
    				}
    	    		buffer.append( getPreTag( preTags, subInfo.getSeqnum() ) );
    	    		buffer.append( encoder.encodeText( source.substring( to.getStartOffset() - startOffset, to.getEndOffset() - startOffset + posOffset ) ) );
    	    		buffer.append( getPostTag( postTags, subInfo.getSeqnum() ) );
    	    		
    	    		int next = breakIterator.following( to.getEndOffset() - startOffset + posOffset );
    	    		if (next == BreakIterator.DONE) { 
    	    			next = source.length();
    	    		}
    	    		// 為句子結尾的標點符號加上 part-of-speech
    	    		if (next != source.length() && 
    	    			next + Config.SYSTEM_POS_SEPERATOR.length() < source.length() &&
    	    			source.substring(next, next + Config.SYSTEM_POS_SEPERATOR.length()).equals(Config.SYSTEM_POS_SEPERATOR))
    	    		{
    	    			int end = source.indexOf(" ", next);
    	    			if (end == -1)
    	    				end = source.length();
    	    			next = end;
    	    		}
    	    		buffer.append( encoder.encodeText( source.substring( to.getEndOffset() - startOffset + posOffset, next)));
    	    		
    	    		if (buffer.length() > 0) {
            			fragments.add( buffer.toString() );
            		}
            		buffer.setLength(0);
    			}
    			
    		}
    	}
    	return fragments.toArray( new String[fragments.size()] );
    }
}
