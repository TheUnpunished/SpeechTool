package xyz.unpunished.speechtool.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import xyz.unpunished.speechtool.model.BigFile;
import xyz.unpunished.speechtool.model.FileCombo;
import xyz.unpunished.speechtool.model.util.FileType;
import xyz.unpunished.speechtool.model.util.Game;
import xyz.unpunished.speechtool.model.util.Language;

import java.io.File;

@AllArgsConstructor
@Getter
@Setter
public class SpeechExplorer {

    private String pathToSpeech;

    public FileCombo getFileComboFromPath(Language language, Game game) throws Exception{
        File path = new File(pathToSpeech);
        if(path.isDirectory()){
            File hdrFile = new File(path.getAbsolutePath() + "/" + game.getStValue() + "speech"
                    + FileType.HDR.getFileTypeSt() + "_" + language.getLanguageSt() + ".big");
            File sthFile = new File(path.getAbsolutePath() + "/" + game.getStValue() + "speech"
                    + FileType.STH.getFileTypeSt() + "_" + language.getLanguageSt() + ".big");
            File datFile = new File(path.getAbsolutePath() + "/" + game.getStValue() + "speech"
                    + FileType.DAT.getFileTypeSt() + "_" + language.getLanguageSt() + ".big");
            return new FileCombo(
                    new BigFile[]{
                            BigReader.readBig(hdrFile.getAbsolutePath(), FileType.HDR, language),
                            BigReader.readBig(sthFile.getAbsolutePath(), FileType.STH, language),
                            BigReader.readBig(datFile.getAbsolutePath(), FileType.DAT, language),
                    }
            );
        }
        else
            throw new Exception();

    }

}
