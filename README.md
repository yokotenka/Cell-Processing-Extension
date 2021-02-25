# Cell-Processing-Extension

## Aim
A pipeline to phenotype cells by calculating and applying a threshold to the marker intensities within a cell. 

## How to use

### SPIAT Threshold
1. On the options table, select the marker you would like to apply the threshold on. 
2. A baseline marker is a marker which is not present in tumour cells. For example, CD45, CD3, CD8 etc are not expressed in tumour cells. Hence we would select these as baseline markers. Select all markers which should not be expressed in tumour cells as baseline markers.
3. Choose where the marker is expressed. eg. Cell, Cytoplasm, Membrane etc. Select tumour marker. 
4. Press run to see the results
5. Selecting the marker in the results selection box will highlight cells which are positive to that marker on the image. 
6. You can save and apply the classifiers and also save the displayed statistics as a csv file. 

### Phenotype
1. Select any composite classifier (can be one made manually using built in qupath methods or ones made by the previous step).    
2. (Optional) if you have previously filled out the options table and saved it, you can load it in to avoid the hassle of pressing many buttons.   
3. Type the name of your phenotype.   
4. Select a marker which is positive in that phenotype, and press the "+" button. If you want to add another marker, simply select another marker in the drag down box and press "+". If you want to remove a particular marker, select that marker in the drag down box and press "-".    
5. Same applies for negative markers.   
6. (Optional) You can save all your hard work of inputting the phenotypes by saving it. It will create a folder in your project directory and save it there. It is recommened that you save the options if you having images from the same experiment and are using the same markers.   
7. Run the phenotype decider.   
8. (Optional) Can save the results table. Not much information other that the count and combination of markers.    

## References
SPIAT: https://www.biorxiv.org/content/10.1101/2020.05.28.122614v1  
Nina Tubau's phenotype method: https://github.com/ninatubau/QuPath_scripts/blob/main/scripts/gui_phenotype_add_col.groovy  

## Potential bugs:
- NullPointer exception when new project is opened. Please ignore
- when only a small ROI is considered, sometimes it cannot find a threshold and will throw an error. If you encounter this error, try and make the ROI larger. If this doesn't work, deselect the marker which is causing the problem. 

