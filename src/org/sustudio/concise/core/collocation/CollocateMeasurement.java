package org.sustudio.concise.core.collocation;

public enum CollocateMeasurement {
	
	Cooccurrence	(5),
	MI				(2),
	MI3				(8),
	Dice			(2),
	OddsRatio		(2),
	TScore			(1.96),
	ZScore			(1.96),
	SimpleLL		(3.84),
	ChiSquaredCorr	(1.96),
	LogLikelihood	(1.96),
	;

	private double defaultCutOff;
	CollocateMeasurement(double defaultCutOff) {
		this.defaultCutOff = defaultCutOff;
	}
	
	public double getValue(Collocate coll) {
		switch (this) {
		case Cooccurrence:		return coll.getFreq();
		case MI:				return coll.getMI();
		case MI3:				return coll.getMI3();
		case Dice:				return coll.getDice();
		case OddsRatio:			return coll.getOddsRatio();
		case SimpleLL:			return coll.getSimpleLL();
		case TScore:			return coll.getTscore();
		case ZScore:			return coll.getZscore();
		case ChiSquaredCorr:	return coll.getChiSquaredCorr();
		case LogLikelihood:		return coll.getLogLikelihood();
		
		default:		return 0d;
		}
	}
	
	public double defaultCutOff() {
		return defaultCutOff;
	}
	
	public static String[] stringValues() {
		String[] str = new String[values().length];
		for (int i=0; i<values().length; i++) {
			str[i] = values()[i].name();
		}
		return str;
	}
}
