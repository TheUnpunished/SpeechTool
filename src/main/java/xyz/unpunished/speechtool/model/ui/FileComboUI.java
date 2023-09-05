package xyz.unpunished.speechtool.model.ui;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.unpunished.speechtool.model.HDRSpeechFile;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FileComboUI {

    private String fileName;
    private int blockSize;
    private int subId;
    private byte userDataSize;
    private int numberOfBlocks;
    private int numberOfSubBanks;
    private int numberOfFiles;
    private FileEntryUI[] entries;

    public FileComboUI(HDRSpeechFile hdrSpeechFile){
        fileName = hdrSpeechFile.getFileDir().substring(0, hdrSpeechFile.getFileDir().length() - 5);
        blockSize = hdrSpeechFile.getBlockSize();
        subId = hdrSpeechFile.getSubId();
        userDataSize = hdrSpeechFile.getUserDataSize();
        numberOfBlocks = hdrSpeechFile.getNumberOfBlocks();
        numberOfSubBanks = hdrSpeechFile.getNumberOfSubBanks();
        numberOfFiles = hdrSpeechFile.getNumberOfFiles();
        entries = new FileEntryUI[numberOfFiles];
    }

}
