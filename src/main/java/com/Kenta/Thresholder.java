package com.Kenta;

import javafx.scene.chart.Chart;
import qupath.lib.objects.PathObject;

import java.util.Collection;
import java.util.HashMap;




public abstract interface Thresholder {

    /**
     * Calculates the thresholds
     * @return
     */
    public abstract double[] calculateThresholds();

}
