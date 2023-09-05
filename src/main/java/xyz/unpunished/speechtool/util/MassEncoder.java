package xyz.unpunished.speechtool.util;

import javafx.application.Platform;
import lombok.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MassEncoder {

    private int threadCount;
    private List<File> files;

    @AllArgsConstructor
    public class EncoderParallel implements Runnable{

        private int threadNum;

        @SneakyThrows
        @Override
        public void run() {
            Runtime runtime = Runtime.getRuntime();
            for(int i = threadNum; i < files.size(); i += threadCount){
                String cmd = "snrtool/snrtool.exe " + "temp/" + files.get(i).getName();
                Process pr = runtime.exec(cmd);
                pr.waitFor();
                pr.destroy();
            }
        }
    }

    public void massEncode() throws InterruptedException, IOException {
        File tempDir = new File("temp");
        tempDir.mkdirs();
        for(File f: files){
            Files.copy(f.toPath(), new File("temp/" + f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        Thread[] thread = new Thread[threadCount];
        for(int i = 0; i < threadCount; i ++){
            thread[i] = new Thread(new EncoderParallel(i));
            thread[i].start();
        }
        for(int i = 0; i < threadCount; i++){
            thread[i].join();
        }
    }

}
