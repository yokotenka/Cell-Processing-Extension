package com.Kenta;


// Imports
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.slf4j.Marker;
import qupath.lib.classifiers.object.ObjectClassifier;
import qupath.lib.classifiers.object.ObjectClassifiers;
import qupath.lib.classifiers.object.ObjectClassifiers.ClassifyByMeasurementBuilder;
import qupath.lib.common.GeneralTools;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.dialogs.Dialogs;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.classes.PathClass;
import qupath.lib.objects.classes.PathClassFactory;
import qupath.lib.projects.Project;
import qupath.lib.projects.Projects;
import static qupath.lib.scripting.QP.*;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Class which controls the GUI presented upon clicking the extension in QuPath gui
 */
public class ThresholdSPIATWindow extends AbstractWindow implements Runnable{

    // The stage of the javafx
    private Stage dialog;
    // The instance of the qupath gui the extension is called from
    private final QuPathGUI qupath;
    // Currently opened viewer in qupath


    private ThresholdSPIATWindowPane pane;


    /**
     * Constructor for the ThresholdSPIATWindow class
     * @param qupath an instance of the qupath gui
     */
    public ThresholdSPIATWindow(QuPathGUI qupath) {
        this.qupath = qupath;
    }


    /**
     * The run() method which will be called
     */
    @Override
    public void run() {
        // Need to add in way to check for opened windows
//
        var viewer = qupath.getViewer();

        if (viewer == null){
            Dialogs.showErrorMessage("SPIAT Threshold", "Viewer is empty. Please open an image.");
            return;
        }
        if (qupath.getProject() == null){
            Dialogs.showErrorMessage("SPIAT Threshold", "Project is empty. Please open a project.");
            return;
        }
        if (qupath.getImageData() == null){
            Dialogs.showErrorMessage("SPIAT Threshold", "Image data is empty");
            return;
        }
        if (qupath.getImageData().getHierarchy().getCellObjects().isEmpty()){
            Dialogs.showErrorMessage("SPIAT Threshold",
                    "No Cells are detected. Must have cell detections to run SPIAT");
            return;
        }
//        var pane = paneMap.get(viewer);
        if (pane == null) {
            pane = new ThresholdSPIATWindowPane(qupath, viewer);
//            paneMap.put(viewer, pane);
        }
        pane.show();
    }


    static class ThresholdSPIATWindowPane implements ChangeListener<ImageData<BufferedImage>> {
        private QuPathGUI qupath;

        private QuPathViewer viewer;
        // Image server of qupath
        private ImageServer<BufferedImage> server;
        // Image data of qupath
        private ImageData imageData;
        // Cells in the current image
        private Collection<PathObject> cells;

        // Main grid pane of the stage
        private GridPane gridPane;
        // The LineChart showing the kernel density estimation
        private LineChart<Number, Number> lineChart;
        // Pane containing the line chart
        private StackPane pane;
        // The results table
        private TableCreator<MarkerTableEntry> resultsTable;
        // ComboBox for selecting tumour marker
        private ComboBox<String> tumourBox;
        // ComboBox for selecting results
        private ComboBox<String> comboBoxResults;
        // Button to run the code
        private Button startButton;
        // Text field for the classifier name
        private TextField classifierName;
        // Button to save the classifier
        private Button saveClassifierButton;
        // Text field for the name of the results table
        private TextField resultsTableName;
        // Button to save results table
        private Button saveResultsTableButton;
        // Button to choose the directory of where the table is saved
        private Button chooseButton;
        // Options table
        private TableCreator<MarkerTableEntry> optionsTable;
        // Threshold line
        private Series<Number, Number> thresholdLine;

        // All the different markers in the current image
        private ObservableList<MarkerTableEntry> markers;
        // The list of all the selected markers ################## Probably redundant
        private ArrayList<SPIATMarkerInformation> selectedMarkers;
        // Observable list for the results selection and table
        private ObservableList<MarkerTableEntry> selectedMarkersResults;
        // List for the baseline markers
        private ArrayList<SPIATMarkerInformation> baselineMarkers;
        // Tumour marker
        private SPIATMarkerInformation tumourMarker;
        // Default measurement
        private String defaultValue;
        // Marker measurement
        private List<String> markerMeasurements;
        // Marker names
        private ObservableList<String> markerNames;
        // Column names
        private ObservableList<String> columnNames;

