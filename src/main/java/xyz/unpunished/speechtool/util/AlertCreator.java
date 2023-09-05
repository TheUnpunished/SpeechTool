package xyz.unpunished.speechtool.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import xyz.unpunished.speechtool.controller.MainController;

public class AlertCreator {

    public static Alert alert;

    public static void createAndWait(
            Alert.AlertType type,
            String title,
            String headerText,
            String contentText

    ){
        alert = new Alert(type);
        if(MainController.darkMode){
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(
                    AlertCreator.class.getResource("/static/css/darkmode.css").toExternalForm());
        }
        alert.setTitle(title);
        alert.setContentText(contentText);
        alert.setHeaderText(headerText);
        alert.setResult(ButtonType.OK);
        alert.showAndWait();
    }

    public static void createAndShow(
            Alert.AlertType type,
            String title,
            String headerText,
            String contentText

    ){
        alert = new Alert(type);
        if(MainController.darkMode){
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(
                    AlertCreator.class.getResource("/static/css/darkmode.css").toExternalForm());
        }
        alert.setTitle(title);
        alert.setContentText(contentText);
        alert.setHeaderText(headerText);
        alert.setResult(ButtonType.OK);
        alert.show();
    }

}
