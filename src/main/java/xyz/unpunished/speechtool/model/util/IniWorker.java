package xyz.unpunished.speechtool.model.util;

import javafx.scene.control.Alert;
import lombok.Getter;
import xyz.unpunished.speechtool.util.AlertCreator;

import java.io.*;
import java.util.Locale;

@Getter
public class IniWorker {

    private final File defaultIni;
    private String lastSpeechPath;
    private Game lastGame;
    private Language lastLanguage;
    private String lastImportPath;
    private String lastExportPath;
    private boolean editMode;
    private boolean keepBackups;
    private boolean newIni;
    private boolean darkMode;

    public void setLastSpeechPath(String lastSpeechPath) {
        this.lastSpeechPath = lastSpeechPath;
        rewriteIni(defaultIni);
    }

    public void setLastGame(Game lastGame) {
        this.lastGame = lastGame;
        rewriteIni(defaultIni);
    }

    public void setLastLanguage(Language lastLanguage) {
        this.lastLanguage = lastLanguage;
        rewriteIni(defaultIni);
    }

    public void setLastImportPath(String lastImportPath) {
        this.lastImportPath = lastImportPath;
        rewriteIni(defaultIni);
    }

    public void setLastExportPath(String lastExportPath) {
        this.lastExportPath = lastExportPath;
        rewriteIni(defaultIni);
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
        rewriteIni(defaultIni);
    }

    public void setKeepBackups(boolean keepBackups) {
        this.keepBackups = keepBackups;
        rewriteIni(defaultIni);
    }

    public void setLastSpeechPars(String lastSpeechPath, Game lastGame, Language lastLanguage){
        this.lastSpeechPath = lastSpeechPath;
        this.lastGame = lastGame;
        this.lastLanguage = lastLanguage;
        rewriteIni(defaultIni);
    }

    public  void setDarkMode(boolean darkMode){
        this.darkMode = darkMode;
        rewriteIni(defaultIni);
    }

    public IniWorker(String iniName){
        defaultIni = new File(iniName);
        initIni();
        if(defaultIni.exists()){
            readIniFile(defaultIni);
            newIni = false;
        }
        else {
            rewriteIni(defaultIni);
            newIni = true;
        }
    }
    
    private void initIni(){
        lastSpeechPath = "";
        lastGame = Game.OTHER;
        lastLanguage = Language.DEFAULT;
        lastImportPath = "";
        lastExportPath = "";
        editMode = false;
        keepBackups = true;
        darkMode = false;
    }

    private void readIniFile(File ini){    
        try{
            BufferedReader reader = new BufferedReader((new FileReader(ini)));
            lastSpeechPath = reader.readLine().split("\\s+=\\s+", 2)[1];
            lastGame = Game.valueOf(reader.readLine().split("\\s+=\\s+", 2)[1]);
            lastLanguage = Language.valueOf(reader.readLine().split("\\s+=\\s+", 2)[1]);
            lastImportPath = reader.readLine().split("\\s+=\\s+", 2)[1];
            lastExportPath = reader.readLine().split("\\s+=\\s+", 2)[1];
            editMode = Boolean.parseBoolean(reader.readLine().split("\\s+=\\s+", 2)[1]);
            keepBackups = Boolean.parseBoolean(reader.readLine().split("\\s+=\\s+", 2)[1]);
            darkMode = Boolean.parseBoolean(reader.readLine().split("\\s+=\\s+", 2)[1]);
            reader.close();
        }
        catch (Exception e){
            e.printStackTrace();
            AlertCreator.createAndWait(Alert.AlertType.ERROR,
                    "speechtool",
                    "Error",
                    "speechtool couldn't read .ini file");
            initIni();
            rewriteIni(ini);
            newIni = true;
        }
    }

    private void rewriteIni(File ini){
        try{
            if(!ini.exists()){
                ini.createNewFile();
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter(ini));
            bw.write("lastSpeechPath = " + lastSpeechPath); bw.newLine();
            bw.write("lastGame = " + lastGame.toString().toUpperCase(Locale.ROOT)); bw.newLine();
            bw.write("lastLanguage = " + lastLanguage.toString().toUpperCase(Locale.ROOT)); bw.newLine();
            bw.write("lastImportPath = " + lastImportPath); bw.newLine();
            bw.write("lastExportPath = " + lastExportPath); bw.newLine();
            bw.write("editMode = " + editMode); bw.newLine();
            bw.write("keepBackups = " + keepBackups); bw.newLine();
            bw.write("darkMode = " + darkMode);bw.newLine();
            bw.flush();
            bw.close();
        }
        catch (Exception e){
            e.printStackTrace();
            AlertCreator.createAndWait(Alert.AlertType.ERROR,
                    "speechtool",
                    "Error",
                    "speechtool failed to write to .ini file");
        }
    }


}
