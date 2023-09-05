package xyz.unpunished.speechtool.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.unpunished.speechtool.model.util.FileTreeItem;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class FileListNavigator {

    private FileTreeItem root;

    public boolean findItem(String[] navigation){
        FileTreeItem root = getRoot();
        for(String navigateTo: navigation){
            Optional<FileTreeItem> newRoot = navigate(navigateTo, root);
            if (newRoot.isPresent()){
                root = newRoot.get();
            }
            else {
                return false;
            }
        }
        return true;
    }

    public Optional<FileTreeItem> getItem(String[] navigation){
        FileTreeItem root = getRoot();
        for(String navigateTo: navigation){
            Optional<FileTreeItem> newRoot = navigate(navigateTo, root);
            if (newRoot.isPresent()){
                root = newRoot.get();
            }
            else {
                return Optional.empty();
            }
        }
        return Optional.of(root);
    }

    public Optional<FileTreeItem> navigate(String navigateTo, FileTreeItem root){
        try{
            return Optional.of(
                    root.getChildren().stream()
                            .filter(fileTreeItem -> fileTreeItem.getName().equals(navigateTo))
                            .collect(Collectors.toList())
                            .get(0)
            );
        }
        catch (Exception e){
            return Optional.empty();
        }
    }

    public boolean addItem(String[] navigation){
        int i = 0;
        String[] tempNav = Arrays.copyOfRange(navigation, 0, navigation.length - (i ++));
        Optional<FileTreeItem> tempParent = getItem(tempNav);
        while (!tempParent.isPresent()){
            tempNav = Arrays.copyOfRange(navigation, 0, navigation.length - (i ++));
            tempParent = getItem(tempNav);
        }
        if(i > 1){
            for (int j = i - 1; j > 0; j--){
                tempParent = Optional.of(tempParent.get().addChild(new FileTreeItem(navigation[navigation.length - j])));
            }
            return true;
        }
        else {
            return false;
        }
    }

}
