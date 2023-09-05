package xyz.unpunished.speechtool.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.unpunished.speechtool.model.util.FileType;
import xyz.unpunished.speechtool.model.util.Language;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BigFile {

    private final String header = "BIG4";
    private String fileDir;
    private SpeechFile[] files;
    private FileType fileType;
    private Language language;
    private int fileSize;
    private int fileCount;
    private int headerSize; // ??


}
