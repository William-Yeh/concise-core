package org.sustudio.concise.core.statistics;

import java.util.List;

import org.sustudio.concise.core.Workspace;

public abstract class ConciseMultivariate {

	public static final double SMALL = -1.0e10;
    public static final double MAXVAL = 1.0e12;
	
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
	 * returns the Eigenvalues
	 * @return
	 */
	public abstract double[] getEigenvalues();
	
	/**
	 * returns rates of inertia (or explained)
	 * @return
	 */
	public abstract double[] getRates();
	
	
	public abstract Object getResult();
}
