package xyz.unpunished.speechtool.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class DATSpeechFile extends SpeechFile {

    public DATSpeechFile(SpeechFile entry){
        setFileDir(entry.getFileDir());
        setFileType(entry.getFileType());
        setFileOffset(entry.getFileOffset());
        setFileSize(entry.getFileSize());
    }

}
