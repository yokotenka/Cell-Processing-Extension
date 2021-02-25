package com.Kenta;

import org.controlsfx.control.action.Action;
import qupath.lib.gui.ActionTools;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.extensions.QuPathExtension;
import qupath.lib.gui.tools.MenuTools;

/**
 * QuPath extension for SPIAT Threshold
 */
public class SPIATThresholdExtension implements QuPathExtension {

    @Override
    public void installExtension(QuPathGUI quPathGUI) {
        Action spiatThresholdAction = ActionTools.createAction(new ThresholdSPIATWindow(quPathGUI), "SPIAT Threshold");

        MenuTools.addMenuItems(
                quPathGUI.getMenu("Extensions>Phenotype Pipeline", true),
                spiatThresholdAction);

        Action phenotypeAction = ActionTools.createAction(new PhenotypeWindow(quPathGUI), "Phenotype");
        MenuTools.addMenuItems(
                quPathGUI.getMenu("Extensions>Phenotype Pipeline", true),
                phenotypeAction
        );

    }

    @Override
    public String getName() {
        return "SPIAT Threshold";
    }

    @Override
    public String getDescription() {
        return "Implementation of the SPIAT predict_phenotype.R script in groovy";
    }
}
