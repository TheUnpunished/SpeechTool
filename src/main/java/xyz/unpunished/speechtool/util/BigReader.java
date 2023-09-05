package xyz.unpunished.speechtool.util;

import xyz.unpunished.speechtool.model.ui.FileComboTreeItem;
import xyz.unpunished.speechtool.model.ui.FileComboUI;
import xyz.unpunished.speechtool.model.ui.FileEntryUI;
import xyz.unpunished.speechtool.model.util.Endianness;
import xyz.unpunished.speechtool.model.util.FileTreeItem;
import xyz.unpunished.speechtool.model.util.FileType;
import xyz.unpunished.speechtool.model.util.Language;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import xyz.unpunished.speechtool.model.BigFile;
import xyz.unpunished.speechtool.model.DATSpeechFile;
import xyz.unpunished.speechtool.model.FileCombo;
import xyz.unpunished.speechtool.model.HDRSpeechFile;
import xyz.unpunished.speechtool.model.STHSpeechFile;
import xyz.unpunished.speechtool.model.SpeechFile;

public class BigReader {

    public static BigFile readBig(String bigFile, FileType type, Language language) throws IOException {
        BigFile big = new BigFile();
        big.setFileDir(bigFile);
        big.setFileType(type);
        big.setLanguage(language);
        byte[] buf;
        File f = new File(bigFile);
        BufferedInputStream is = new BufferedInputStream(new FileInputStream(f));
        SpeechReplacer.skip(4, is);
        buf = new byte[4];
        is.read(buf);
        big.setFileSize(HexReader.read32(buf, Endianness.LITTLE));
        is.read(buf);
        big.setFileCount(HexReader.read32(buf, Endianness.BIG));
        is.read(buf);
        big.setHeaderSize(HexReader.read32(buf, Endianness.BIG));
        SpeechFile[] entries = new SpeechFile[big.getFileCount()];
        for(int i = 0; i < big.getFileCount(); i ++){
            SpeechFile entry = new SpeechFile();
            is.read(buf);
            entry.setFileOffset(HexReader.read32(buf, Endianness.BIG));
            is.read(buf);
            entry.setFileSize(HexReader.read32(buf, Endianness.BIG));
            entry.setFileDir(HexReader.readS(is));
            entry.setFileType(type);
            entries[i] = entry;
        }
        big.setFiles(entries);
        switch (type){
            case DAT:{
                convertToDat(big);
                break;
            }
            case HDR:{
                convertToHDR(big);
                break;
            }
            case STH:{
                convertToSTH(big);
                break;
            }
        }
        is.close();
        return big;
    }

    public static void convertToHDR(BigFile bigFile) throws IOException {
        BufferedInputStream is = new BufferedInputStream(new FileInputStream(bigFile.getFileDir()));
        HDRSpeechFile[] hdrFiles = new HDRSpeechFile[bigFile.getFileCount()];
        SpeechReplacer.skip(bigFile.getFiles()[0].getFileOffset(), is);
        for(int i = 0; i < bigFile.getFileCount(); i ++)
            hdrFiles[i] = new HDRSpeechFile(bigFile.getFiles()[i]);
        for(int i = 0; i < bigFile.getFileCount(); i ++){
            byte[] buf = new byte[hdrFiles[i].getFileSize()];
            is.read(buf);
            hdrFiles[i].fromBuf(buf);
            if(i != (bigFile.getFileCount() - 1)){
                SpeechReplacer.skip(hdrFiles[i + 1].getFileOffset()
                        - (hdrFiles[i].getFileSize() + hdrFiles[i].getFileOffset()), is);
            }
        }
        bigFile.setFiles(hdrFiles);
        is.close();
    }

    public static void convertToSTH(BigFile bigFile) throws IOException {
        BufferedInputStream is = new BufferedInputStream(new FileInputStream(bigFile.getFileDir()));
        STHSpeechFile[] sthFiles = new STHSpeechFile[bigFile.getFileCount()];
        SpeechReplacer.skip(bigFile.getFiles()[0].getFileOffset(), is);
        for(int i = 0; i < bigFile.getFileCount(); i ++)
            sthFiles[i] = new STHSpeechFile(bigFile.getFiles()[i]);
        for(int i = 0; i < bigFile.getFileCount(); i ++){
            byte[] buf = new byte[sthFiles[i].getFileSize()];
            is.read(buf);
            sthFiles[i].fromBuf(buf);
            if(i != (bigFile.getFileCount() - 1)){
                SpeechReplacer.skip(sthFiles[i + 1].getFileOffset()
                        - (sthFiles[i].getFileSize() + sthFiles[i].getFileOffset()), is);
            }
        }
        bigFile.setFiles(sthFiles);
        is.close();
    }

    public static void convertToDat(BigFile bigFile) throws IOException {
        BufferedInputStream is = new BufferedInputStream(new FileInputStream(bigFile.getFileDir()));
        DATSpeechFile[] datFiles = new DATSpeechFile[bigFile.getFileCount()];
        SpeechReplacer.skip(bigFile.getFiles()[0].getFileOffset(), is);
        for(int i = 0; i < bigFile.getFileCount(); i ++)
            datFiles[i] = new DATSpeechFile(bigFile.getFiles()[i]);
        bigFile.setFiles(datFiles);
        is.close();
    }

    public static List<FileComboUI> fromComboToUI(FileCombo combo){
        List<FileComboUI> fileComboUIList = new ArrayList<>();
        for(int i = 0; i < combo.getSpeechFiles()[0].getFileCount(); i ++){
            HDRSpeechFile hdrSpeechFile = (HDRSpeechFile) combo.getSpeechFiles()[0].getFiles()[i];
            STHSpeechFile sthSpeechFile = (STHSpeechFile) combo.getSpeechFiles()[1].getFiles()[i];
            FileComboUI comboUI = new FileComboUI(hdrSpeechFile);
            for(int j = 0; j < hdrSpeechFile.getNumberOfFiles(); j++){
                FileEntryUI fileEntryUI = new FileEntryUI(hdrSpeechFile.getHDREntries()[j],
                sthSpeechFile.getSTHEntries()[j], (short) j);
                comboUI.getEntries()[j] = fileEntryUI;
            }
            fileComboUIList.add(comboUI);
        }
        return fileComboUIList;
    }

    public static FileTreeItem createFileTree(FileCombo combo, List<FileComboUI> fileComboUIList){
        FileListNavigator navigator = new FileListNavigator(new FileTreeItem("/"));
        for(FileComboUI fileComboUI: fileComboUIList){
            String[] navigation = fileComboUI.getFileName().split("\\\\");
            navigator.addItem(navigation);
        }
        return navigator.getRoot();
    }
}
