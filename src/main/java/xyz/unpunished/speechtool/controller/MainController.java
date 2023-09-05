package xyz.unpunished.speechtool.controller;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.*;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;
import xyz.unpunished.speechtool.model.FileCombo;
import xyz.unpunished.speechtool.model.ui.FileComboTreeItem;
import xyz.unpunished.speechtool.model.ui.FileComboUI;
import xyz.unpunished.speechtool.model.ui.FileEntryUI;
import xyz.unpunished.speechtool.model.util.FileTreeItem;
import xyz.unpunished.speechtool.model.util.Game;
import xyz.unpunished.speechtool.model.util.IniWorker;
import xyz.unpunished.speechtool.model.util.Language;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import javafx.scene.image.Image;
import xyz.unpunished.speechtool.util.AlertCreator;
import xyz.unpunished.speechtool.util.SpeechExplorer;
import xyz.unpunished.speechtool.util.SpeechExtractor;
import xyz.unpunished.speechtool.util.SpeechPlayer;
import xyz.unpunished.speechtool.util.SpeechReplacer;

@Getter
@Setter
public class MainController implements Initializable {

    private FileCombo combo;
    private List<FileComboUI> fileComboUIList;
    private TreeItem<FileComboTreeItem> selectedTreeItem;
    private Language currentLanguage = Language.DEFAULT;
    private Game currentGame = Game.OTHER;
    private boolean editMode = true;
    private boolean keepBackups = true;
    public static boolean darkMode = false;
    private boolean wasDarkMode = false;
    private Stage dialogStage;
    private ChangeListener<TreeItem<FileComboTreeItem>> listener;
    private IniWorker iniWorker;
    private ObservableList<FileEntryUI> entryList = FXCollections.observableArrayList();
    private int add;


    @FXML
    private AnchorPane lowPane;
    @FXML
    private TreeView<FileComboTreeItem> treeView;
    @FXML
    private ListView<FileEntryUI> entryListView;
    @FXML
    private Label currentFileLabel;
    @FXML
    private Label currentModeLabel;
    @FXML
    private Label numOfFilesLabel;
    @FXML
    private Label subIdLabel;
    @FXML
    private Label numOfBlocksLabel;
    @FXML
    private Label blockSizeLabel;
    @FXML
    private Label numOfSubBanksLabel;
    @FXML
    private Label uDataSizeLabel;
    @FXML
    private CheckMenuItem changeKeepBackupsMenuItem;
    @FXML
    private CheckMenuItem changeDarkModeMenuItem;


