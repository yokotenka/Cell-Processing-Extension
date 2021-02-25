package com.wehi;

import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class for table entry for phenotypes
 */
public class PhenotypeTableEntry implements Serializable {
    // Text field for entering name
    private TextField name;
    // Positive marker table cell
    private HBox positiveMarkers;
    // Negative marker table cell
    private HBox negativeMarkers;
    // To select the positive markers
    private ComboBox<String> positiveList;
    // To select the negative markers
    private ComboBox<String> negativeList;
    // List of positive markers
    private ArrayList<String> positiveMarkerArray = new ArrayList<>();
    // List of negative markers
    private ArrayList<String> negativeMarkerArray = new ArrayList<>();
    // Count of the phenotypes
    private int count = 0;

    /**
     * Constructor for the table row for phenotypes
     * @param markers The list of markers
     */
    public PhenotypeTableEntry(ObservableList<String> markers){
        this.name = new TextField();
        positiveMarkers = createPositiveMarkersCell(markers);
        negativeMarkers = createNegativeMarkersCell(markers);
    }

    /**
     * Constructor for loading options from previously saved options
     * @param option options
     * @param markers available markers
     */
    public PhenotypeTableEntry(PhenotypeWindow.PhenotypeOptions option, ObservableList<String> markers){
        this.name = new TextField(option.getName());
        this.positiveMarkerArray = option.getPositiveMarkers();
        this.negativeMarkerArray = option.getNegativeMarkers();

        this.positiveMarkers = createPositiveMarkersCell(markers);
        this.negativeMarkers = createNegativeMarkersCell(markers);
    }


    // Cell for positive markers
    private HBox createPositiveMarkersCell (ObservableList<String> markers){
        TextField textField = new TextField();
        if (positiveMarkerArray.size() > 0) {
            String text = positiveMarkerArray.toString();
            textField.setText(text.substring(1, text.length()-1));
        }
        textField.setEditable(false);
        positiveList = new ComboBox<>(markers);
        Button add = new Button("+");
        Button minus = new Button("-");

        add.setOnAction((event) -> {
            String selected = positiveList.getValue();
            if (!positiveMarkerArray.contains(selected)){
                positiveMarkerArray.add(selected);
                String text = positiveMarkerArray.toString();
                textField.setText(text.substring(1, text.length() - 1));
            };
        });

        minus.setOnAction((event) ->{
            String selected = positiveList.getValue();
            if (positiveMarkerArray.contains(selected)){
                positiveMarkerArray.remove(selected);
                String text = positiveMarkerArray.toString();
                textField.setText(text.substring(1, text.length()-1));
            };
        });


        HBox cell = new HBox();
        cell.getChildren().addAll(textField, positiveList, add, minus);
        return cell;
    }

    // Cell for negative markers
    // ################# Definitely do not need this method since it is exactly the same as the one above
    private HBox createNegativeMarkersCell (ObservableList<String> markers){
        TextField textField = new TextField();
        if (negativeMarkerArray.size() > 0) {
            String text = negativeMarkerArray.toString();
            textField.setText(text.substring(1, text.length()-1));
        }
        textField.setEditable(false);
        negativeList = new ComboBox<>(markers);
        Button add = new Button("+");
        Button minus = new Button("-");

        add.setOnAction((event) -> {
            String selected = negativeList.getValue();
            if (!negativeMarkerArray.contains(selected)){
                negativeMarkerArray.add(selected);
                String text = negativeMarkerArray.toString();
                textField.setText(text.substring(1, text.length() - 1));
            }
        });

        minus.setOnAction((event) ->{
            String selected = negativeList.getValue();
            if (negativeMarkerArray.contains(selected)){
                negativeMarkerArray.remove(selected);
                String text = negativeMarkerArray.toString();
                textField.setText(text.substring(1, text.length()-1));
            }
        });

        HBox cell = new HBox();
        cell.getChildren().addAll(textField, negativeList, add, minus);
        return cell;
    }

    /**
     * Getter for the Name text field
     * @return TextField
     */
    public TextField getName(){
        return name;
    }

    /**
     * Getter for the phenotype name
     * @return
     */
    public String getPhenotypeName(){
        return name.getText();
    }

    /**
     * Getter for the table cell of positive markers
     * @return HBox
     */
    public HBox getPositiveMarkers(){
        return positiveMarkers;
    }

    /**
     * Getter for the table cell of negative markers
     * @return HBox
     */
    public HBox getNegativeMarkers(){
        return negativeMarkers;
    }

    /**
     * Getter for the array of positive markers
     * @return array of positive markers
     */
    public ArrayList<String> getPositiveMarkerArray(){
        return positiveMarkerArray;
    }

    /**
     * Getter for the arrya of negative markers
     * @return array of negative markers
     */
    public ArrayList<String> getNegativeMarkerArray() {
        return negativeMarkerArray;
    }

    /**
     * Updates the available markers
     * @param markers the new set of markers
     */
    public void updateMarkers (ObservableList<String> markers){
        positiveList.setItems(markers);
        negativeList.setItems(markers);
    }

    /**
     * Increases the count by one
     */
    public void incrementCount(){
        count++;
    }

    /**
     * Getter for the count
     * @return count
     */
    public int getCount(){
        return count;
    }

    /**
     * Getter for the string containing the positive markers
     * @return string
     */
    public String getPositiveMarkerString(){
        return positiveMarkerArray.toString().substring(1, positiveMarkerArray.toString().length()-1);
    }

    /**
     * Getter for the string containing the negative markers
     * @return string
     */
    public String getNegativeMarkerString(){
        return negativeMarkerArray.toString().substring(1, negativeMarkerArray.toString().length()-1);
    }

    /**
     * resets count to 0
     */
    public void resetCount(){
        this.count = 0;
    }

    /**
     * Changes phenotype name
     * @param phenotypeName new phenotype name
     */
    public void setPhenotypeName(String phenotypeName) {
        this.name.setText(phenotypeName);
    }
}