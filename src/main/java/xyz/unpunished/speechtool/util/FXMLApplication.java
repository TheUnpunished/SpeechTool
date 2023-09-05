package xyz.unpunished.speechtool.util;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import xyz.unpunished.speechtool.Main;
import xyz.unpunished.speechtool.controller.MainController;
import xyz.unpunished.speechtool.model.util.IniWorker;

public class FXMLApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/static/FXML/main.fxml"));
        Parent root = loader.load();
        MainController controller = loader.getController();
        Scene scene = new Scene(root);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/static/icon/icon.png")));
        stage.setTitle("NFS EALAYER3 Speech Tool");
        stage.setScene(scene);
        IniWorker iniWorker = new IniWorker("speechTool.ini");
        MainController.darkMode = iniWorker.isDarkMode();
        controller.setWasDarkMode(MainController.darkMode);
        controller.getChangeDarkModeMenuItem().setSelected(MainController.darkMode);
        if(MainController.darkMode){
            stage.getScene().getStylesheets().add(getClass().getResource("/static/css/darkmode.css").toExternalForm());
        }
        stage.setOnCloseRequest(event -> {
            event.consume();
            System.exit(0);
        });
        controller.setDialogStage(stage);
        stage.show();
    }
}
