package com.Kenta;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.io.Serializable;
import java.util.ArrayList;

public class PhenotypeTableEntry implements Serializable {

    private String title = "Phenotype";
    private TextField name;
    private String phenotypeName = "";

    private HBox positiveMarkers;
    private HBox negativeMarkers;

    private ComboBox<String> positiveList;
    private ComboBox<String> negativeList;

    private ArrayList<String> positiveMarkerArray = new ArrayList<>();
    private ArrayList<String> negativeMarkerArray = new ArrayList<>();

    private int count = 0;


    public PhenotypeTableEntry(ObservableList<String> markers){
        this.name = new TextField();
        positiveMarkers = createPositiveMarkersCell(markers);
        negativeMarkers = createNegativeMarkersCell(markers);
    }

    public PhenotypeTableEntry(PhenotypeWindow.PhenotypeOptions option, ObservableList<String> markers){
        this.name = new TextField(option.getName());
        this.positiveMarkerArray = option.getPositiveMarkers();
        this.negativeMarkerArray = option.getNegativeMarkers();

        this.positiveMarkers = createPositiveMarkersCell(markers);
        this.negativeMarkers = createNegativeMarkersCell(markers);
    }




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
            };
        });


        HBox cell = new HBox();
        cell.getChildren().addAll(textField, negativeList, add, minus);
        return cell;
    }

    public TextField getName(){
        return name;
    }

    public String getPhenotypeName(){
        return name.getText();
    }


    public HBox getPositiveMarkers(){
        return positiveMarkers;
    }

    public HBox getNegativeMarkers(){
        return negativeMarkers;
    }

    public ArrayList<String> getPositiveMarkerArray(){
        return positiveMarkerArray;
    }

    public ArrayList<String> getNegativeMarkerArray() {
        return negativeMarkerArray;
    }

    void updateMarkers (ObservableList<String> markers){
        positiveList.setItems(markers);
        negativeList.setItems(markers);
    }

    public void incrementCount(){
        count++;
    }

    public int getCount(){
        return count;
    }

    public String getPositiveMarkerString(){
        return positiveMarkerArray.toString().substring(1, positiveMarkerArray.toString().length()-1);
    }

    public String getNegativeMarkerString(){
        return negativeMarkerArray.toString().substring(1, negativeMarkerArray.toString().length()-1);
    }

    public void resetCount(){
        this.count = 0;
    }

    public void setPhenotypeName(String phenotypeName) {
        this.name.setText(phenotypeName);
    }
}