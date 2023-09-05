package xyz.unpunished.speechtool.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import xyz.unpunished.speechtool.model.DATSpeechFile;
import xyz.unpunished.speechtool.model.FileCombo;
import xyz.unpunished.speechtool.model.STHEntry;
import xyz.unpunished.speechtool.model.STHSpeechFile;
import xyz.unpunished.speechtool.model.util.Game;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Locale;

public class SpeechExtractor {

    public static void createTempWAV(FileCombo fc, int fileIndex, int soundIndex) throws IOException, InterruptedException {
        STHSpeechFile sth = (STHSpeechFile) fc.getSpeechFiles()[1].getFiles()[fileIndex];
        DATSpeechFile dat = (DATSpeechFile) fc.getSpeechFiles()[2].getFiles()[fileIndex];
        STHEntry sthEntry = sth.getSTHEntries()[soundIndex];
        BufferedInputStream is = new BufferedInputStream(
                new FileInputStream(fc.getSpeechFiles()[2].getFileDir()));
        int skipSize = dat.getFileOffset() + sthEntry.getOffset();
        SpeechReplacer.skip(skipSize, is);
        File tempFile = new File("temp.dat");
        BufferedOutputStream os = new BufferedOutputStream(
                new FileOutputStream(tempFile)
        );
        byte[] buf = new byte[(soundIndex == sth.getSTHEntries().length - 1)
                ? dat.getFileSize() - sthEntry.getOffset()
                : sth.getSTHEntries()[soundIndex + 1].getOffset() - sthEntry.getOffset()];
        is.read(buf);
        is.close();
        os.write(buf);
        os.flush();
        os.close();
        String cmd = "ealayer/ealayer3.exe temp.dat -w";
        Runtime runtime = Runtime.getRuntime();
        Process pr = runtime.exec(cmd);
        pr.waitFor();
        pr.destroy();
        tempFile.delete();
    }

    public static void createTempWAV(FileCombo fc, int fileIndex) throws IOException, InterruptedException {
        DATSpeechFile dat = (DATSpeechFile) fc.getSpeechFiles()[2].getFiles()[fileIndex];
        BufferedInputStream is = new BufferedInputStream(
                new FileInputStream(fc.getSpeechFiles()[2].getFileDir()));
        int skipSize = dat.getFileOffset();
        SpeechReplacer.skip(skipSize, is);
        File tempDir = new File("temp");
        if(!tempDir.exists())
            tempDir.mkdir();
        File tempFile = new File("temp/temp.dat");
        BufferedOutputStream os = new BufferedOutputStream(
                new FileOutputStream(tempFile)
        );
        skipSize = dat.getFileSize();
        SpeechReplacer.skipWrite(skipSize, is, os);
        os.flush();
        os.close();
        String cmd = "ealayer/ealayer3.exe temp/temp.dat -w";
        Runtime runtime = Runtime.getRuntime();
        Process pr = runtime.exec(cmd);
        pr.waitFor();
        pr.destroy();
        tempFile.delete();
    }

    public static void extractFile(FileCombo fc, int fileIndex, int soundIndex, File moveTo) throws IOException, InterruptedException {
        createTempWAV(fc, fileIndex, soundIndex);
        Files.move(new File("temp.wav").toPath(), moveTo.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void extractFile(FileCombo fc, int fileIndex, File moveTo) throws IOException, InterruptedException {
        createTempWAV(fc, fileIndex);
        File tempDir = new File("temp");
        moveTo.mkdir();
        ArrayList<File> resultFiles = new ArrayList<>(
                FileUtils.listFiles(tempDir, new String[]{"wav"}, false)
        );
        for(File f: resultFiles){
            Files.move(
                    f.toPath(),
                    new File(moveTo.getAbsolutePath() + "/" + f.getName()).toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    public static void createBackup(FileCombo fc, Game game, int fileIndex, int soundIndex, String fileLocation) throws IOException, InterruptedException {
        createTempWAV(fc, fileIndex, soundIndex);
        File f = new File("BACKUP/" + game.toString().toUpperCase(Locale.ROOT)
                + "/" + fileLocation.replaceAll("\\\\", "/"));
        f.mkdirs();
        String absPath = f.getAbsolutePath();
        f = new File(
                absPath + "/" + absPath.substring(absPath.length() - 2)
                + (soundIndex > 0 ? "_part" + soundIndex : "")
                + ".bak0.wav"
        );
        while (f.exists()){
            String ext = FilenameUtils.getExtension(FilenameUtils.removeExtension(f.getAbsolutePath()));
            ext = "bak" + (Integer.parseInt(ext.substring(3)) + 1);
            f = new File(
                    FilenameUtils.removeExtension(FilenameUtils.removeExtension(f.getAbsolutePath())) 
                            + "." + ext + ".wav"
            );
        }
        Files.move(new File("temp.wav").toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void createBackup(FileCombo fc, Game game, int fileIndex, String fileLocation) throws IOException, InterruptedException {
        createTempWAV(fc, fileIndex);
        File tempDir = new File("temp");
        File moveTo = new File("BACKUP/" + game.toString().toUpperCase(Locale.ROOT)
                + "/" + fileLocation.replaceAll("\\\\", "/"));
        moveTo.mkdirs();
        ArrayList<File> resultFiles = new ArrayList<>(
                FileUtils.listFiles(tempDir, new String[]{"wav"}, false)
        );
        String absPath = moveTo.getAbsolutePath();
        for(int soundIndex = 0; soundIndex < resultFiles.size(); soundIndex++){
            moveTo = new File(
                    absPath + "/" + absPath.substring(absPath.length() - 2)
                            + (soundIndex > 0 ? "_part" + soundIndex : "")
                            + ".bak0.wav"
            );
            while (moveTo.exists()){
                String ext = FilenameUtils.getExtension(FilenameUtils.removeExtension(moveTo.getAbsolutePath()));
                ext = "bak" + (Integer.parseInt(ext.substring(3)) + 1);
                moveTo = new File(
                        FilenameUtils.removeExtension(FilenameUtils.removeExtension(moveTo.getAbsolutePath())) 
                                + "." + ext + ".wav"
                );
            }
            Files.move(
                    resultFiles.get(soundIndex).toPath(),
                    moveTo.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

}
