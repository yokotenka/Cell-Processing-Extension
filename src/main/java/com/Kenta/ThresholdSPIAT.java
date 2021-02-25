package com.Kenta;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.classes.PathClassFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ThresholdSPIAT implements Thresholder{

    /** The intensities at which the density values will be present*/
    private final double[] x;
    /** Number of points in x */
    private final int n = 1024;

    /** ArrayList containing all the baseline markers*/
    private ArrayList<SPIATMarkerInformation> baselineMarkers;
    /** The tumour marker*/
    private final SPIATMarkerInformation tumourMarker;
    /** Hashmap containing all the marker information*/
    private HashMap<String, SPIATMarkerInformation> markerInformationMap = new HashMap<>();
    /** Cells */
    private final Collection<PathObject> cells;

    /**
     * Constructor
     * @param cells The cell objects
     * @param markers The markers
     * @param baselineMarkers The baseline markers. I.e. the markers not present in the tumour cells
     * @param tumourMarker The tumour marker
     */
    public ThresholdSPIAT(Collection<PathObject> cells, ArrayList<SPIATMarkerInformation> markers,
                          ArrayList<SPIATMarkerInformation> baselineMarkers, SPIATMarkerInformation tumourMarker){
        this.cells = cells;
        this.tumourMarker = tumourMarker;

        // Populate marker hashmap
        for (SPIATMarkerInformation markerInformation : markers){
            markerInformationMap.put(markerInformation.getMarkerName(), markerInformation);
        }

        // Populate baseline marker array
        this.baselineMarkers = baselineMarkers;


        // Initialise state space for probability density function
        this.x = new double[n];
        x[0] = 0;
        double MAX_INTENSITY = 255;
        double MIN_INTENSITY = 0;
        double increment = (MAX_INTENSITY - MIN_INTENSITY) / n;
        for (int i=1; i<n; i++){
            x[i] = x[i - 1] + increment;
        }
    }

    /**
     * Calculates thresholds of all the given markers
     */
    @Override
    public double[] calculateThresholds(){

        for (SPIATMarkerInformation marker : markerInformationMap.values()){
            calculateThresholdForSingleMarker(marker);
        }
        return markerInformationMap.values().stream()
                                                .mapToDouble(SPIATMarkerInformation::getThreshold)
                                                .toArray();
    }

    /**
     * Calculates the threshold for a single marker
     * @param marker Marker information
     * @return double the threshold
     */
    private double calculateThresholdForSingleMarker(SPIATMarkerInformation marker){

        // Return if marker has been calculated already
        if (marker.getThreshold() > -1){
            return marker.getThreshold();
        }

        // Get the marker name
        String markerName = marker.getMarkerName();

        // Get the measurement name
        String columnName = marker.getMeasurementName();

        // Get the marker intensities from spreadsheet
        double[] markerIntensities = cells.stream()
                .parallel()
                .mapToDouble(p -> p.getMeasurementList().getMeasurementValue(columnName))
                .filter(d -> !Double.isNaN(d)).toArray();

        // Get the max intensity
        marker.setMaxIntensity(findMax(markerIntensities).getValue());

        // Instantiate KernelDensityEstimation class
        KernelDensityEstimation kernelDensityEstimation = new KernelDensityEstimation(markerIntensities, 0, 255, n);

        // Estimate
        double[] estimate = kernelDensityEstimation.estimate();

        // Find local minima of the density function (inflection of the distribution function)
         ArrayList<Integer> minimaIndex = findLocalMinimaIndex(estimate);

        // Check that we have a minima
        assert (minimaIndex.size() > 0): "Minima not detected for " + markerName;
        // Make sure the threshold list is not empty
//            assert (thresholdList.size().isEmpty): "Threshold list empty for " + markerName


        // Get the maximum density
        ValueIndexPair maxDensity = findMax(estimate);
        marker.setMaxDensity(maxDensity.getValue());

        double threshold = -1;

        // For tumour marker
        if (tumourMarker != null && markerName.equals(tumourMarker.getMarkerName())) {
            // Calculations for the tumour marker
            double[] nanIncludedMarkerIntensity = cells.stream().parallel()
                    .mapToDouble(p -> p.getMeasurementList().getMeasurementValue(columnName)).toArray();

            List<Integer> filteredIntensityIndices = new ArrayList<>();

            // Iterate through the baseline markers
            for (SPIATMarkerInformation baselineMarker : this.baselineMarkers){
                String baselineColumnName = baselineMarker.getMeasurementName();

                // Get the baseline marker intensities
                double[] baselineMarkerIntensities = cells.stream().parallel()
                        .mapToDouble(p -> p.getMeasurementList().getMeasurementValue(baselineColumnName)).toArray();


                // Get the baseline marker threshold
                double baselineThreshold = calculateThresholdForSingleMarker(baselineMarker);

                for (int i=0; i < cells.size(); i++){
                    if (!Double.isNaN(baselineMarkerIntensities[i]) && !Double.isNaN(nanIncludedMarkerIntensity[i])){
                        if (baselineMarkerIntensities[i] > baselineThreshold) {
                            if (!filteredIntensityIndices.contains(i)) {
                                filteredIntensityIndices.add(i);
                            }
                        }
                    }
                }
            }

            double[] filteredIntensities = new double[filteredIntensityIndices.size()];

            for (int j=0; j<filteredIntensityIndices.size(); j++){
                int inde = filteredIntensityIndices.get(j);
                double num = nanIncludedMarkerIntensity[inde];
                filteredIntensities[j] = num;
            }
            // Get the 95th percentile for all cells which are positive for the baseline cells
            DescriptiveStatistics stats = new DescriptiveStatistics(filteredIntensities);
            double cutOffForTumour = stats.getPercentile(95);



            // get the threshold for the tumour marker
            for (int index : minimaIndex) {
                if (getValueAtX(index) < getValueAtX(maxDensity.getIndex())) {
                    continue;
                }
                if (Double.isNaN(cutOffForTumour)){
                    threshold = getValueAtX(index);
                    break;
                }

                // Get the first threshold and compare to the cutOffForTumour.
                if (Double.compare(getValueAtX(index), cutOffForTumour) <= 0) {
                    threshold = getValueAtX(index);
                } else {
                    threshold = cutOffForTumour;
                }
                break;
            }
        }
        // For non-tumour marker
        else {
            // 25% of the max density
            double upperThreshold = maxDensity.getValue() * 0.25;
            // Filter the threshold list
            for (Integer index : minimaIndex) {
                // Get threshold which is larger than the intensity with the max density, and less than 25% of the
                // max density.
                if (Double.compare(estimate[index], (upperThreshold)) <= 0 &&
                        Double.compare(getValueAtX(index), getValueAtX(maxDensity.getIndex())) >= 0) {
                    threshold = getValueAtX(index);
                    break;
                }
            }
        }

        // Add to the density array list
        marker.setEstimatedDensity(estimate);
        marker.setThreshold(threshold);

        // Get the percentage expression of the marker
        double finalThreshold = threshold;
        long count = Arrays.stream(markerIntensities).parallel().filter(markerIntensity -> Double.compare(markerIntensity, finalThreshold) >0).count();
        marker.setExpressionProportion(((double) count / markerIntensities.length));
        marker.setCount((int) count);

        // return the threshold
        return threshold;
    }

    /**
     * Finds the local minima indeces of an array
     * ###### Probably not the best way to do it
     * @param arr double array
     * @return array list of indices of where local minima are
     */
    private ArrayList<Integer> findLocalMinimaIndex(double[] arr) {

        // Empty vector to store points of
        // local maxima and minima
        ArrayList<Integer> mn = new ArrayList<>();
        int n = arr.length;

        // Iterating over all points to check
        // local maxima and local minima
        for(int i = 1; i < arr.length - 1; i++) {
            // Condition for local minima
            if (Double.compare(arr[i - 1], (arr[i])) > 0 &&
                    Double.compare(arr[i], arr[i + 1]) < 0) {
                mn.add(i);
            }
        }

        // Checking whether the last point is
        // local maxima or minima or none
        if (Double.compare(arr[n - 1], arr[n - 2]) < 0){
            mn.add(n - 1);
        }
        return mn;
    }

    /**
     * Finds the maximum value in an array with the index
     * @param arr array of doubles
     * @return the value, index pair
     */
    private ValueIndexPair findMax(double[] arr){
        double max = -1;
        int index = 0;
        for (int i=0; i<arr.length; i++){
            if (arr[i] > max){
                max = arr[i];
                index = i;
            }
        }
        return new ValueIndexPair(max, index);
    }

    /** Utility class to find a value in an array along with index
     */
    private static class ValueIndexPair{
        private final double value;
        private final int index;

        public ValueIndexPair(double value, int index){
            this.value = value;
            this.index = index;
        }

        // Gets the value
        public double getValue() {
            return value;
        }

        // Getter for index of that value
        public int getIndex() {
            return index;
        }
    }

    /**
     * Gets the array x
     * @return gets the x array
     */
    public double[] getX() {
        return x;
    }

    /**
     * Gets the value of x and the given index i
     * @param i the ith position
     * @return the ith position in x
     */
    public double getValueAtX(int i){
        return x[i];
    }

    /**
     * Gets the marker information map
     * @return gets the information map
     */
    public HashMap<String, SPIATMarkerInformation> getMarkerInformationMap() {
        return markerInformationMap;
    }
}
