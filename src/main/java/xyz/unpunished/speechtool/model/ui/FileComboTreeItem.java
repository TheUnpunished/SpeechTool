package xyz.unpunished.speechtool.model.ui;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FileComboTreeItem {

    private String name;
    private boolean fileAttached;
    private FileComboUI attachedCombo;

    public void attachFile(FileComboUI file){
        attachedCombo = file;
        fileAttached = true;
    }

    public void deattachFile(){
        attachedCombo = null;
        fileAttached = false;
    }

    public FileComboTreeItem(String name){
        this.name = name;
        fileAttached = false;
        attachedCombo = null;
    }

    @Override
    public String toString() {
        return name;
    }
}
