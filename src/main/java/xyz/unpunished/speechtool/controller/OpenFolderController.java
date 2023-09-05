package xyz.unpunished.speechtool.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import xyz.unpunished.speechtool.model.util.Game;
import xyz.unpunished.speechtool.model.util.Language;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

@Getter
@Setter
public class OpenFolderController implements Initializable {

    private Stage dialogStage;
    private MainController controller;
    private String prevSpeechPath;

    @FXML
    private TextField gamePathField;
    @FXML
    private ComboBox<Game> gameBox;
    @FXML
    private ComboBox<Language> languageBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gameBox.setItems(FXCollections.observableArrayList(Game.values()));
        gameBox.getSelectionModel().select(0);
        languageBox.setItems(FXCollections.observableArrayList(Language.values()));
        languageBox.getSelectionModel().select(0);
    }

    @FXML
    private void selectPath(){
        String speechLocation = gamePathField.getText();
        DirectoryChooser dc = new DirectoryChooser();
        if(speechLocation != null || !speechLocation.equals("")){
            File f = new File(speechLocation);
            if(f.exists() && f.isDirectory()){
                dc.setInitialDirectory(f);
            }
        }
        File f = dc.showDialog(dialogStage);
        if(f != null && f.exists()){
            gamePathField.setText(f.getAbsolutePath());
        }
    }

    @FXML
    private void cancelAction(){
        dialogStage.close();
    }

    @FXML
    private void okAction() throws Exception {
        controller.initDirectory(gamePathField.getText(), languageBox.getValue(), gameBox.getValue());
        dialogStage.close();
    }

}
