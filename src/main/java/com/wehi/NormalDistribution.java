package com.wehi;
import org.apache.commons.math3.util.FastMath;


/** Represents the normal distribution. Reason for creating own class is because
 * org.apache.commons.math3.distribution.NormalDistribution did not have a method which returns an array of densities.
 * It was faster making a new class which does this.
 * @author Kenta
 */
public class NormalDistribution {
    // The mean of the normal distribution
    private double mean = 0;
    // The standard deviation of the normal distribution
    private double standardDeviation = 1;


    /** Constructor
     * @param mean mean for the normal distribution
     * @param standardDeviation standard deviation of normal distribution
     */
    public NormalDistribution(double mean, double standardDeviation){
        this.mean = mean;
        this.standardDeviation = standardDeviation;
    }

    /** Constructor only with the standard deviation
     * @param standardDeviation
     */
    public NormalDistribution(double standardDeviation){
        this.standardDeviation = standardDeviation;
    }

    /**
     * Getter for the density of a given range
     * @param min lower bound of range
     * @param max upper bound of range
     * @param n the number of bins
     * @return the density in that range
     */
    double[] getDensityOfRange(double min, double max , int n){
        double[] densityArray = new double[n];

        double increment = (max-min)/ n;
        double currentPoint = min;

        for (int i=0; i<n; i++){
            densityArray[i] = density(currentPoint);
            currentPoint += increment;
        }
        return densityArray;
    }

    /** Calculates the density at a specific point
     @param x The point at which the density function is being evaluated at.
     */
    double density(double x){
        double x0 = (x - mean) / standardDeviation;
        double exponent = -0.5 * x0 * x0;
        return FastMath.exp(exponent) / (standardDeviation * FastMath.sqrt(2 * Math.PI));
    }

    /** Setter for the mean
     * @param mean The new mean for the normal distribution.
     */
    void setMean(double mean) {
        this.mean = mean;
    }

    /** Setter for the standard deviation
     * @param standardDeviation
     */
    void setStandardDeviation(double standardDeviation){
        this.standardDeviation = standardDeviation;
    }
}