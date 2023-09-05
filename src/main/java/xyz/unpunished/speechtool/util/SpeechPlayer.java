package xyz.unpunished.speechtool.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.io.*;
import xyz.unpunished.speechtool.model.FileCombo;

@AllArgsConstructor
@Getter
@Setter
public class SpeechPlayer {

    public static void playSound(FileCombo fc, int fileIndex, int soundIndex) throws IOException, InterruptedException {
        SpeechExtractor.createTempWAV(fc, fileIndex, soundIndex);
        Desktop.getDesktop().open(new File("temp.wav"));
    }

}
