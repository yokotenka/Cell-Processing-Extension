package com.wehi;

import javafx.collections.ObservableList;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.classes.PathClass;
import qupath.lib.objects.classes.PathClassFactory;

import java.util.Collection;

import static qupath.lib.objects.classes.PathClassFactory.getPathClass;

/**
 * Class which decides what phenotype each cell should be assigned. The main driver of this code was taken from
 * Nina Tubau at https://github.com/ninatubau/QuPath_scripts/blob/main/scripts/gui_phenotype.groovy
 */
public class PhenotypeDecider {
    // Cells to be processed
    private Collection<PathObject> cells;
    // The options from the options table
    private ObservableList<PhenotypeTableEntry> phenotypeCriteriaList;
    // The undefined cell type
    private PhenotypeTableEntry undefined;

    /**
     * Constructor for deciding cells
     * @param cells cells
     * @param phenotypeList options
     * @param undefined undefined cell
     */
    public PhenotypeDecider(Collection<PathObject> cells, ObservableList<PhenotypeTableEntry> phenotypeList, PhenotypeTableEntry undefined){
        this.cells = cells;
        this.phenotypeCriteriaList = phenotypeList;
        this.undefined = undefined;
    }

    /**
     * Decider based off Nina's gui_phenotype.groovy
     */
    public void decide(){
        // Default path class
        PathClass nothing = getPathClass("Undefined");

        // Iterate through each cell
        for (PathObject cell : cells){
            boolean cell_undefined = true;

            // Check that the cell is not empty
            if (cell.getPathClass() != null) {
                String str_class = cell.getPathClass().toString();
                cell.setPathClass(null);

                // Get measurement list
                var measurementList = cell.getMeasurementList();

                // Iterate through each phenotype
                for (PhenotypeTableEntry phenotype : phenotypeCriteriaList) {
                    boolean phenotype_valid = true;

                    // Check that negative markers do not appear in the cell
                    for (String negativeMarker : phenotype.getNegativeMarkerArray()) {
                        if (str_class.contains(negativeMarker)) {
                            phenotype_valid = false;
                            break;
                        }
                    }

                    // Check that positive markers are in the cell
                    for (String positiveMarker : phenotype.getPositiveMarkerArray()) {
                        if (!str_class.contains(positiveMarker)) {
                            phenotype_valid = false;
                            break;
                        }
                    }

                    // Set the default as being not that phenotype
                    measurementList.putMeasurement(phenotype.getPhenotypeName(), 0);

                    // If the phenotype is valid set the path class and make the table entry 1
                    if (phenotype_valid) {
                        PathClass currentClass = cell.getPathClass();
                        PathClass pathClass;

                        if (currentClass == null) {
                            pathClass = PathClassFactory.getPathClass(phenotype.getPhenotypeName());
                        } else {
                            pathClass = PathClassFactory.getDerivedPathClass(
                                    currentClass,
                                    phenotype.getPhenotypeName(),
                                    null);
                        }
                        cell.setPathClass(pathClass);
                        phenotype.incrementCount();
                        cell_undefined = false;
                        measurementList.putMeasurement(phenotype.getPhenotypeName(), 1);
                    }
                }
            }

            // For undefined cell types
            if (cell_undefined){
                cell.setPathClass(nothing);
                undefined.incrementCount();
            }
        }
    }

}
