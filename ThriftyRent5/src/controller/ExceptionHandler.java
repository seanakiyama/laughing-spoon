package controller;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;




public class ExceptionHandler {
    public static void ShowExceptionAlert(String message) {
        //Create the alert object
        Alert alert = new Alert(AlertType.WARNING);
        //set alert title as error
        alert.setTitle("Error");
        //set the message that will be shown on the alert
        alert.setContentText(message);
        //Show the alert and wait for the user to proceed
        alert.showAndWait();
    }
}
