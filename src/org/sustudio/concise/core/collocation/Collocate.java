package org.sustudio.concise.core.collocation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Collocate is a word cooccurred near the node word.  J. R. Firth (1957) introduced the term "collocations" 
 * for characteristic and frequently recurrent word combinations, arguing that the meaning and usage of a 
 * word (the node) can to some extent be characterised by its most typical collocates: "You shall know a 
 * word by the company it keeps" (Firth 1957, 179).
 * <p>
 * Following the Firthian tradition (e.g. Sinclair 1991), a collocation is a combination of two words that 
 * exhibit a tendency to occur near each other in natural language, i.e. to cooccur.  The cooccurrence of 
 * words here is defined as surfface cooccurrence (Evert 2007) where words are said to cooccur if they appear 
 * close to each other in running text, measured by the number of intervening word tokens.  Surface 
 * cooccurrence is often, though not always combined with a node-collocate view, looking for collocates 
 * within the collocational spans around the instances of a given node word.
 * <p>
 * Two kinds of association measures are computed in the {@link Collocate}, i.e. simple association measures 
 * and statistical association measures.  A simple association measure interprets observed cooccurrence 
 * frequency <tt>O</tt> by comparison with the expected frequency <tt>E</tt>, and calculates an association 
 * score as a quantitative measure for the attraction between two words.
 * <p>
 * A rigious statistical approach to measuring association is based on contingency tables representing the 
 * cross-classification of a set of items.  However, the statistical interpretation of surface cooccurrence 
 * is slightly different from the normal contingency table (See the extended manuscript of Evert's (2007) 
 * <a href="http://www.stefan-evert.de/PUB/Evert2007HSK_extended_manuscript.pdf">Corpora and 
 * Collocations</a>, pp. 27-28).
 * <p>
 * Association measures can be divided into two general groups: measures of <i>effect size</i> (see {@link #getMI()}, 
 * {@link #getMI3()}, {@link #getOddsRatio()} and {@link #getDice()}) and measures of <i>significance</i> 
 * (see {@link #getZscore()}, {@link #getTscore()}, {@link #getSimpleLL()}, {@link #getChiSquaredCorr()} and 
 * {@link #getLogLikelihood()}).  The former ask the question "how strongly are the words attracted to each 
 * other?" (operationalised as "how much does observed cooccurrence frequency exceed expected frequency?"), while 
 * the latter as "how much evidence is there for a positive association between the words, no matter how small 
 * effect size is?" (operationalised as "how unlikely is the null hypothesis that the words are independent?").
 * The two approaches to measuring association are not entirely unrelated: a word pair with large "true" effect
 * size is also more likely to show significant evidence against the null hypothesis in a sample.  However, there
 * is an important difference between the two groups.  Effect-size measures typically fail to account for sampling
 * variation and are prone to a low-frequency bias (small <tt>E</tt> easily leads to spuriously high effect 
 * size estimates, even for <tt>O=1</tt> or <tt>O=2</tt>), while significance measures are often prone to a 
 * high-frequency bias (if <tt>O</tt> is sufficiently large, even a small relative difference between <tt>O</tt> and 
 * <tt>E</tt>, i.e. a small effect size, can be highly significant).
 * <p> 
 * 
 * @author Kuan-ming Su.
 *
 */
public class Collocate implements Serializable {

	private static final long serialVersionUID = -6288403493926365739L;
	
	// the signature (O, f1, f2, N) is based on Evert's article
	private long signatureO;
	private long signatureF1;
	private long signatureF2;
	private long signatureN;
	
	// basic info
	private String word;
	
	// simple association measures
	private double mi;
	private double mi3;
	private double zscore;
	private double tscore;
	private double simpleLL;
	
	// statistical association measures
	private double dice;
	private double oddsRatio;
	private double chiSquaredCorr;
	private double logLikelihood;
	
	private Map<String, Long> positionVector = new HashMap<String, Long>();
	
	/**
	 * Constructor of a collocate that creates a collocation and its association measures (<tt>MI, MI3, 
	 * simple-ll, Dice coefficient, odds ratio, z-score, t-score, chi-squared<sub>corr</sub> and 
	 * log-likelihood</tt>).  Association measures are basically based on Evert's (2007) extended manuscript 
	 * of <a href="http://cogsci.uni-osnabrueck.de/~severt/PUB/Evert2007HSK_extended_manuscript.pdf">Corpora 
	 * and Collocations</a> <small>(available on-line at <a href="http://cogsci.uni-osnabrueck.de/~severt/PUB/Evert2007HSK_extended_manuscript.pdf">
	 * http://cogsci.uni-osnabrueck.de/~severt/PUB/Evert2007HSK_extended_manuscript.pdf</a>)</small>, 
	 * except the expected value of simple association measures.
	 * <p>
	 * For simple association measures, Evert (2007) suggests an approximation formula for expected value, 
	 * <tt>E = k*f<sub>1</sub>*f<sub>2</sub>/N</tt>. The factor <tt>k</tt> represents the total span size, 
	 * e.g. <tt>k=10</tt> for a symmetric span of 5 words (L5, R5), <tt>k=4</tt>
	 * for a span (L3, R1), and <tt>k=1</tt> for simple bigrams (L0, R1).
	 * <p>
	 * However, the span is not always fixed while running into the sentence boundary or paragraph boundary.  
	 * To deal with such cases, the total number of words (tokens) in the sample <tt>N<sub>s</sub></tt> is 
	 * considered to calculate the expected value.  The formula thus turns into 
	 * <tt>E = N<sub>s</sub>*f<sub>1</sub>/N</tt>.
	 * <p>
	 * <p>
	 * <b>PLEASE NOTE:</b> Statistical association measures are not 100% accurate.  There is a little error in the 
	 * way Concise implements contingency table.  Concise over calculates the cooccurrences of node-collocate pair 
	 * in some circumstances.  This is best illustrated with an example:
	 * <p>
	 * <blockquote>He said <i>please please please</i> <b>leave</b> me alone.</blockquote>
	 * <p>
	 * Here, the lexical item <i>please</i> co-occurs with <i>leave</i> three times within a window span of 
	 * -3 to +3 words.  In theory, the collocate <i>please</i> should be only counted once.  However, Concise 
	 * counts three occurrences of <i>please</i> in this sentence.  As a result, the collocational strength of
	 * items which co-occur repeatedly with the node item will be higher than it should be.  In extreme cases,
	 * where the collocate is found more often than the number of the running text containing the node-collocate
	 * pair, this will result in a mathematical error (calculation of logarithm of a negative number).  These
	 * collocates will be displayed in the collocation table but no collocation value is given.
	 * 
	 * @param word		the collocate word.
	 * @param fn 		marginal frequency of the node 
	 * 					(i.e. number of occurrences of the node in the corpus, i.e. number of concordance lines).
	 * @param fc		marginal frequency of the collocate 
	 * 					(i.e. number of occurrences of the collocate in the corpus).
	 * @param fnc		co-occurrence frequency of (node, collocate) pair. 
	 * 					(i.e. number of co-occurrences of the node and the collocate within a given span).
	 * @param Nc		the total number of words (tokens) in the corpus.
	 * @param Ns		the total number of words (tokens) in the sample.
	 */
	public Collocate(String word, long fn, long fc, long fnc, long Nc, long Ns) {
		this(word, fnc, (Ns-fn), fc, (Nc-fn));
	}
	
	
	/**
	 * Another collocate calculation by Evert's (O(f), f1, f2, N) signature
	 * @param word
	 * @param cooccurrence		co-occurrence
	 * @param nodeMarginalFrequency	node marginal frequency
	 * @param collocateMarginalFrequency	collocate's marginal frequency
	 * @param numberOfTexts		number of texts
	 */
	public Collocate(String word, long cooccurrence, long nodeMarginalFrequency, long collocateMarginalFrequency, long numberOfTexts) {
		this.word = word;
		this.signatureO 	= cooccurrence;
		this.signatureF1 	= nodeMarginalFrequency;
		this.signatureF2 	= collocateMarginalFrequency;
		this.signatureN 	= numberOfTexts;
				
		double O11 = (double)signatureO;
		double O12 = (double)signatureF1 - (double)signatureO;
		double O21 = (double)signatureF2 - (double)signatureO;
		double O22 = (double)signatureN - (double)signatureF1 - (double)signatureF2 + (double)signatureO;
		
		double E11 = (double)signatureF1 * (double)signatureF2 / (double)signatureN;
		double E12 = (double)signatureF1 * (double)(signatureN - signatureF2) / (double)signatureN;
		double E21 = (double)(signatureN - signatureF1) * (double)signatureF2 / (double)signatureN;
		double E22 = (double)(signatureN - signatureF1) * (double)(signatureN - signatureF1) / (double)signatureN;
				
		zscore = (O11 - E11) / Math.pow(E11, 0.5);
		tscore = (O11 - E11) / Math.pow(O11, 0.5);
		mi = Math.log( O11 / E11 ) / Math.log(2.0);
		mi3 = Math.log( Math.pow( O11, 3.0 ) / E11) / Math.log(2.0);
		simpleLL = 2 * ( O11 * Math.log( O11/E11) - (O11 - E11));
		
		dice = 2 * O11 / (2 * O11 + O12 + O21);
		oddsRatio = Math.log( ((O11 + 0.5) * (O22 + 0.5)) / ((O12 + 0.5) * (O21 + 0.5)) );
		chiSquaredCorr = (O11 + O12 + O21 + O22) * Math.pow( Math.abs(O11 * O22 - O12 * O21) - (double)signatureN / 2d, 2d) 
							/ ( (O11 + O12) * (O21 + O22) * (O11 + O21) * (O12 + O22) );
		
		//
		// since log(0) = -Infinity (undefined), we have to deal with it.
		// if yi = 0, likelihood(yi | p) = 1-p, log-likelihood = log(1-p)
		// that is, p = 0, 1-p = 1, log(1) = 0.
		// however, a simple way is shown at
		// http://tdunning.blogspot.com/2008/03/surprise-and-coincidence.html
		//
		logLikelihood = 2 * ( O11 * Math.log(O11 / E11 + (O11==0 ? 1 : 0)) 
							+ O12 * Math.log(O12 / E12 + (O12==0 ? 1 : 0)) 
							+ O21 * Math.log(O21 / E21 + (O21==0 ? 1 : 0)) 
							+ O22 * Math.log(O22 / E22 + (O22==0 ? 1 : 0)) );
	}
	
	public long getSignatureO() {
		return signatureO;
	}
	
	public long getSignatureF1() {
		return signatureF1;
	}
	
	public long getSignatureF2() {
		return signatureF2;
	}
	
	public long getSignatureN() {
		return signatureN;
	}
	
	
	public String toString() {
		return(word+" \tf: "+signatureO+"/"+signatureF1+"/"+signatureF2 + " \tT: "+tscore+" \tZ: "+zscore+" \tMI: "+mi);
		//return(word + "\tf:" + signatureO + "\todds-ratio:" + oddsRatio + "\tll:" + logLikelihood);
	}
	
	/**
	 * Returns the collocate word.
	 * @return		the collocate word.
	 */
	public String getWord() {
		return word;
	}
	
	/**
	 * Returns Mutual Information or MI score of the collocate.
	 * <p><tt>MI = log<sub>2</sub>(O/E)</tt>
	 * <p><tt>MI</tt> score is an effect-size measure.
	 * 
	 * @return		MI score of the collocate.
	 */
	public double getMI() {
		return mi;
	}
	
	/**
	 * Returns MI3 score of the collocate.
	 * <p><tt>MI<sup>3</sup> = log<sub>2</sub>(O<sup>3</sup>/E)</tt>
	 * <p><tt>MI<sup>3</sup></tt> score is an effect-size measure.
	 * 
	 * @return		MI3 score of the collocate.
	 */
	public double getMI3() {
		return mi3;
	}
	
	/**
	 * Returns z-score of the collocate.
	 * <p><tt>z-score = (O-E)/sqrt(E)</tt>
	 * <p><tt>z-score</tt> is a significance measure.
	 * <p>An absolute value <tt>|z| > 1.96</tt> is generally considered sufficient to reject the null hypothesis,
	 * i.e. to provide significant evident for a (positive or negative) association; a more conservative threshold 
	 * is <tt>|z| > 3.29</tt>.
	 * 
	 * @return		z-score of the collocate.
	 */
	public double getZscore() {
		return zscore;
	}
	
	/**
	 * Returns t-score of the collocate.
	 * <p><tt>t-score = (O-E)/sqrt(O)</tt>
	 * <p><tt>t-score</tt> is a significance measure.
	 * 
	 * @return		t-score of the collocate.
	 */
	public double getTscore() {
		return tscore;
	}
	
	/**
	 * Returns a simplified version of Dunning's (1993) log likelihood ratio.
	 * <p><tt>simple-ll = 2(O*log(O/E)-(O-E))</tt>
	 * <p><tt>simple-ll</tt> is two-sided.  The cut-off threshold at alpha .95 should be <tt>|simple-ll| > 3.84</tt>.
	 * <p><tt>simple-ll</tt> is a significance measure.
	 * <p>
	 * <p>A reference table can be found at <a href="http://gautam.lis.illinois.edu/monkmiddleware/public/analytics/dunnings.html">
	 * http://gautam.lis.illinois.edu/monkmiddleware/public/analytics/dunnings.html</a>.
	 * <pre>
	 * simple-ll	Percentage	Odds
	 * 3.84 		5%  		1 in 20
	 * 6.63 		1%  		1 in 100
	 * 7.9  		0.5%		1 in 200
	 * 10.83		0.1%		1 in 1,000
	 * 15.15		0.01%		1 in 10,000
	 * 19.5 		0.001%		1 in 100,000
	 * 23.9 		0.0001%		1 in a million
	 * 37.3 		0.0000001	1 in a billion
	 * </pre> 
	 * 
	 * @return 		simplified log likelihood ratio.
	 */
	public double getSimpleLL() {
		return simpleLL;
	}
	
	/**
	 * Returns the Dice coefficient (Smadja et al. 1996) of the collocate.
	 * <p><tt>Dice = 2*O<sub>11</sub>/(R<sub>1</sub>+C<sub>1</sub>)</tt>
	 * <p><tt>Dice coefficient</tt> is an effect-size measure.
	 * 
	 * @return		the Dice coefficient (Smadja et al. 1996) of the collocate.
	 */
	public double getDice() {
		return dice;
	}
	
	/**
	 * Returns the odds ratio (Blaheta and Johnson 2001, 56) of the collocate.
	 * <p><tt>odds-ratio = log( (O<sub>11</sub> + 1/2)(O<sub>22</sub> + 1/2) / (O<sub>12</sub> + 1/2)(O<sub>21</sub> + 1/2) )</tt>
	 * <p><tt>Odds ratio</tt> is an effect-size measure.
	 * <p>
	 * <p>
	 * <b>PLEASE NOTE:</b> There is an error in the way Concise implements odds ratio. 
	 * See {@link #Collocate(String, long, long, long, long, long[], long[], long)} 
	 * or {@link #getLogLikelihood()} for detailed explanation.
	 * 
	 * @return		the odds ratio of the collocate.
	 */
	public double getOddsRatio() {
		return oddsRatio;
	}
	
	/**
	 * Returns Yates' continuity-corrected version of chi-squared.
	 * <p><tt>chi-squared<sub>corr</sub> = N(|O<sub>11</sub>O<sub>22</sub>-O<sub>12</sub>O<sub>21</sub>| - N/2)<sup>2</sup> 
	 * / R<sub>1</sub>R<sub>2</sub>C<sub>1</sub>C<sub>2</sub></tt>
	 * <p><tt>chi-squared<sub>corr</sub></tt> is a significance measure.
	 * <p>
	 * <p>
	 * <b>PLEASE NOTE:</b> There is an error in the way Concise implements contingency table. 
	 * See {@link #Collocate(String, long, long, long, long, long[], long[], long)} 
	 * or {@link #getLogLikelihood()} for detailed explanation.
	 * 
	 * @return		Yates' continuity-corrected version of chi-squared.
	 */
	public double getChiSquaredCorr() {
		return chiSquaredCorr;
	}
	
	/**
	 * Returns Dunning's (1993) log-likelihood ratio.
	 * <p><tt>log-likelihood = 2&Sigma;O<sub>ij</sub>log(O<sub>ij</sub>/E<sub>ij</sub>)</tt>
	 * <p><tt>log-likelihood</tt> is a significance measure.
	 * <p>
	 * <p>
	 * <b>PLEASE NOTE:</b> There is an error in the way Concise implements Dunning's log-likelihood ratio.
	 * In principle, the calculation should be strictly binary and the above formula therefore does not contains
	 * the variable "window span".  This is best illustrated with an example:
	 * <p>
	 * <blockquote>He said <i>please please please</i> <b>leave</b> me alone.</blockquote>
	 * <p>
	 * Here, the lexical item <i>please</i> co-occurs with <i>leave</i> three times within a window span of 
	 * -3 to +3 words.  In theory, the collocate <i>please</i> should be only counted once.  However, Concise 
	 * counts three occurrences of <i>please</i> in this sentence.  As a result, the collocational strength of
	 * items which co-occur repeatedly with the node item will be higher than it should be.  In extreme cases,
	 * where the collocate is found more often than the number of the running text containing the node-collocate
	 * pair, this will result in a mathematical error (calculation of logarithm of a negative number).  These
	 * collocates will be displayed in the collocation table but no collocation value is given.
	 * 
	 * @return		log-likelihood.
	 */
	public double getLogLikelihood() {
		return logLikelihood;
	}
	
	/**
	 * Returns the surface co-occurrences of the (node, collocate) pair.
	 * @return		the surface co-occurrences of the (node, collocate) pair.
	 */
	public long getFreq() {
		return signatureO;
	}
	
	public long lFreq;
	public long rFreq;
	public long nFreq;
	
	public long getLeftFreq() {
		return lFreq;
	}

	public void setLeftFreq(long lFreq) {
		this.lFreq = lFreq;
	}

	public long getRightFreq() {
		return rFreq;
	}

	public void setRightFreq(long rFreq) {
		this.rFreq = rFreq;
	}

	public long getNodeFreq() {
		return nFreq;
	}

	public void setNodeFreq(long nFreq) {
		this.nFreq = nFreq;
	}
	
	public void setPositionVector(String field, long value) {
		positionVector.put(field, value);
	}
	
	public Map<String, Long> getPositionVector() {
		return positionVector;
	}
	
	/**
	 * Returns
	 * @param index
	 * @return
	 */
	public long getCountAtPosition(int index) {
		Long retval;
		if (index < 0) {  // left
			retval = positionVector.get("L"+Math.abs(index));
		}
		else if (index > 0) { // right
			retval = positionVector.get("R"+index);
		}
		else {  // node
			retval = nFreq;
		}
		return retval == null ? 0 : retval;
	}
	
	public long getCountAsPosition(String field) {
		if (positionVector != null && field.matches("[LR]\\d+")) {
			Long count = positionVector.get(field);
			return count == null ? 0 : count;
		}
		return 0;
	}
	
	public int getLeftSpanSize() {
		int count = 0;
		for (String key : positionVector.keySet()) {
			if (key.startsWith("L"))
				count++;
		}
		return count;
	}
	
	public int getRightSpanSize() {
		return positionVector.size() - getLeftSpanSize();
	}
	
	
}
