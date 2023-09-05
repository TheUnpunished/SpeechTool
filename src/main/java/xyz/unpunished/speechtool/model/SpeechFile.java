package xyz.unpunished.speechtool.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.unpunished.speechtool.model.util.FileType;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SpeechFile {

    private String fileDir;
    private FileType fileType;
    private int fileOffset;
    private int fileSize;

}
