package com.wehi;

import org.controlsfx.control.action.Action;
import qupath.lib.gui.ActionTools;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.extensions.QuPathExtension;
import qupath.lib.gui.tools.MenuTools;

/**
 * QuPath extension for Phenotype
 */
public class PhenotypePipelineExtension implements QuPathExtension {

    @Override
    public void installExtension(QuPathGUI quPathGUI) {
        Action spiatThresholdAction = ActionTools.createAction(new ThresholdSPIATWindow(quPathGUI), "SPIAT Threshold");

        MenuTools.addMenuItems(
                quPathGUI.getMenu("Extensions>Phenotype Pipeline>Threshold", true),
                spiatThresholdAction);

        Action phenotypeAction = ActionTools.createAction(new PhenotypeWindow(quPathGUI), "Phenotype");
        MenuTools.addMenuItems(
                quPathGUI.getMenu("Extensions>Phenotype Pipeline", true),
                phenotypeAction
        );

    }

    @Override
    public String getName() {
        return "Phenotype Pipeline";
    }

    @Override
    public String getDescription() {
        return "Marker Threshold and phenotype";
    }
}
