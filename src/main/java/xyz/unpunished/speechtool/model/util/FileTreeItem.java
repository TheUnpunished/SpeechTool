package xyz.unpunished.speechtool.model.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FileTreeItem {

    private String name;
    private List<FileTreeItem> children;

    public FileTreeItem(String name) {
        this.name = name;
        children = new ArrayList<>();
    }

    public FileTreeItem addChild(FileTreeItem item){
        children.add(item);
        return children.get(children.size() - 1);
    }

    public Optional<FileTreeItem> findChild(String name){
        try{
            return Optional.of(
                    getChildren().stream()
                            .filter(fileTreeItem -> fileTreeItem.getName().equals(name))
                            .collect(Collectors.toList())
                            .get(0)
            );
        }
        catch (Exception e){
            return Optional.empty();
        }
    }
}
