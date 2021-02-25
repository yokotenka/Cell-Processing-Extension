# Cell-Processing-Extension

## Aim
Implements the SPIAT threshold method for multiplexed immunohistochemistry marker intensities in the function predict_phenotypes.R

## How to use
1. On the options table, select the marker you would like to apply the threshold on. 
2. A baseline marker is a marker which is not present in tumour cells. For example, CD45, CD3, CD8 etc are not expressed in tumour cells. Hence we would select these as baseline markers. Select all markers which should not be expressed in tumour cells as baseline markers.
3. Choose where the marker is expressed. eg. Cell, Cytoplasm, Membrane etc. Select tumour marker. 
4. Press run to see the results
5. Selecting the marker in the results selection box will highlight cells which are positive to that marker on the image. 
6. You can save and apply the classifiers and also save the displayed statistics as a csv file. 

## References
SPIAT: https://www.biorxiv.org/content/10.1101/2020.05.28.122614v1  
Nina Tubau's phenotype method: https://github.com/ninatubau/QuPath_scripts/blob/main/scripts/gui_phenotype_add_col.groovy  

## Potential bugs:
- NullPointer exception when new project is opened. Please ignore
- when only a small ROI is considered, sometimes it cannot find a threshold and will throw an error. If you encounter this error, try and make the ROI larger. If this doesn't work, deselect the marker which is causing the problem. 

