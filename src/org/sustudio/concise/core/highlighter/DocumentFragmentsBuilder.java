package org.sustudio.concise.core.highlighter;

import org.apache.lucene.document.Field;
import org.apache.lucene.search.highlight.Encoder;
import org.apache.lucene.search.vectorhighlight.FieldFragList.WeightedFragInfo;
import org.apache.lucene.search.vectorhighlight.FieldFragList.WeightedFragInfo.SubInfo;
import org.apache.lucene.search.vectorhighlight.FieldPhraseList.WeightedPhraseInfo.Toffs;
import org.apache.lucene.search.vectorhighlight.ScoreOrderFragmentsBuilder;
import org.sustudio.concise.core.Config;

/**
 * 整份文件的 FragmentsBuilder
 * 
 * @author Kuan-ming Su
 *
 */
public class DocumentFragmentsBuilder extends ScoreOrderFragmentsBuilder {
	
	protected String makeFragment( StringBuilder buffer, int[] index, Field[] values, WeightedFragInfo fragInfo,
			String[] preTags, String[] postTags, Encoder encoder ){
		
		StringBuilder fragment = new StringBuilder();
		final int s = fragInfo.getStartOffset();
		int[] modifiedStartOffset = { s };
		String src = getFragmentSourceMSO( buffer, index, values, s, fragInfo.getEndOffset(), modifiedStartOffset );
		int srcIndex = 0;
		for( SubInfo subInfo : fragInfo.getSubInfos() ){
			for( Toffs to : subInfo.getTermsOffsets() ){

				int posOffset = 0;
				if (to.getEndOffset() - modifiedStartOffset[0] + Config.SYSTEM_POS_SEPERATOR.length() < src.length() &&
					src.substring(to.getEndOffset() - modifiedStartOffset[0], to.getEndOffset() - modifiedStartOffset[0] + Config.SYSTEM_POS_SEPERATOR.length()).equals(Config.SYSTEM_POS_SEPERATOR) )
				{
					posOffset = src.indexOf(" ", to.getEndOffset() - modifiedStartOffset[0]);
					if (posOffset != -1) posOffset -= (to.getEndOffset() - modifiedStartOffset[0]);
					else posOffset = 0;
				}
				
				fragment
				.append( encoder.encodeText( src.substring( srcIndex, to.getStartOffset() - modifiedStartOffset[0] ) ) )
				.append( getPreTag( preTags, subInfo.getSeqnum() ) )
				.append( encoder.encodeText( src.substring( to.getStartOffset() - modifiedStartOffset[0], to.getEndOffset() - modifiedStartOffset[0] + posOffset ) ) )
				.append( getPostTag( postTags, subInfo.getSeqnum() ) );
				srcIndex = to.getEndOffset() - modifiedStartOffset[0] + posOffset;
			}
		}
		fragment.append( encoder.encodeText( src.substring( srcIndex ) ) );
		return fragment.toString();
	}

}
