package com.Kenta;

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

public class PhenotypeWindow extends AbstractWindow implements Runnable, ChangeListener<ImageData<BufferedImage>> {

    private TableCreator<PhenotypeTableEntry> phenotypeTable;
    private ObservableList<PhenotypeTableEntry> phenotypeList = FXCollections.observableArrayList();
    private TableCreator<PhenotypeTableEntry> resultsTable;

    private VBox mainVBox;
    private Stage stage;
    private String title = "Phenotype";


    private QuPathGUI qupath;
    private Collection<PathObject> cells;
    private ImageData<BufferedImage> imageData;
    private ImageServer<BufferedImage> server;
//    private QuPathViewer viewer;

    private ObservableList<String> markers;
    private Button addPhenotype;
    private Button removePhenotype;
    private ComboBox<String> compositeClassifierBox;
    private ComboBox<String> fileNameOptions;
    private File folderName;

    private HashMap<String, ObservableList<PhenotypeTableEntry>> resultsMap = new HashMap<>();

    public PhenotypeWindow(QuPathGUI qupath){
        this.qupath = qupath;
    }


    @Override
    public void run() {

        // Need to add in way to check for opened windows
//
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

        if (stage == null) {
            try {
                stage = createDialog();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        stage.show();
    }



    public Stage createDialog() throws IOException {
        stage = new Stage();
        qupath.getViewer().imageDataProperty().addListener(this);
        ObservableList<String> channels = FXCollections.observableArrayList(qupath.getProject().getObjectClassifiers().getNames());

        markers = FXCollections.observableArrayList();

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


        // ******************* Button
        addPhenotype = new Button("+");
        removePhenotype = new Button("-");

        addPhenotype.setOnAction((event) -> {
            PhenotypeTableEntry newPhenotype = new PhenotypeTableEntry(markers);
            phenotypeList.add(newPhenotype);
            phenotypeTable.setItems(phenotypeList);
        });

        removePhenotype.setOnAction((event) ->{
            PhenotypeTableEntry toBeRemoved = phenotypeTable.removeRow();

            if (toBeRemoved !=null) {
                phenotypeList.remove(toBeRemoved);
            }
        });


        mainVBox = new VBox();
        mainVBox.setFillWidth(true);
        mainVBox.setSpacing(5);


        Label chooseClassifierLabel = createLabel("Load a composite classifier      ");
        compositeClassifierBox = new ComboBox<>(channels);

        var project = qupath.getProject();

        compositeClassifierBox.setOnAction((event) -> {
            updateMarkers();
            for (PhenotypeTableEntry entry : phenotypeList) entry.updateMarkers(markers);
        });

        updateTitle();
        updateQuPath();


        HBox titlePane = new HBox();
        titlePane.getChildren().addAll(chooseClassifierLabel, compositeClassifierBox);


        Label loadLabel = createLabel("Load previously saved options (can be ignored)       ");
        fileNameOptions = new ComboBox<>();
        updateAvailableClassifiers();

        Button loadButton = new Button("Load Options");


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


        // ***************************** Load Options *****************************
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
                        phenotypeList.add(new PhenotypeTableEntry(option, markers));
                    }
                    phenotypeTable.setItems(phenotypeList);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });



        Label addRemoveLabel = createLabel("Add/Remove Phenotypes     ");
        HBox pane = new HBox();
        pane.getChildren().addAll(addRemoveLabel, addPhenotype, removePhenotype);
        pane.setAlignment(Pos.CENTER_RIGHT);



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



        Button runButton = new Button("Run");
        mainVBox.getChildren().add(runButton);


        // ***************************** Run Phenotype *****************************
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

        mainVBox.setPadding(new Insets(10, 10, 10, 10));

        phenotypeTable.getTable().prefWidthProperty().bind(stage.widthProperty());
        resultsTable.getTable().prefWidthProperty().bind(stage.widthProperty());

        stage.initOwner(QuPathGUI.getInstance().getStage());
        stage.setScene(new Scene(mainVBox));
        updateTitle();
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

    private void updateQuPath() {
        server = qupath.getViewer().getServer();
        imageData = qupath.getViewer().getImageData();
        try {
            cells = imageData.getHierarchy().getCellObjects();
        }
        catch (Exception ex){
            cells = null;
        }
    }


    public void updateMarkers(){
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


    public void updateAvailableClassifiers(){
        folderName = new File(Projects.getBaseDirectory(qupath.getProject()), "Phenotype Options");

        if (!folderName.exists()){
            folderName.mkdirs();
        }
        fileNameOptions.setItems(FXCollections.observableArrayList(folderName.list()));
    }

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


    public static class PhenotypeOptions{
        private final String classifierName;
        private final String name;
        private final ArrayList<String> positiveMarkers;
        private final ArrayList<String> negativeMarkers;

        public PhenotypeOptions(String classifierName, String name, ArrayList<String> positiveMarkers, ArrayList<String> negativeMarkers){
            this.classifierName = classifierName;
            this.name = name;
            this.positiveMarkers = positiveMarkers;
            this.negativeMarkers = negativeMarkers;
        }

        public String getClassifierName(){
            return classifierName;
        }

        public String getName() {
            return name;
        }

        public ArrayList<String> getNegativeMarkers() {
            return negativeMarkers;
        }

        public ArrayList<String> getPositiveMarkers() {
            return positiveMarkers;
        }
    }
}
