package com.Kenta;

import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;

/**
 * The entry for the options table. Will contain the checkBoxes and comboBoxes
 */
public class MarkerTableEntry {
    // Check box indicating whether the marker is selected or not
    private CheckBox isSelectedForThreshold;
    // Check box indicating whether the marker is a baseline marker
    private final CheckBox isBaselineMarker;
    // The selected measurement to be used
    private ComboBox<String> measurement;

    // The SPIATMarkerInformation
    private SPIATMarkerInformation markerInfo;

    /**
     * Constructor which will be used while populating options table
     * @param name Name of the marker
     * @param isSelected boolean value indicating whether the marker is selected for thresholding
     * @param isBaselineMarker boolean value indicating whether marker is baseline marker
     * @param measurementList List of measurements
     * @param defaultValue Default value for the measurement comboBox
     */
    public MarkerTableEntry(String name, boolean isSelected, boolean isBaselineMarker,
                            ObservableList<String> measurementList, String defaultValue){

        this.markerInfo = new SPIATMarkerInformation(name);
        this.isSelectedForThreshold = new CheckBox();
        this.isSelectedForThreshold.setSelected(isSelected);
        this.isBaselineMarker = new CheckBox();
        this.isBaselineMarker.setSelected(isBaselineMarker);
        this.measurement = new ComboBox<>(measurementList);
        measurement.setValue(defaultValue);
    }


    // Getters needed for PropertyValueFactory<>() during table creation
    /**
     * Getter for the name of the marker
     * @return marker name
     */
    public String getName() {
        return markerInfo.getMarkerName();
    }

    /**
     * Getter for boolean value of whether selected for threshold
     * @return isSelectedForThreshold
     */
    public CheckBox getIsSelectedForThreshold() {
        return isSelectedForThreshold;
    }

    /**
     * Getter for the measurement used for the threshold
     * @return ComboBox with all the options for the measurements
     */
    public ComboBox getMeasurement() {
        return measurement;
    }

    /**
     * Getter for the baseline marker
     * @return
     */
    public CheckBox getIsBaselineMarker() {
        return isBaselineMarker;
    }

    /**
     * Getter for the threshold
     * @return Threshold value for the marker
     */
    public double getThreshold() {
        return markerInfo.getThreshold();
    }

    /**
     * Getter for the proportion of the cells which are positive for the marker
     * @return proportion
     */
    public double getProportion() {
        return markerInfo.getExpressionProportion();
    }

    /**
     * Count of the number of cells which are positive for the marker
     * @return count of cells
     */
    public int getCount(){return markerInfo.getCount();}

    /**
     * Getter for the SPIATMarkerInfo
     * @return SPIATMarkerInfo
     */
    public SPIATMarkerInformation getMarkerInfo() {
        return markerInfo;
    }

    // Setters

    /**
     * Sets name
     * @param name name of marker
     */
    public void setName(String name) {
        this.markerInfo.setMarkerName(name);
    }

    /**
     * Setter for whether marker is selected or not
     * @param isSelectedForThreshold boolean for whether the marker is selected for threshold
     */
    public void setIsSelectedForThreshold(CheckBox isSelectedForThreshold) {
        this.isSelectedForThreshold = isSelectedForThreshold;
    }

    /**
     * Setter for the measurement comboBox
     * @param measurement Measurement the user wants to use
     */
    public void setMeasurement(ComboBox measurement) {
        this.measurement = measurement;
    }

    /**
     * Setter for the measurement name to be used.
     * @return Boolean value which indicates whether it was successful at changing measurement name or not
     */
    public boolean setMeasurementName(){
        this.markerInfo.setMeasurementName(this.getName()+": "+measurement.getValue());
        if (measurement.getValue() != null)
            return true;
        return false;
    }

    /**
     * Setter for the threshold
     * @param threshold The new threshold value
     */
    public void setThreshold(double threshold) {
        this.markerInfo.setThreshold(threshold);
    }

    /**
     * Setter for the proportion
     * @param proportion The new proportion value
     */
    public void setProportion(double proportion) {
        this.markerInfo.setExpressionProportion(proportion);
    }

    // For my use

    /**
     * Gets whether the marker is selected or not
     * @return boolean value
     */
    public boolean isSelected() {
        return isSelectedForThreshold.isSelected();
    }

    /**
     * Gets whether the marker is a baseline marker or not
     * @return boolean value
     */
    public boolean isBaselineMarker(){
        return isBaselineMarker.isSelected();
    }

    /**
     * Gets the comboBox value
     * @return Measurement name
     */
    public String getMeasurementName(){
        return measurement.getValue();
    }
}