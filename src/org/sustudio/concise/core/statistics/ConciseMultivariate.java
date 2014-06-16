package org.sustudio.concise.core.statistics;

import java.util.List;

import org.sustudio.concise.core.Workspace;

public abstract class ConciseMultivariate {

	protected final Workspace workspace;
	protected final boolean showPartOfSpeech;
	
	public ConciseMultivariate(Workspace workspace, boolean showPartOfSpeech) {
		this.workspace = workspace;
		this.showPartOfSpeech = showPartOfSpeech;
	}
	
	public abstract void setWords(List<String> words) throws Exception;
	
	
	/**
	 * 傳回文字的投影坐標（僅看 Factor1(x) 和 Factor2(y) 的投影）
	 * @return
	 */
	public abstract List<WordPlotData> getRowProjectionData();
	
	
	/**
	 * 傳回文件的投影坐標（僅看 Factor1(x) 和 Factor2(y) 的投影）
	 * @return
	 */
	public abstract List<DocumentPlotData> getColProjectionData();
	
	
	/**
	 * returns the EigenValues of the principal components analysis
	 * @return
	 */
	public abstract double[] getEigenValues();
	
	public abstract double[] getRatesOfInertia();
}
