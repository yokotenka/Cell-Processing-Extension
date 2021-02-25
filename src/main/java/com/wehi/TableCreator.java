package com.wehi;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Class which helps create a javafx TableView class
 */
public class TableCreator<T> {
    // The TableView instance
    private TableView<T> table;

    /**
     * Constructor
     */
    public TableCreator (){
        this.table = new TableView<>();
    }

    /**
     * Set items to the TableView
     * @param markers
     */
    public void setItems(ObservableList<T> markers){
        table.setItems(markers);
    }

    /**
     * Adding columns to the table
     * @param title Title of the column
     * @param variableName variable name in MarkerTableEntry
     */
    public void addColumn(String title, String variableName){
        table.getColumns().add(this.createColumn(title, variableName));
    }

    public void addColumn(String title, String variableName, double proportion){
        TableColumn<T, Object> col = createColumn(title, variableName);
        col.prefWidthProperty().bind(table.widthProperty().multiply(proportion));
        table.getColumns().add(col);
    }

    /**
     * Getter for table
     * @return table
     */
    public TableView getTable(){
        return table;
    }

    /**
     * Static creator for a table column
     * @param title Title of the column
     * @param variableName Name of the variable in MarkerTableEntry
     * @return Instance of TableColumn
     */
    public TableColumn<T, Object> createColumn(String title, String variableName){
        TableColumn<T, Object> nameCol = new TableColumn<>(title);
        nameCol.setCellValueFactory(new PropertyValueFactory<>(variableName));
        return nameCol;
    }


    /**
     * Adds a row to the table
     * @param row Row to be added
     */
     public void addRow(T row){
        table.getItems().add(row);
     }

    /**
     * Removes a row from table
     */
    public T removeRow(){
        if (table.getSelectionModel().getSelectedItem() != null) {
            T toBeRemoved = table.getSelectionModel().getSelectedItem();
            table.getItems().remove(
                    toBeRemoved
            );
            return toBeRemoved;
        }
        return null;
    }



    /**
     * Static creator for a TableView
     * @param markers Items to be set
     * @param tableColumns TableColumns
     * @return Instance of TableView
     */
    @SafeVarargs
    public static TableView<MarkerTableEntry> createTable(ObservableList<MarkerTableEntry> markers,
                                                          TableColumn<MarkerTableEntry, Object>... tableColumns){
        TableView<MarkerTableEntry> table = new TableView<>(markers);
        table.getColumns().addAll(tableColumns);
        return table;
    }
}