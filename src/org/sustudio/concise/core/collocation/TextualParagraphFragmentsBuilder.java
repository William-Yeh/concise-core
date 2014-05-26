package org.sustudio.concise.core.collocation;

import java.io.IOException;
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
public class TextualParagraphFragmentsBuilder extends SimpleFragmentsBuilder {
	
	/** 要顯示的部分 */
	public enum SHOW {
		/** 顯示所有句子 */
		ALL_SENTENCES,
		/** 只顯示有 highlight 的句子 */
		HIGHLIGH_SENTENCES;
	}
	
	private final SHOW show;
	
	public TextualParagraphFragmentsBuilder(SHOW show) {
		super(new String[] { Conc.preNodeTag }, new String[] { Conc.postNodeTag });
		this.show = show;
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
    	ParaIterator para = new ParaIterator(source);
    	
    	StringBuilder buffer = new StringBuilder();
    	for( int n = 0; n < fragInfos.size(); n++ ){
    		WeightedFragInfo fragInfo = fragInfos.get( n );
    		
    		int startOffset = fragInfo.getStartOffset();
    		int paraStart = para.first();
    		ArrayList<Toffs> builds = new ArrayList<Toffs>();
    		for (int paraEnd = para.next();
    			 paraEnd != ParaIterator.DONE;
    			 paraStart = paraEnd, paraEnd = para.next())
    		{
    			for ( SubInfo subInfo : fragInfo.getSubInfos() ) {
        			for ( Toffs to : subInfo.getTermsOffsets() ) {
        				if (to.getStartOffset() - startOffset >= paraStart &&
        					to.getEndOffset() - startOffset <= paraEnd) 
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
					.append( encoder.encodeText( source.substring(paraStart, to.getStartOffset() - startOffset) ) )
					.append( getPreTag( preTags, 0 ) );
		    		String node = encoder.encodeText( source.substring( to.getStartOffset() - startOffset, to.getEndOffset() - startOffset + posOffset ) ); 
		    		buffer
					.append( node.trim().replace(" ", CollocateIterator._NODE_SEPARATOR) )
		    		.append( getPostTag( postTags, 0 ) + " " );
					paraStart = to.getEndOffset() - startOffset + posOffset;
				}
	    		
	    		buffer.append( encoder.encodeText( source.substring( paraStart, paraEnd)));
	    		
	    		fragments.add( buffer.toString() );
	    		buffer.setLength(0);
    			builds.clear();
    		}
    		
    	}
    	return fragments.toArray( new String[fragments.size()] );
    }
    
    
    class ParaIterator {
    	
    	static final int DONE = -1;
    	private static final char paraChar = '\n';
    	private final String text;
    	
    	int offset;
    	
    	ParaIterator(String text) {
    		this.text = text;
    		offset = first();
    	}
    	
    	int first() {
    		return 0;
    	}
    	
    	boolean isBoundary(int offset) {
    		return offset == 0 || 
    			   offset == text.length() || 
    			   text.charAt(offset) == paraChar;
    	}
    	
    	int next() {
    		if (isBoundary(offset)) {
    			offset++;
    		}
    		int nextOffset = text.indexOf(paraChar, offset);
    		if (nextOffset == -1 && offset < text.length()) {
    			nextOffset = text.length();
    		}
    		offset = nextOffset;
    		return offset;
    	}
    	
    }
}
