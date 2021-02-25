package com.wehi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import qupath.lib.classifiers.object.ObjectClassifier;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.dialogs.Dialogs;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.classes.PathClass;
import qupath.lib.projects.Projects;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Class for the phenotype graphical interface. Phenotypes cells based on composite classifier obtained by one of the
 * threshold methods.
 */
public class PhenotypeWindow extends AbstractWindow implements Runnable, ChangeListener<ImageData<BufferedImage>> {
    // Phenotype options table
    private TableCreator<PhenotypeTableEntry> phenotypeTable;
    // List of phenotypes
    private ObservableList<PhenotypeTableEntry> phenotypeList = FXCollections.observableArrayList();
    // Table displaying results
    private TableCreator<PhenotypeTableEntry> resultsTable;
    // Button which will add a phenotype to the options table
    private Button addPhenotype;
    // Button which will remove a selected phenotype from the options table
    private Button removePhenotype;
    // ComboBox to select the classifier to use
    private ComboBox<String> compositeClassifierBox;
    // ComboBox to select the previously saved options
    private ComboBox<String> fileNameOptions;

    // Folder name of where the options will be saved
    private File folderName;

    // Main pane of the window
    private VBox mainVBox;
    // The window
    private Stage stage;
    // The title of the Window
    private String title = "Phenotype";

    // The current qupath instance
    private QuPathGUI qupath;
    // The cells which need classifying
    private Collection<PathObject> cells;
    // Current image data
    private ImageData<BufferedImage> imageData;

    // Markers which are present in the current classifier
    private ObservableList<String> markers;
    // Hashmap to keep track of the results after switching images
    private HashMap<String, ObservableList<PhenotypeTableEntry>> resultsMap = new HashMap<>();

    /**
     * Constructor
     * @param qupath the current instance of qupath
     */
    public PhenotypeWindow(QuPathGUI qupath){
        this.qupath = qupath;
    }


