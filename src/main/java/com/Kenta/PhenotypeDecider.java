package com.Kenta;

import javafx.collections.ObservableList;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.classes.PathClass;
import qupath.lib.objects.classes.PathClassFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

import static qupath.lib.objects.classes.PathClassFactory.getPathClass;

public class PhenotypeDecider {
    private Collection<PathObject> cells;
    private ObservableList<PhenotypeTableEntry> phenotypeCriteriaList;
    private PhenotypeTableEntry undefined;

    public PhenotypeDecider(Collection<PathObject> cells, ObservableList<PhenotypeTableEntry> phenotypeList, PhenotypeTableEntry undefined){
        this.cells = cells;
        this.phenotypeCriteriaList = phenotypeList;
        this.undefined = undefined;
    }

    public void decide(){

        PathClass nothing = getPathClass("Undefined");
        for (PathObject cell : cells){
            boolean cell_undefined = true;

            if (cell.getPathClass() != null) {
                String str_class = cell.getPathClass().toString();
                cell.setPathClass(null);

                for (PhenotypeTableEntry phenotype : phenotypeCriteriaList) {
                    boolean phenotype_valid = true;


                    for (String negativeMarker : phenotype.getNegativeMarkerArray()) {
                        if (str_class.contains(negativeMarker)) {
                            phenotype_valid = false;
                            break;
                        }
                    }

                    for (String positiveMarker : phenotype.getPositiveMarkerArray()) {
                        if (!str_class.contains(positiveMarker)) {
                            phenotype_valid = false;
                            break;
                        }
                    }

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
                    }
                }
            }

            if (cell_undefined){
                cell.setPathClass(nothing);
                undefined.incrementCount();
            }
        }
    }

}
