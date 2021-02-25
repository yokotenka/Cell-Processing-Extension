package com.Kenta;

import javafx.geometry.Pos;
import javafx.scene.control.Label;

public class AbstractWindow {

    // Creates label for the javafx nodes
    public static Label createLabel(String msg) {
        Label label = new Label(msg);
        label.setFont(javafx.scene.text.Font.font(14));
        label.setAlignment(Pos.CENTER);
        return label;
    }
}