    @Override
    public void run() {

        var viewer = qupath.getViewer();

        if (viewer == null){
            Dialogs.showErrorMessage(title, "Viewer is empty. Please open an image.");
            return;
        }
        if (qupath.getProject() == null){
            Dialogs.showErrorMessage(title, "Project is empty. Please open a project.");
            return;
        }
        if (qupath.getImageData() == null){
            Dialogs.showErrorMessage(title, "Image data is empty");
            return;
        }
        if (qupath.getImageData().getHierarchy().getCellObjects().isEmpty()){
            Dialogs.showErrorMessage(title,
                    "No Cells are detected. Must have cell detections to run SPIAT");
            return;
        }

        // Only have one instance of the window open
        if (stage == null) {
            try {
                stage = createDialog();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        stage.show();
    }


    /**
     * Creates Stage which will be displayed
     * @return stage to be displayed
     * @throws IOException could be thrown when opening options.
     */
    public Stage createDialog() throws IOException {
        // Initialise stage
        stage = new Stage();

        // Main Pane of the window
        mainVBox = new VBox();
        mainVBox.setFillWidth(true);
        mainVBox.setSpacing(5);
        mainVBox.setPadding(new Insets(10, 10, 10, 10));

        // Change title to match current image
        updateTitle();
        // Change qupath data to match the current image
        updateQuPath();

        // Get the project
        var project = qupath.getProject();


        // Add listener to the image data so that the window updates when image data changes
        qupath.getViewer().imageDataProperty().addListener(this);

        /* The available classifiers
         * - could add in listener so that the channel list updates. Bit extra but might need it if going back
         *  between threshold and phenotype
         */
        ObservableList<String> channels = FXCollections.observableArrayList(
                                                        qupath.getProject().getObjectClassifiers().getNames());

        // The available markers
        markers = FXCollections.observableArrayList();

        // The initial phenotypes in the options table
        PhenotypeTableEntry phenotypeOptions1 = new PhenotypeTableEntry(markers);
        PhenotypeTableEntry phenotypeOptions2 = new PhenotypeTableEntry(markers);
        phenotypeList.add(phenotypeOptions1);
        phenotypeList.add(phenotypeOptions2);


        // *************** Table *****************
        phenotypeTable = new TableCreator<>();
        phenotypeTable.setItems(phenotypeList);
        phenotypeTable.addColumn("Phenotype", "name", 0.1);
        phenotypeTable.addColumn("Positive Markers", "positiveMarkers", 0.45);
        phenotypeTable.addColumn("Negative Markers", "negativeMarkers", 0.45);


        // *************** Load classifiers ***************
        Label chooseClassifierLabel = createLabel("Load a composite classifier      ");
        compositeClassifierBox = new ComboBox<>(channels);

        // Refresh the markers in the table
        compositeClassifierBox.setOnAction((event) -> {
            updateMarkers();
            for (PhenotypeTableEntry entry : phenotypeList) entry.updateMarkers(markers);
        });

        HBox titlePane = new HBox();
        titlePane.getChildren().addAll(chooseClassifierLabel, compositeClassifierBox);

        // *************** Load options ***************
        Label loadLabel = createLabel("Load previously saved options (can be ignored)       ");
        fileNameOptions = new ComboBox<>();
        updateAvailableClassifiers();

        Button loadButton = new Button("Load Options");
        loadButton.setOnAction((event) -> {
            if (fileNameOptions.getValue() != null) {

                try {
                    FileReader file = new FileReader(new File(folderName, fileNameOptions.getValue()));
                    Gson gson = new Gson();
                    PhenotypeOptions[] options = gson.fromJson(file, PhenotypeOptions[].class);

                    compositeClassifierBox.setValue(options[0].getClassifierName());
                    updateMarkers();

                    phenotypeList = FXCollections.observableArrayList();
                    for (PhenotypeOptions option : options) {
                        if (!option.getName().equals("Undefined")) {
                            phenotypeList.add(new PhenotypeTableEntry(option, markers));
                        }
                    }
                    phenotypeTable.setItems(phenotypeList);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        GridPane headerPane = new GridPane();
        headerPane.add(chooseClassifierLabel, 0 ,0, 1, 1);
        headerPane.add(compositeClassifierBox, 1, 0, 2, 1);
        headerPane.add(loadLabel, 0, 1, 1, 1);
        headerPane.add(new HBox(fileNameOptions, loadButton), 1, 1, 1, 1);
        headerPane.prefWidthProperty().bind(stage.widthProperty());


        loadButton.prefWidthProperty().bind(Bindings.divide(compositeClassifierBox.widthProperty(), 2));
        fileNameOptions.prefWidthProperty().bind(Bindings.divide(compositeClassifierBox.widthProperty(), 2));

        mainVBox.getChildren().add(headerPane);
        mainVBox.getChildren().add(phenotypeTable.getTable());


        // ******************* Add Remove Phenotypes *******************
        addPhenotype = new Button("+");
        removePhenotype = new Button("-");

        // Update the table and phenotype list
        addPhenotype.setOnAction((event) -> {
            PhenotypeTableEntry newPhenotype = new PhenotypeTableEntry(markers);
            phenotypeList.add(newPhenotype);
            phenotypeTable.setItems(phenotypeList);
        });

        // Update the table and phenotype list
        removePhenotype.setOnAction((event) ->{
            PhenotypeTableEntry toBeRemoved = phenotypeTable.removeRow();

            if (toBeRemoved !=null) {
                phenotypeList.remove(toBeRemoved);
            }
        });

        Label addRemoveLabel = createLabel("Add/Remove Phenotypes     ");
        HBox pane = new HBox();
        pane.getChildren().addAll(addRemoveLabel, addPhenotype, removePhenotype);
        pane.setAlignment(Pos.CENTER_RIGHT);


        // ***************** Save Options ********************
        Label saveLabel = createLabel("Save table of options    ");
        TextField saveTextField = new TextField();
        Button saveButton = new Button("Save Options");

        HBox saveHBox = new HBox(saveLabel, saveTextField, saveButton);
        saveButton.setOnAction((event) -> {
            try  {
                File fileName = new File(folderName, saveTextField.getText());
                FileWriter writer = new FileWriter(fileName);
                Gson gson = new GsonBuilder().create();

                PhenotypeOptions[] options = new PhenotypeOptions[phenotypeList.size()];
                for (int i=0; i < phenotypeList.size(); i++){
                    options[i] = new PhenotypeOptions(
                            compositeClassifierBox.getValue(),
                            phenotypeList.get(i).getPhenotypeName(),
                            phenotypeList.get(i).getPositiveMarkerArray(),
                            phenotypeList.get(i).getNegativeMarkerArray()
                    );
                }

                gson.toJson(options, writer);
                Dialogs.showInfoNotification(title, "Saved options at " + fileName.toString());
                writer.flush();
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            updateAvailableClassifiers();
        });

        HBox justBelowTable = new HBox(saveHBox,pane);
        justBelowTable.setSpacing(40);
        mainVBox.getChildren().add(justBelowTable);

        // ***************************** Run Phenotype *****************************
        Button runButton = new Button("Run");
        mainVBox.getChildren().add(runButton);

        runButton.setOnAction((event) -> {
            if (cells ==null){
                Dialogs.showErrorMessage(title, "No cells detected!");
                return;
            }
            ObjectClassifier<BufferedImage> classifier = null;
            try {
                classifier = project.getObjectClassifiers().get(compositeClassifierBox.getValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert classifier != null;
            classifier.classifyObjects(imageData, cells, true);

            phenotypeList.forEach(PhenotypeTableEntry::resetCount);
            PhenotypeTableEntry undefined = new PhenotypeTableEntry(markers);
            undefined.setPhenotypeName("Undefined");
            PhenotypeDecider decider = new PhenotypeDecider(cells, phenotypeList, undefined);
            decider.decide();
            phenotypeList.add(undefined);
            resultsTable.setItems(phenotypeList);
            resultsTable.getTable().refresh();
            resultsMap.put(imageData.getServerPath(), phenotypeList);
        });

        Separator sep = new Separator();
        mainVBox.getChildren().add(sep);
        resultsTable = new TableCreator<>();
        resultsTable.addColumn("Phenotype", "phenotypeName",0.1);
        resultsTable.addColumn("Count", "count",0.1);
        resultsTable.addColumn("Positive Markers", "positiveMarkerString", 0.4);
        resultsTable.addColumn("Negative Markers", "negativeMarkerString", 0.4);
        mainVBox.getChildren().add(resultsTable.getTable());


        // ***************************** Save results table *****************************
        Label saveTableLabel = createLabel("Table name  ");
        TextField saveTableTextField = new TextField();
        Button chooseTableButton = new Button("Choose");
        Button saveTableButton = new Button("Save");
        HBox saveTableBox = new HBox(saveTableLabel, saveTableTextField, chooseTableButton, saveTableButton);

        // Choose the directory for the table to be saved to.
        chooseTableButton.setOnAction((event) -> {
            if (imageData == null || phenotypeList == null){
                Dialogs.showErrorMessage(title, "Nothing to save");
                return;
            }
            String ext = ".csv";
            String defaultName = "Phenotype_Count";
            String extDesc = "CSV (Comma delimited)";
            File pathOut = Dialogs.promptToSaveFile("Output file",
                    Projects.getBaseDirectory(qupath.getProject()),
                    defaultName + ext,
                    extDesc,
                    ext);
            if (pathOut != null) {
                if (pathOut.isDirectory())
                    pathOut = new File(
                            pathOut.getAbsolutePath() + File.separator + "Phenotype_Count" + ext);
                saveTableTextField.setText(pathOut.getAbsolutePath());
            }
        });

        // Save the results table
        saveTableButton.setOnAction((event) -> {
            if (imageData == null || phenotypeList == null){
                Dialogs.showErrorMessage(title, "Nothing to save");
                return;
            }
            String fullFileName = saveTableTextField.getText();
            try {
                writeExcel(fullFileName, phenotypeList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        mainVBox.getChildren().add(saveTableBox);
        phenotypeTable.getTable().prefWidthProperty().bind(stage.widthProperty());
        resultsTable.getTable().prefWidthProperty().bind(stage.widthProperty());

        // Final settings for the stage
        stage.initOwner(QuPathGUI.getInstance().getStage());
        stage.setScene(new Scene(mainVBox));
        stage.setWidth(850);
        stage.setHeight(500);

        return stage;
    }


    // Updates the title of the window
    private void updateTitle() {
        try {
            stage.setTitle(title + " (" + imageData.getServer().getMetadata().getName() + ")");
        }catch(Exception e){
            stage.setTitle(title);
        }
    }

    // Updates data extracted from qupath
    private void updateQuPath() {
        imageData = qupath.getViewer().getImageData();
        try {
            cells = imageData.getHierarchy().getCellObjects();
        }
        catch (Exception ex){
            cells = null;
        }
    }

    // Updates marker options depending on the classifier
    private void updateMarkers(){
        ObjectClassifier<BufferedImage> classifier = null;
        try {
            classifier = qupath.getProject().getObjectClassifiers().get(compositeClassifierBox.getValue());
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert classifier != null;
        ArrayList<String> markersArrList = classifier
                .getPathClasses()
                .stream()
                .parallel()
                .map(PathClass::getName)
                .collect(Collectors.toCollection(ArrayList::new));
        markers.setAll(markersArrList);
    }


    // Updates the available phenotype classifiers if one is added.
    private void updateAvailableClassifiers(){
        folderName = new File(Projects.getBaseDirectory(qupath.getProject()), "Phenotype Options");

        if (!folderName.exists()){
            folderName.mkdirs();
        }
        fileNameOptions.setItems(FXCollections.observableArrayList(folderName.list()));
    }

    // Updates the results table
    public void updateResultsTable(){
        phenotypeList = resultsMap.get(imageData.getServerPath());
        resultsTable.setItems(phenotypeList);
    }

    @Override
    public void changed(ObservableValue<? extends ImageData<BufferedImage>> observableValue,
                        ImageData<BufferedImage> bufferedImageImageData, ImageData<BufferedImage> t1) {
        updateQuPath();
        updateTitle();
        updateResultsTable();
    }

    // Writes the results table as a csv to the specified fileName
    private void writeExcel(String fileName, ObservableList<PhenotypeTableEntry> phenotypes) throws Exception {
        try {
            File file = new File(fileName);
            Writer writer = new BufferedWriter(new FileWriter(file));
            // Title


            writer.write("Phenotype,Count,Positive Markers,Negative Markers\n");
            for (PhenotypeTableEntry phenotype : phenotypes) {
                String text = phenotype.getName() + ","
                        + phenotype.getCount() + ","
                        + phenotype.getPositiveMarkerString()+ ","
                        + phenotype.getNegativeMarkerString() + "\n";

                writer.write(text);
            }
            Dialogs.showInfoNotification(title, "Results table saved");
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Class to save options using Gson
     */
    public static class PhenotypeOptions{
        // Classifier file name
        private final String classifierName;
        // name of phenotype
        private final String name;
        // list of positive markers
        private final ArrayList<String> positiveMarkers;
        // list of negative markers
        private final ArrayList<String> negativeMarkers;

        /**
         * Constructor
         * @param classifierName classifier file name
         * @param name name of phenotype
         * @param positiveMarkers list of positive markers
         * @param negativeMarkers list of negative markers
         */
        public PhenotypeOptions(String classifierName, String name, ArrayList<String> positiveMarkers, ArrayList<String> negativeMarkers){
            this.classifierName = classifierName;
            this.name = name;
            this.positiveMarkers = positiveMarkers;
            this.negativeMarkers = negativeMarkers;
        }

        /**
         * Getter for classifier file name
         * @return classifier file name
         */
        public String getClassifierName(){
            return classifierName;
        }

        /**
         * Getter for the phenotype name
         * @return phenotype name
         */
        public String getName() {
            return name;
        }

        /**
         * Getter for the negative marker list
         * @return Array list of negative markers
         */
        public ArrayList<String> getNegativeMarkers() {
            return negativeMarkers;
        }

        /**
         * Getter for the positive marker list
         * @return Array list of positive markers
         */
        public ArrayList<String> getPositiveMarkers() {
            return positiveMarkers;
        }
    }
}
