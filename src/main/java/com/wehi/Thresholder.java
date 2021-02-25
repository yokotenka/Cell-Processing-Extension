package com.wehi;


public abstract interface Thresholder {

    /**
     * Calculates the thresholds
     * @return
     */
    public abstract double[] calculateThresholds();

}
