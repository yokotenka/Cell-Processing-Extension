package com.Kenta;
import javafx.scene.chart.XYChart.Series;

/**
 * For storing all the information required to perform the thresholding on the marker
 */
public class SPIATMarkerInformation {
    private String markerName;
    private String measurementName;
    private boolean isTumour;
    private double maxIntensity;
    private double maxDensity;
    private double threshold = -1;
    private double[] estimatedDensity;
    private double expressionProportion;
    private int count;

    // The series which shows the kernel density estimation
    private Series<Number, Number> mainSeries;

    /**
     * Constructor
     * @param markerName Marker name
     * @param measurementName Measurement to be used in thresholding
     * @param isTumour indicated whether it marks tumour cells
     */
    public SPIATMarkerInformation(String markerName, String measurementName, boolean isTumour){
        this.markerName = markerName;
        this.measurementName = measurementName;
        this.isTumour = isTumour;
        mainSeries = new Series<>();
    }

    /**
     * Constructor
     * @param markerName Marker name
     */
    public SPIATMarkerInformation(String markerName){
        this.markerName = markerName;
    }

    // Setters

    /**
     * Sets the marker name
     * @param markerName marker name
     */
    public void setMarkerName(String markerName) {
        this.markerName = markerName;
    }

    /**
     * Sets whether it is a tumour marker or not
     * @param tumour boolean value indicating whether tumour or not
     */
    public void setIsTumour(boolean tumour) {
        isTumour = tumour;
    }

    /**
     * Setter for measurement name
     * @param measurementName measurement name
     */
    public void setMeasurementName(String measurementName) {
        this.measurementName = measurementName;
    }

    /**
     * Sets the max intensity observed
     * @param intensity maximum intensity observed in the measurement
     */
    public void setMaxIntensity(double intensity){
        this.maxIntensity = intensity;
    }

    /**
     * Max density value in the KDE
     * @param density max density
     */
    public void setMaxDensity(double density){
        this.maxDensity = density;
    }

    /**
     * Setter for the threshold value
     * @param threshold threshold which was calculated
     */
    public void setThreshold(double threshold){
        this.threshold = threshold;
    }

    /**
     * Setter for the estimated density
     * @param estimatedDensity The kernel density estimation
     */
    public void setEstimatedDensity(double[] estimatedDensity){
        this.estimatedDensity = estimatedDensity;
    }

    /**
     * Setter for the proportion of cells which express the marker
     * @param expressionProportion the proportion of cells expressing this marker
     */
    public void setExpressionProportion(double expressionProportion) {
        this.expressionProportion = expressionProportion;
    }

    /**
     * Setter for the main series
     * @param series the series of a line chart
     */
    public void setMainSeries(Series series){
        this.mainSeries = series;
    }

    /**
     * Setter for the count of cells
     * @param count number of cells which are positive for the marker
     */
    public void setCount(int count){
        this.count = count;
    }

    // Getters

    /**
     * Getter for the count
     * @return count
     */
    public int getCount(){
        return count;
    }

    /**
     * Getter for the expression proportion
     * @return expression proportion
     */
    public double getExpressionProportion() {
        return expressionProportion;
    }

    /**
     * Getter for the marker name
     * @return marker name
     */
    public String getMarkerName() {
        return markerName;
    }

    /**
     * Getter for the max intensity observed
     * @return maxIntensity
     */
    public double getMaxIntensity(){
        return maxIntensity;
    }

    /**
     * Getter for the max density in the kde
     * @return maxDensity
     */
    public double getMaxDensity() {
        return maxDensity;
    }

    /**
     * Getter for the threshold of the marker
     * @return threshold
     */
    public double getThreshold() {
        return threshold;
    }

    /**
     * Getter for the measurement name
     * @return measurementName
     */
    public String getMeasurementName() {
        return measurementName;
    }

    /**
     * Getter for the estimated density
     * @return array containing values for the kde
     */
    public double[] getEstimatedDensity() {
        return estimatedDensity;
    }

    /**
     * Getter for the series for line chart
     * @return mainSeries
     */
    public Series<Number, Number> getMainSeries() {
        return mainSeries;
    }


    /**
     * Is tumour or not
     * @return isTumour
     */
    public boolean isTumour() {
        return isTumour;
    }
}