    @SneakyThrows
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        listener = (observableValue, fileComboTreeItemTreeItem, t1) -> {
            if(t1.getValue().isFileAttached()){
                if (t1.getValue().getAttachedCombo().getEntries().length > 0) {
                    entryList.clear();
                    entryList.addAll(Arrays.asList(t1.getValue().getAttachedCombo().getEntries()));
                    selectedTreeItem = t1;
                    entryListView.getSelectionModel().select(-1);
                    setSelectedTreeItemData();
                }
            }
        };
        entryListView.setItems(entryList);
        iniWorker = new IniWorker("speechTool.ini");
        if(!iniWorker.isNewIni() && new File(iniWorker.getLastSpeechPath()).exists()){
            initDirectory(iniWorker.getLastSpeechPath(), iniWorker.getLastLanguage(), iniWorker.getLastGame());
        }
        keepBackups = iniWorker.isKeepBackups();
        editMode = iniWorker.isEditMode();
        changeModeLabel();
        changeKeepBackupsMenuItem.setSelected(keepBackups);
    }

    public void initDirectory(String dir, Language language, Game game) {
        try{
            SpeechExplorer explorer = new SpeechExplorer(dir);
            currentLanguage = language;
            currentGame = game;
            combo = explorer.getFileComboFromPath(language, game);
            setupFromCombo();
            iniWorker.setLastSpeechPars(dir, game, language);
        }
        catch (Exception e){
            e.printStackTrace();
            AlertCreator.createAndWait(Alert.AlertType.ERROR,
            "Error!",
            "Error",
            "Error opening speech folder. Check location and settings");
        }
    }

    private void setupFromCombo(){
        entryList.clear();
        fileComboUIList = xyz.unpunished.speechtool.util.BigReader.fromComboToUI(combo);
        FileTreeItem item = xyz.unpunished.speechtool.util.BigReader.createFileTree(combo, fileComboUIList);
        TreeItem<FileComboTreeItem> rootItem = new TreeItem<>(
                new FileComboTreeItem("Speech")
        );
        rootItem.setExpanded(true);
        addChildrenToRoot(rootItem, item);
        attachFiles(rootItem, "", fileComboUIList);
        treeView.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if(mouseEvent.getClickCount() == 2){
                    TreeItem<FileComboTreeItem> comboTreeItem = treeView.getSelectionModel().getSelectedItem();
                    if(comboTreeItem !=null && !comboTreeItem.getValue().isFileAttached()) {
                        comboTreeItem.setExpanded(!comboTreeItem.isExpanded());
                    }
                }
            }
            if(mouseEvent.getButton().equals(MouseButton.SECONDARY)){
                TreeItem<FileComboTreeItem> comboTreeItem = treeView.getSelectionModel().getSelectedItem();
                if(!comboTreeItem.getValue().isFileAttached()) {
                    comboTreeItem.setExpanded(!comboTreeItem.isExpanded());
                }
            }
        });
        treeView.getSelectionModel().selectedItemProperty().removeListener(listener);
        treeView.setRoot(rootItem);
        treeView.getSelectionModel().selectedItemProperty().addListener(listener);
        entryListView.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                if(mouseEvent.getClickCount() == 2){
                    if(editMode){
                        replaceWith();
                    }
                    else {
                        playCurrent();
                    }
                }
            }
        });
    }

    private void refresh(){
        MultipleSelectionModel multipleSelectionModel = treeView.getSelectionModel();
        int treeIndex = treeView.getRow(selectedTreeItem);
        int listIndex = entryListView.getSelectionModel().getSelectedIndex();
        List<Integer> rows = new ArrayList<>();
        add = 0;
        getExpandedItems(treeView.getRoot(), true, rows);
        setupFromCombo();
        for(Integer row: rows){
            multipleSelectionModel.select(row.intValue());
            treeView.getSelectionModel().getSelectedItem().setExpanded(true);
        }
        multipleSelectionModel.select(treeIndex);
        entryList.clear();
        entryList.addAll(Arrays.asList(selectedTreeItem.getValue().getAttachedCombo().getEntries()));
        entryListView.getSelectionModel().select(listIndex);
    }

    private void getExpandedItems(TreeItem item, boolean isRoot, List<Integer> rows){
        List<TreeItem> treeItems = item.getChildren();
        if(item.isExpanded() && !isRoot){
            add += treeItems.size();
            rows.add(treeView.getRow(item));
            for(TreeItem treeItem: treeItems){
                getExpandedItems(treeItem, false, rows);
            }
        }
        if(isRoot){
            for(TreeItem treeItem: treeItems){
                getExpandedItems(treeItem, false, rows);
            }
        }

    }

    @FXML
    private void open() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/static/FXML/openFolder.fxml"));
        Parent root = loader.load();
        OpenFolderController controller = loader.getController();
        controller.setController(this);
        Scene scene = new Scene(root);
        if(darkMode){
            String darkStyles = getClass().getResource("/static/css/darkmode.css").toExternalForm();
            scene.getStylesheets().add(darkStyles);
        }
        Stage stage = new Stage();
        stage.setResizable(false);
        stage.setTitle("Open speech path");
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(blockSizeLabel.getScene().getWindow());
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/static/icon/icon.png")));
        stage.setScene(scene);
        controller.setDialogStage(stage);
        controller.setPrevSpeechPath(iniWorker.getLastSpeechPath());
        controller.getGamePathField().setText(iniWorker.getLastSpeechPath());
        controller.getGameBox().getSelectionModel().select(iniWorker.getLastGame());
        controller.getLanguageBox().getSelectionModel().select(iniWorker.getLastLanguage());
        stage.show();
    }

    @FXML
    private void exportLine(){
        if(selectedTreeItem == null || entryListView.getSelectionModel().getSelectedIndex() < 0)
            AlertCreator.createAndWait(
                    Alert.AlertType.ERROR,
                    "Error!",
                    "Error",
                    "No file/line is currently selected."

            );
        else {
            FileChooser chooser = new FileChooser();
            FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter(
                    "MPEG3 Audio File", "*.mp3"
            );
            chooser.getExtensionFilters().add(extensionFilter);
            if(iniWorker.getLastExportPath() != null && !iniWorker.getLastExportPath().equals("")){
                File f = new File(iniWorker.getLastExportPath());
                if (f.exists()){
                    chooser.setInitialDirectory(f);
                }
            }
            File f = chooser.showSaveDialog(dialogStage);
            if(f != null)
                try{
                    AlertCreator.createAndShow(Alert.AlertType.NONE,
                            "SpeechTool",
                            "Extraction",
                            "Extracting...");
                    SpeechExtractor.extractFile(combo,
                            indexFromName(nameFromFile()),
                            indexFromList(),
                            f);
                    AlertCreator.alert.close();
                    iniWorker.setLastExportPath(FilenameUtils.getFullPath(f.getAbsolutePath()));
                }
                catch (Exception e){
                    AlertCreator.alert.close();
                    e.printStackTrace();
                    AlertCreator.createAndWait(Alert.AlertType.ERROR,
                            "Error!",
                            "Error",
                            "An error has occurred. Check CMD window.");
                }
        }
    }

    @FXML
    private void exportFile(){
        if(selectedTreeItem == null)
            AlertCreator.createAndWait(
                    Alert.AlertType.ERROR,
                    "Error!",
                    "Error",
                    "No file is currently selected."

            );
        else {
            DirectoryChooser chooser = new DirectoryChooser();
            if(iniWorker.getLastExportPath() != null && !iniWorker.getLastExportPath().equals("")){
                File f = new File(iniWorker.getLastExportPath());
                if(f.exists()){
                    chooser.setInitialDirectory(f);
                }
            }
            File f = chooser.showDialog(dialogStage);
            if(f != null && f.exists() && f.isDirectory())
                try{
                    AlertCreator.createAndShow(Alert.AlertType.NONE,
                            "SpeechTool",
                            "Extraction",
                            "Extracting...");
                    SpeechExtractor.extractFile(combo,
                            indexFromName(nameFromFile()),
                            f);
                    AlertCreator.alert.close();
                    iniWorker.setLastExportPath(f.getAbsolutePath());
                }
                catch (Exception e){
                    AlertCreator.alert.close();
                    e.printStackTrace();
                    AlertCreator.createAndWait(Alert.AlertType.ERROR,
                            "Error!",
                            "Error",
                            "An error has occurred. Check CMD window.");
                }
        }
    }

    @FXML
    private void quit(){
        System.exit(0);
    }

    @FXML
    private void replaceWith(){
        if(selectedTreeItem == null || entryListView.getSelectionModel().getSelectedIndex() < 0)
            AlertCreator.createAndWait(
                    Alert.AlertType.ERROR,
                    "Error!",
                    "Error",
                    "No file/line is currently selected."

            );
        else {
            FileChooser chooser = new FileChooser();
            FileChooser.ExtensionFilter allSuppExtensionFilter = new FileChooser.ExtensionFilter(
                    "All supported types", "*.wav", "*.mp3"
            );
            FileChooser.ExtensionFilter mp3ExtensionFilter = new FileChooser.ExtensionFilter(
                    "MPEG3 Audio File", "*.mp3"
            );
            FileChooser.ExtensionFilter wavExtensionFilter = new FileChooser.ExtensionFilter(
                    "Waveform Audio File", "*.wav"
            );
            FileChooser.ExtensionFilter allExtensionFilter = new FileChooser.ExtensionFilter(
                    "All file types", "*.*"
            );
            chooser.getExtensionFilters().add(allSuppExtensionFilter);
            chooser.getExtensionFilters().add(mp3ExtensionFilter);
            chooser.getExtensionFilters().add(wavExtensionFilter);
            chooser.getExtensionFilters().add(allExtensionFilter);
            if(iniWorker.getLastImportPath() != null && !iniWorker.getLastImportPath().equals("")){
                File f = new File(iniWorker.getLastImportPath());
                if(f.exists()){
                    chooser.setInitialDirectory(f);
                }
            }
            File f = chooser.showOpenDialog(dialogStage);
            if(f != null)
                try{
                    AlertCreator.createAndShow(Alert.AlertType.NONE,
                            "SpeechTool",
                            "Replacement",
                            "Replacing...");
                    int IFN = indexFromName(nameFromFile());
                    int IFL = indexFromList();
                    if(keepBackups){
                        SpeechExtractor.createBackup(combo, currentGame, IFN, currentFileLabel.getText());
                    }
                    SpeechReplacer.replaceSound(combo,
                            IFN,
                            IFL,
                            f);
                    refresh();
                    AlertCreator.alert.close();
                    iniWorker.setLastImportPath(FilenameUtils.getFullPath(f.getAbsolutePath()));
                    AlertCreator.createAndWait(
                            Alert.AlertType.INFORMATION,
                            "speechtool",
                            "Success",
                            "Speech replaced");
                }
                catch (Exception e){
                    AlertCreator.alert.close();
                    e.printStackTrace();
                    AlertCreator.createAndWait(Alert.AlertType.ERROR,
                            "Error!",
                            "Error",
                            "An error has occurred. Check CMD window.");
                }
        }
    }

    @FXML
    private void bulkReplace(){
        if(selectedTreeItem == null)
            AlertCreator.createAndWait(
                    Alert.AlertType.ERROR,
                    "Error!",
                    "Error",
                    "No file/line is currently selected."

            );
        else {
            DirectoryChooser chooser = new DirectoryChooser();
            if(iniWorker.getLastImportPath() != null && !iniWorker.getLastImportPath().equals("")){
                File f = new File(iniWorker.getLastImportPath());
                if(f.exists()){
                    chooser.setInitialDirectory(f);
                }
            }
            File f = chooser.showDialog(dialogStage);
            if(f != null){
                int mp3Count = f.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".mp3")).length;
                int wavCount = f.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".wav")).length;
                int required = entryListView.getItems().size();
                if(mp3Count != required && wavCount != required)
                    AlertCreator.createAndWait(Alert.AlertType.ERROR,
                            "speechtool",
                            "Error",
                            "Amount of audio files is not equal. Needed value: "
                                    + required);
                else{
                    try{
                        AlertCreator.createAndShow(Alert.AlertType.NONE,
                                "SpeechTool",
                                "Replacement",
                                "Replacing...");
                        int IFN = indexFromName(nameFromFile());
                        if(keepBackups){
                            SpeechExtractor.createBackup(combo, currentGame, IFN, currentFileLabel.getText());
                        }
                        SpeechReplacer.replaceSound(combo,
                                IFN,
                                f);
                        refresh();
                        AlertCreator.alert.close();
                        AlertCreator.createAndWait(
                            Alert.AlertType.INFORMATION,
                            "speechtool",
                            "Success",
                            "Speech replaced");
                    }
                    catch (Exception e){
                        AlertCreator.alert.close();
                        e.printStackTrace();
                        AlertCreator.createAndWait(Alert.AlertType.ERROR,
                                "Error!",
                                "Error",
                                "An error has occurred. Check CMD window.");
                    }
                }
            }
        }
    }

    @FXML
    private void changeKeepBackups(){
        keepBackups = !keepBackups;
        changeKeepBackupsMenuItem.setSelected(keepBackups);
        iniWorker.setKeepBackups(keepBackups);
    }

    @FXML
    private void changeDarkMode(){
        darkMode = !darkMode;
        changeDarkModeMenuItem.setSelected(darkMode);
        executeDarkMode();
        iniWorker.setDarkMode(darkMode);
    }

    @FXML
    public void executeDarkMode(){
        String stockStyles = getClass().getResource("/static/css/styles.css").toExternalForm();
        String darkStyles = getClass().getResource("/static/css/darkmode.css").toExternalForm();
        if(darkMode){
            dialogStage.getScene().getStylesheets().add(darkStyles);
            wasDarkMode = true;
            lowPane.setBorder(new Border(new BorderStroke(Color.web("#3C3C3C"),
                    BorderStrokeStyle.SOLID,
                    CornerRadii.EMPTY,
                    new BorderWidths(1.0))));;
        }
        else{
            lowPane.setBorder(new Border(new BorderStroke(Color.web("#c8c8c8"),
                    BorderStrokeStyle.SOLID,
                    CornerRadii.EMPTY,
                    new BorderWidths(1.0))));;
            if(wasDarkMode){
                dialogStage.getScene().getStylesheets().clear();
                dialogStage.getScene().getStylesheets().add(stockStyles);
            }
        }
    }

    @FXML
    private void changeMode(){
        editMode = !editMode;
        iniWorker.setEditMode(editMode);
        changeModeLabel();
    }

    private void changeModeLabel(){
        currentModeLabel.setText(
                (editMode ? "Edit" : "Play") + " (" + currentLanguage.getLanguageSt().toUpperCase(Locale.ROOT)
                        + ")"
        );
        if(!editMode){
            currentModeLabel.setStyle("-fx-font-weight: normal");
            currentModeLabel.setTextFill(Color.rgb(0,0, 0));
        }
        else{
            currentModeLabel.setStyle("-fx-font-weight: bold");
            currentModeLabel.setTextFill(Color.rgb(255,0, 0));
        }
    }

    @FXML
    private void about(){
        AlertCreator.createAndWait(Alert.AlertType.INFORMATION,
                "speechtool",
                "SpeechTool v1.0 by The_Unpunished",
                "Press OK for Widega");
    }

    private void setSelectedTreeItemData(){
        FileComboUI item = selectedTreeItem.getValue().getAttachedCombo();
        currentFileLabel.setText(nameFromFile());
        numOfFilesLabel.setText(Integer.toString(item.getNumberOfFiles()));
        subIdLabel.setText(Integer.toString(item.getSubId()));
        numOfBlocksLabel.setText(Integer.toString(item.getNumberOfBlocks()));
        blockSizeLabel.setText(Integer.toString(item.getBlockSize()));
        numOfSubBanksLabel.setText(Integer.toString(item.getNumberOfSubBanks()));
        uDataSizeLabel.setText(Byte.toString(item.getUserDataSize()) + " byte(s)");
    }

    private void attachFiles(TreeItem<FileComboTreeItem> root, String dir, List<FileComboUI> fileComboUIList){
        for(TreeItem<FileComboTreeItem> item: root.getChildren()){
            if(item.getChildren().size() > 0){
                attachFiles(item, dir + (dir.equals("") ? "" : "\\") + item.getValue().toString(), fileComboUIList);
            }
            else {
                String tempDir = dir + (dir.equals("") ? "" : "\\")  + item.getValue().toString();
                List<FileComboUI> tempList = fileComboUIList.stream()
                        .filter(fileComboUI -> fileComboUI.getFileName().equals(tempDir))
                        .collect(Collectors.toList());
                if(tempList.size() > 0){
                    FileComboTreeItem treeItem = new FileComboTreeItem(
                            item.getValue().toString(),
                            true,
                            tempList.get(0)
                    );
                    item.setValue(treeItem);
                }
            }
        }
    }

    private void addChildrenToRoot(TreeItem<FileComboTreeItem> root, FileTreeItem item){
        for(FileTreeItem fileTreeItem: item.getChildren()){
            root.getChildren().add(new TreeItem<>(new FileComboTreeItem(fileTreeItem.getName())));
            TreeItem<FileComboTreeItem> currentParent = root.getChildren().get(root.getChildren().size() - 1);
            if(fileTreeItem.getChildren().size() > 0){
                addChildrenToRoot(currentParent, fileTreeItem);
            }
        }
    }

    private String nameFromFile(){
        TreeItem<FileComboTreeItem> item = selectedTreeItem;
        TreeItem<FileComboTreeItem> parent = item.getParent();
        String path = "\\" + parent.getValue().toString() + "\\" + item.getValue().toString();
        while ((parent = parent.getParent()) != null){
            path = "\\" + parent.getValue().getName() + path;
        }
        path = path.split("Speech\\\\")[1];
        return path;
    }

    private int indexFromName(String name){
        for(int i = 0; i < fileComboUIList.size(); i ++){
            if (name.equals(fileComboUIList.get(i).getFileName()))
                return i;
        }
        return -1;
    }

    private int indexFromList(){
        return entryListView.getSelectionModel().getSelectedIndex();
    }

    private void playCurrent() {
        try{
            SpeechPlayer.playSound(
                    combo, indexFromName(nameFromFile()), indexFromList()
            );
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