        // Instance of the ThresholdSPIAT class
        private ThresholdSPIAT thresholdSPIAT;

        // The cells which are currently selected
        private Collection<PathObject> currentlySelected;

        // The title of the current window
        private String title = "SPIAT Threshold";


        private Stage dialog;
        private Stage stage;

        private HashMap<String, ObservableList<MarkerTableEntry>> markersMap = new HashMap<>();
        private HashMap<String, ObservableList<MarkerTableEntry>> selectedMarkersResultsMap = new HashMap<>();
        private HashMap<String, ArrayList<SPIATMarkerInformation>> selectedMarkersMap = new HashMap<>();
        private HashMap<String, ThresholdSPIAT> thresholdMap = new HashMap<>();

        public ThresholdSPIATWindowPane(QuPathGUI qupath, QuPathViewer viewer) {
            this.qupath = qupath;
            this.viewer = viewer;
            server = viewer.getServer();
            imageData = viewer.getImageData();
            cells = imageData.getHierarchy().getCellObjects();

            createDialog();
        }

        public void show() {
            viewer.imageDataProperty().addListener(this);
            dialog.show();
        }


        /**
         * Formats and creates the stage which will be showed.
         *
         * @return Stage to be displayed when the extension is callled
         */
        protected Stage createDialog() {

//            updateQuPath();

            updateMeasurements();
            updateMarkers();

            // Create the grid
            //Settings to control the dialog boxes for the GUI
            int col = 0;

            // Row number for the current column
            int row_col0 = 0;
            //        int row_col1 = 0;

            // Initialise the grid
            gridPane = new GridPane();
            gridPane.setVgap(5);
            gridPane.setPadding(new Insets(10, 10, 10, 10));


            // For selecting the markers which threshold will be applied
            gridPane.add(createLabel("Select the markers to apply threshold"), col, row_col0++, 2, 1);
            optionsTable = new TableCreator<>();
            updateOptionsTable();
            optionsTable.addColumn("Marker", "name");
            optionsTable.addColumn("Select", "isSelectedForThreshold");
            optionsTable.addColumn("Select\nBaseline Marker", "isBaselineMarker");
            optionsTable.addColumn("Measurement", "measurement");
            gridPane.add(optionsTable.getTable(), col, row_col0++, 2, 2);


            row_col0++;
            gridPane.add(createLabel("Select tumour marker "), 0, row_col0, 1, 1);
            tumourBox = new ComboBox<>(markerNames);
            gridPane.add(tumourBox, 1, row_col0++, 1, 1);

            startButton = new Button("Run Threshold");
            gridPane.add(startButton, col, row_col0++);

            // Separator
            gridPane.add(new Separator(Orientation.VERTICAL), 2, 0, 1, row_col0++);

            // Results Selection
            int row_col3 = 0;
            gridPane.add(createLabel("Select marker to display results "), 3, row_col3, 2, 1);
            comboBoxResults = new ComboBox<>();
            gridPane.add(comboBoxResults, 5, row_col3++, 2, 1);

            // Add the Results line chart to grid pane
            pane = new StackPane();
            pane.getChildren().add(initialiseLineChart());
            gridPane.add(pane, 3, row_col3++, 4, 1);

            // Results table
            resultsTable = new TableCreator<>();
            resultsTable.addColumn("Marker", "name");
            resultsTable.addColumn("Measurement", "measurementName");
            resultsTable.addColumn("Threshold", "threshold");
            resultsTable.addColumn("Proportion", "proportion");
            resultsTable.addColumn("Count", "count");
//            resultsTable.getTable().setPrefSize(400, 100);
            gridPane.add(resultsTable.getTable(), 3, row_col3++, 4, 1);

            // Save options for the classifier
            gridPane.add(createLabel("Classifier Name "), 3, row_col3, 1, 1);
            classifierName = new TextField("SPIAT_Classifier");
            gridPane.add(classifierName, 4, row_col3, 1, 1);
            saveClassifierButton = new Button("Save & Apply");
            gridPane.add(saveClassifierButton, 5, row_col3++, 2, 1);

            // Save options for the results table
            gridPane.add(createLabel("Table Name "), 3, row_col3, 1, 1);
            resultsTableName = new TextField("SPIAT_Table_Results");
            gridPane.add(resultsTableName, 4, row_col3, 1, 1);
            chooseButton = new Button("Choose");
            gridPane.add(chooseButton, 5, row_col3, 1, 1);
            saveResultsTableButton = new Button("Save");
            gridPane.add(saveResultsTableButton, 6, row_col3++, 1, 1);

            // Action upon pressing the start button
            startButton.setOnAction((event) -> {
                if (imageData == null){
                    Dialogs.showErrorMessage(title, "No images open");
                    return;
                }

                if (cells == null){
                    Dialogs.showErrorMessage(title, "No Cells are detected");
                    return;
                }


                comboBoxResults.setValue(null);
                // Collect the options
                String tumourMarkerName = tumourBox.getValue();
                StringBuilder invalidInputs = new StringBuilder();
                StringBuilder measurementNotSelected = new StringBuilder();

                selectedMarkers = new ArrayList<>();
                selectedMarkersResults = FXCollections.observableArrayList();
                baselineMarkers = new ArrayList<>();

                /*
                 Get the selected markers, baseline markers and the tumour marker.
                 Note: only markers which are selected for thresholding will be included
                 as a baseline marker if selected.
                */
                boolean isBaselineAndTumour = false;
                boolean isInvalid = false;
                for (MarkerTableEntry marker : markers) {
                    if (marker.isSelected()) {
                        selectedMarkers.add(marker.getMarkerInfo());
                        selectedMarkersResults.add(marker);
                    }
                    if (marker.isBaselineMarker()) {
                        baselineMarkers.add(marker.getMarkerInfo());
                    }
                    if (marker.isBaselineMarker() && !marker.isSelected()) {
                        invalidInputs.append(marker.getName()).append(", ");
                        isInvalid = true;
                    }

                    if (marker.getName().equals(tumourMarkerName)) {
                        tumourMarker = marker.getMarkerInfo();
                        if (marker.isBaselineMarker()) {
                            isBaselineAndTumour = true;
                            isInvalid = true;
                        }
                    }
                    if (!marker.setMeasurementName()) {
                        measurementNotSelected.append(marker.getName()).append(", ");
                        isInvalid = true;
                    }
                    marker.setThreshold(-5);
                }

                // Checks
                String errorMsg = "";
                if (!invalidInputs.toString().equals("")) {
                    Dialogs.showErrorMessage(title,
                            "The following markers were selected as baseline marker but not selected for thresholding:\n"
                                    + invalidInputs.subSequence(0, invalidInputs.length() - 2) + "\n");
                }

                if (!measurementNotSelected.toString().equals("")) {
                    Dialogs.showErrorMessage(title, "The following markers did not have a measurement selected: \n"
                            + measurementNotSelected.subSequence(0, measurementNotSelected.length() - 2) + "\n");
                }
                if (isBaselineAndTumour) {
                    Dialogs.showErrorMessage(title,
                            "Tumour is also selected as baseline marker. Cannot be both!\n");
                }
                if (isInvalid) {
                    return;
                }

                // Initialise the threshold algorithm
                thresholdSPIAT = new ThresholdSPIAT(cells, selectedMarkers, baselineMarkers, tumourMarker);

                // Calculate the thresholds
                double[] thresholds = thresholdSPIAT.calculateThresholds();

                // Populate the results comboBox
                comboBoxResults.setItems(
                        FXCollections.observableList(
                                selectedMarkers
                                        .stream()
                                        .map(SPIATMarkerInformation::getMarkerName)
                                        .collect(Collectors.toCollection(ArrayList::new))
                        )
                );

                // Add to the results table
//                resultsTable.getTable().getItems().clear();
                updateResultsTable();

                // populate the graph
                // Clear previous results if exists
                lineChart.getData().clear();
                populateLineChart();

                markersMap.put(imageData.getServerPath(), markers);
                thresholdMap.put(imageData.getServerPath(), thresholdSPIAT);
                selectedMarkersResultsMap.put(imageData.getServerPath(), selectedMarkersResults);
                selectedMarkersMap.put(imageData.getServerPath(), selectedMarkers);
            });

            // Get the currently selected cells
            currentlySelected = getSelectedObjects();

            // ComboBox for results
            comboBoxResults.setOnAction((event) -> {
                if (thresholdSPIAT == null) {
                    return;
                }

                // Get the selected marker
                String selectedForResults = comboBoxResults.getValue();
                if (selectedForResults == null) {
                    return;
                }

                // Index of the current marker
                int i = 0;
                for (SPIATMarkerInformation marker : thresholdSPIAT.getMarkerInformationMap().values()) {
                    // Set previous as not visible
                    if (marker.getMainSeries().getNode().isVisible()) {
                        lineChart.getData().get(i).getNode().setVisible(false);
                    }
                    // Set the selected marker to be visible and update line chart
                    if (marker.getMarkerName().equals(selectedForResults)) {
                        lineChart.getData().get(i).getNode().setVisible(true);

                        Series<Number, Number> line = new Series<>();
                        line.getData().add(new Data<>(marker.getThreshold(), 0));
                        line.getData().add(new Data<>(marker.getThreshold(), marker.getMaxDensity() * 1.25));

                        Color color = Color.RED;
                        String rgb = String.format("%d, %d, %d",
                                (int) (color.getRed() * 255),
                                (int) (color.getGreen() * 255),
                                (int) (color.getBlue() * 255));


                        if (thresholdLine != null) {
                            lineChart.getData().remove(thresholdLine);
                        }




                        thresholdLine = line;
                        lineChart.getData().add(line);

                        Node node = line.getNode().lookup(".chart-series-line");
                        node.setStyle("-fx-stroke: rgba(" + rgb + ", 0.3);");

                        // The x and y axis for the line chart
                        NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();
                        NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();

                        // Update the bounds
                        yAxis.setUpperBound(marker.getMaxDensity() * 1.25);
                        yAxis.setLowerBound(0);
                        xAxis.setUpperBound(marker.getThreshold() * 2);
                        xAxis.setLowerBound(0);

                        // Deselect the previous cells
                        if (currentlySelected != null) {
                            imageData.getHierarchy().getSelectionModel().deselectObjects(currentlySelected);
                        }

                        // Select the cells which are positive for the current marker
                        List<PathObject> positive = cells.stream().parallel().filter(
                                it -> Double.compare(
                                        it.getMeasurementList().getMeasurementValue(marker.getMeasurementName()),
                                        marker.getThreshold()
                                ) > 0
                        ).collect(Collectors.toList());
                        imageData.getHierarchy().getSelectionModel().selectObjects(positive);

                        // Update the cells which are currently selected
                        currentlySelected = positive;
                    }
                    i++;
                }
            });

            // Save the classifiers and apply the classifier
            saveClassifierButton.setOnAction((event) -> {
                // Apply classifier
                if (imageData == null){
                    Dialogs.showErrorMessage(title, "Nothing to save");
                    return;
                }
                if (thresholdSPIAT == null){
                    Dialogs.showErrorMessage(title, "No Results to save");
                    return;
                }

                imageData.getHierarchy().getDetectionObjects().forEach(it -> it.setPathClass(null));
                setCellPathClass();
                // Save classifier
                saveClassifiers();
            });

            // Choose the directory for the table to be saved to.
            chooseButton.setOnAction((event) -> {
                if (imageData == null){
                    Dialogs.showErrorMessage(title, "Nothing to save");
                    return;
                }
                if (thresholdSPIAT == null){
                    Dialogs.showErrorMessage(title, "No Results to save");
                    return;
                }
                String ext = ".csv";
                String defaultName = "SPIAT_Table_Results";
                String extDesc = "CSV (Comma delimited)";
                File pathOut = Dialogs.promptToSaveFile("Output file",
                        Projects.getBaseDirectory(qupath.getProject()),
                        defaultName + ext,
                        extDesc,
                        ext);
                if (pathOut != null) {
                    if (pathOut.isDirectory())
                        pathOut = new File(
                                pathOut.getAbsolutePath() + File.separator + "SPIAT_Table_Results" + ext);
                    resultsTableName.setText(pathOut.getAbsolutePath());
                }
            });

            // Save the results table
            saveResultsTableButton.setOnAction((event) -> {
                if (imageData == null){
                    Dialogs.showErrorMessage(title, "Nothing to save");
                    return;
                }
                if (thresholdSPIAT == null){
                    Dialogs.showErrorMessage(title, "No Results to save");
                    return;
                }
                String fullFileName = resultsTableName.getText();
                try {
                    writeExcel(fullFileName, selectedMarkersResults);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });


            VBox h = new VBox(gridPane);
            h.setFillWidth(true);

//            VBox v = new VBox(h);
//            v.setFillWidth(true);

            // Initialise stage
            stage = new Stage();
            stage.initOwner(QuPathGUI.getInstance().getStage());
            stage.setScene(new Scene(h));
            updateTitle();

            stage.setHeight(500);
            stage.setWidth(750);

            dialog = stage;
            return stage;
        }

        public Stage getDialog() {
            return dialog;
        }

        // ************************* Refresh methods ************************* //

        // Updates the attributes associated with the qupath gui
        private void updateQuPath() {
            server = viewer.getServer();
            imageData = viewer.getImageData();
            try {
                cells = imageData.getHierarchy().getCellObjects();
            }
            catch (Exception ex){
                cells = null;
            }
        }

        // Updates the title of the window
        private void updateTitle() {
            try {
                stage.setTitle(title + " (" + imageData.getServer().getMetadata().getName() + ")");
            }catch(Exception e){
                stage.setTitle(title);
            }
        }

        // Sets the selected for results array to null if results don't already exist
        private void updateSelectedForResults() {
            if (selectedMarkers != null) {
                comboBoxResults.setItems(FXCollections.observableList(
                        selectedMarkers
                                .stream()
                                .map(SPIATMarkerInformation::getMarkerName)
                                .collect(Collectors.toCollection(ArrayList::new))
                        )
                );
            } else {
                comboBoxResults.setItems(FXCollections.observableArrayList());
            }
        }

        // Updates the measurements that are available
        private void updateMeasurements() {
            // Collect the measurement names
            getMarkerMeasurementNames();
            // Set the default Measurement to Cell mean if it exists
            assert markerMeasurements != null;
            if (markerMeasurements.contains("Cell: Mean")) {
                defaultValue = "Cell: Mean";
            } else {
                defaultValue = markerMeasurements.get(0);
            }
        }

        // Updates the markers which are available
        private void updateMarkers() {
            if (imageData == null){
                markers = null;
                return;
            }
            markers = markersMap.getOrDefault(imageData.getServerPath(), null);
            if (markers == null) {
                // For collecting options
                markers = FXCollections.observableArrayList();
                markerNames = FXCollections.observableArrayList();
                columnNames = FXCollections.observableArrayList(markerMeasurements);

                // Create the entries for the table rows
                for (int i = 0; i < server.nChannels(); i++) {
                    String markerName = server.getChannel(i).getName();
                    markerNames.add(markerName);

                    markers.add(
                            new MarkerTableEntry(
                                    server.getChannel(i).getName(),
                                    true,
                                    false,
                                    columnNames,
                                    defaultValue
                            )
                    );
                }
            }
        }

        // Updates the tumour marker selection box
        private void updateComboBoxTumour() {
            if (markers != null) {
                tumourBox.setItems(FXCollections.observableList(
                        markers
                                .stream()
                                .map(MarkerTableEntry::getName)
                                .collect(Collectors.toCollection(ArrayList::new))
                ));
            } else{
                tumourBox.setItems(null);
            }
        }

        // Updates the options table
        private void updateOptionsTable() {
            optionsTable.setItems(markers);
        }

        // Updates the results table
        private void updateResultsTable() {
            resultsTable.setItems(selectedMarkersResults);
        }

        private void refreshOptions() {
            updateQuPath();
            updateMeasurements();
            updateMarkers();
            updateComboBoxTumour();

            try {
                String serverPath = imageData.getServerPath();
                selectedMarkers = selectedMarkersMap.getOrDefault(serverPath, null);
                selectedMarkersResults = selectedMarkersResultsMap.getOrDefault(serverPath, null);
                thresholdSPIAT = thresholdMap.getOrDefault(serverPath, null);
            } catch (Exception e){
                selectedMarkers = null;
                selectedMarkersResults = null;
                thresholdSPIAT = null;
            }

            updateSelectedForResults();
            updateOptionsTable();
            updateResultsTable();
            updateTitle();
            if (thresholdSPIAT == null) {
                lineChart.getData().clear();
            } else {
                populateLineChart();
            }
        }

        @Override
        public void changed(ObservableValue<? extends ImageData<BufferedImage>> observableValue,
                            ImageData<BufferedImage> bufferedImageImageData, ImageData<BufferedImage> t1) {
            refreshOptions();
        }

        // ****************************** JavaFx helper methods **************************** //



        // Initialises the line chart
        private LineChart<Number, Number> initialiseLineChart() {

            final NumberAxis xAxis = new NumberAxis();
            final NumberAxis yAxis = new NumberAxis();
            xAxis.setAutoRanging(false);
            xAxis.setUpperBound(255);
            xAxis.setLabel("Intensity");
            yAxis.setAutoRanging(false);

            //creating the chart
            lineChart = new LineChart<>(xAxis, yAxis);
            lineChart.setTitle("Density Estimation");
            lineChart.setCreateSymbols(false);
            lineChart.setLegendVisible(false);
//            lineChart.setPrefSize(400, 100);

            return lineChart;
        }

        // Populates the line chart
        private void populateLineChart() {
            if (thresholdSPIAT != null) {
                lineChart.getData().clear();
                int i = 0;
                for (SPIATMarkerInformation marker : thresholdSPIAT.getMarkerInformationMap().values()) {

                    // For the actual density
                    Series<Number, Number> series = new Series<>();
                    for (int j = 0; j < thresholdSPIAT.getX().length; j++) {
                        series.getData().add(new Data<>(thresholdSPIAT.getValueAtX(j), marker.getEstimatedDensity()[j]));
                    }

                    marker.setMainSeries(series);
                    lineChart.getData().add(series);
                    lineChart.getData().get(i++).getNode().setVisible(false);
                }
            }
        }

        // Extracts the measurements for each marker
        // Must be a better way to access the measurements
        private void getMarkerMeasurementNames() {

            // Do something for when no cell detected
            if (cells == null) {
                return;
            }
            // Gets a cell
            PathObject cell = (PathObject) cells.toArray()[0];
            String markerName = server.getChannel(0).getName();
            List<String> measurementList = cell.getMeasurementList().getMeasurementNames();

            // Potentially could be a source of error #################################################
            this.markerMeasurements = measurementList.stream()
                    .parallel()
                    .filter(it -> it.contains(markerName + ":"))
                    .map(it -> it.substring(markerName.length() + 2))
                    .collect(Collectors.toList());
        }

        // Sets the path class for each of the cells
        private void setCellPathClass() {
            for (SPIATMarkerInformation marker : thresholdSPIAT.getMarkerInformationMap().values()) {
                List<PathObject> positive = cells.stream().parallel().filter(it ->
                        it.getMeasurementList().getMeasurementValue(marker.getMeasurementName())
                                > marker.getThreshold()).collect(Collectors.toList());

                positive.forEach(it -> {
                            PathClass currentClass = it.getPathClass();
                            PathClass pathClass;

                            if (currentClass == null) {
                                pathClass = PathClassFactory.getPathClass(marker.getMarkerName());
                            } else {
                                pathClass = PathClassFactory.getDerivedPathClass(
                                        currentClass,
                                        marker.getMarkerName(),
                                        null);
                            }
                            it.setPathClass(pathClass);
                        }
                );
            }
        }

        // Writes the results table as a csv to the specified fileName
        private void writeExcel(String fileName, ObservableList<MarkerTableEntry> markers) throws Exception {
            try {
                File file = new File(fileName);
                Writer writer = new BufferedWriter(new FileWriter(file));
                // Title


                writer.write("Marker,Measurement,Threshold,Proportion,Count\n");
                for (MarkerTableEntry marker : markers) {
                    String text = marker.getName() + ","
                            + marker.getMeasurementName() + ","
                            + marker.getThreshold() + ","
                            + marker.getProportion() + ","
                            + marker.getCount() + "\n";

                    writer.write(text);
                }
                Dialogs.showInfoNotification(title, "Results table saved");
                writer.flush();
                writer.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // Save the classifiers
        private void saveClassifiers() {
            // Get project
            var project = qupath.getProject();
            // Check if project is null
            if (project == null) {
                Dialogs.showErrorMessage(title, "You need a project to save this classifier!");
                return;
            }
            // Get the classifier name
            String name = GeneralTools.stripInvalidFilenameChars(classifierName.getText());
            if (name.isBlank()) {
                Dialogs.showErrorMessage(title, "Please enter a name for the classifier!");
                return;
            }
            try {
                var classifierNames = project.getObjectClassifiers().getNames();

                ObservableList<ClassifierWrapper<BufferedImage>> array = FXCollections.observableArrayList();
                for (MarkerTableEntry marker : selectedMarkersResults) {
                    String markerClassifierName = makeClassifierName(marker.getName());

                    var classifier = updateClassifier(marker);
                    project.getObjectClassifiers().put(markerClassifierName, classifier);

                    var wrap = wrapClassifier(markerClassifierName);
                    if (!array.contains(wrap)) {
                        array.add(wrap);
                    }
                }
                tryToSave(project, array, name);
            } catch (Exception e) {
                Dialogs.showErrorNotification(title, e);
                return;
            }
        }

        // Makes the classifier name for individual markers
        private String makeClassifierName(String markerName) {
            return classifierName.getText() + "_" + markerName;
        }

        // Initialises the classifier which will be saved
        private ObjectClassifier<BufferedImage> updateClassifier(MarkerTableEntry marker) {
            String measurement = marker.getMarkerInfo().getMeasurementName();
            double threshold = marker.getThreshold();
            PathClass classAbove = PathClassFactory.getPathClass(marker.getName());
            var classEquals = classAbove; // We use >= and if this changes the tooltip must change too!

            if (measurement == null || Double.isNaN(threshold))
                return null;

            return new ClassifyByMeasurementBuilder<BufferedImage>(measurement)
                    .threshold(threshold)
                    .above(classAbove)
                    .equalTo(classEquals)
                    .build();
        }

        // Wraps classifier in the ProjectClassifierWrapper so it can be saved via the QuPath API
        private ProjectClassifierWrapper<BufferedImage> wrapClassifier(String classifierName) {
            return new ProjectClassifierWrapper<>(qupath.getProject(), classifierName);
        }


        /*
         ******************** Code taken from Pete Bankhead's "CreateCompositeClassifier.java" *****************************
         * */
        private ObjectClassifier<BufferedImage> tryToBuild(Collection<ClassifierWrapper<BufferedImage>> wrappers) throws
                IOException {
            var classifiers = new LinkedHashSet<ObjectClassifier<BufferedImage>>();
            for (var wrapper : wrappers) {
                classifiers.add(wrapper.getClassifier());
            }
            if (classifiers.size() < 2) {
                Dialogs.showErrorMessage(title,
                        "At least two different classifiers must be selected to create a composite!");
                return null;
            }
            return ObjectClassifiers.createCompositeClassifier(classifiers);
        }

        private ObjectClassifier<BufferedImage> tryToSave(Project<BufferedImage> project,
                                                          Collection<ClassifierWrapper<BufferedImage>> wrappers,
                                                          String name) {
            try {
                var composite = tryToBuild(wrappers);
                if (composite == null)
                    return null;

                name = name == null ? null : GeneralTools.stripInvalidFilenameChars(name);
                if (project != null && name != null && !name.isBlank()) {
                    if (project.getObjectClassifiers().contains(name)) {
                        if (!Dialogs.showConfirmDialog(title, "Overwrite existing classifier called '" + name + "'?"))
                            return null;
                    }
                    project.getObjectClassifiers().put(name, composite);
                    Dialogs.showInfoNotification(title, "Classifier written to project as " + name);
                } else {
                    var file = Dialogs.promptToSaveFile(title, null, name, "JSON", ".json");
                    if (file != null) {
                        ObjectClassifiers.writeClassifier(composite, file.toPath());
                        Dialogs.showInfoNotification(title, "Classifier written to " + file.getAbsolutePath());
                    }
                }
                return composite;
            } catch (Exception e) {
                Dialogs.showErrorMessage(title, e);
                return null;
            }

        }
    }
}
