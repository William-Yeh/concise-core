package org.sustudio.concise.core.statistics;

public class PlotData<T> {

	private final T type;
	private final double xCoordinate;
	private final double yCoordinate;
	
	public PlotData(T type, double xCoordinate, double yCoordinate) {
		this.type = type;
		this.xCoordinate = xCoordinate;
		this.yCoordinate = yCoordinate;
	}
	
	public T get() {
		return type;
	}
	
	public double getX() {
		return xCoordinate;
	}
	
	public double getY() {
		return yCoordinate;
	}
	
	public String toString() {
		return type.toString() + " (" + xCoordinate + "," + yCoordinate + ")";
	}
	
}
