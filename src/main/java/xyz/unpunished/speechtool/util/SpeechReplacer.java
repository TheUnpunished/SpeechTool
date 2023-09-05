package xyz.unpunished.speechtool.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import xyz.unpunished.speechtool.model.DATSpeechFile;
import xyz.unpunished.speechtool.model.FileCombo;
import xyz.unpunished.speechtool.model.STHEntry;
import xyz.unpunished.speechtool.model.STHSpeechFile;
import xyz.unpunished.speechtool.model.util.FileNameComparator;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class SpeechReplacer {

    private static int jumpSize = 16384;

    public static void replaceSound(FileCombo fc, int fileIndex, int soundIndex, File replaceWith) throws IOException, InterruptedException {
        File dest = new File("temp." + FilenameUtils.getExtension(replaceWith.getName()));
        FileUtils.copyFile(replaceWith, dest);
        Runtime runtime = Runtime.getRuntime();
        String cmd = "snrtool/snrtool.exe temp." + FilenameUtils.getExtension(replaceWith.getName());
        Process process = runtime.exec(cmd);
        process.waitFor();
        process.destroy();
        STHSpeechFile sth = (STHSpeechFile) fc.getSpeechFiles()[1].getFiles()[fileIndex];
        DATSpeechFile dat = (DATSpeechFile) fc.getSpeechFiles()[2].getFiles()[fileIndex];
        File snr = new File("temp."
                + FilenameUtils.getExtension(replaceWith.getName())
                + ".snr");
        File sns = new File("temp."
                + FilenameUtils.getExtension(replaceWith.getName())
                + ".sns");
        byte[] buf;
        byte[] snrBuf = new byte[8];
        {
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(snr));
            is.read(snrBuf);
            is.close();
        }
        int sizeDif;
        int oldSize;
        STHEntry entry;
        {
            ByteBuffer byteBuf = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
            byteBuf.order(ByteOrder.LITTLE_ENDIAN);
            byteBuf.putInt(sth.getSTHEntries()[soundIndex].getOffset());
            entry = STHEntry.fromBuf(ArrayUtils.addAll(byteBuf.array(), snrBuf));
            sth.getSTHEntries()[soundIndex] = entry;
            oldSize = (soundIndex == (sth.getSTHEntries().length - 1))
                    ? dat.getFileSize() - entry.getOffset()
                    : sth.getSTHEntries()[soundIndex + 1].getOffset() - entry.getOffset();
            sizeDif = (int) sns.length() - oldSize;
        }
        // writing sth
        {
            File src = new File(fc.getSpeechFiles()[1].getFileDir());
            File temp = new File(fc.getSpeechFiles()[1].getFileDir() + ".temp");
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(
                    src
            ));
            BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(
                    temp
            ));
            int skipSize = sth.getFileOffset() + soundIndex * 12;
            skipWrite(skipSize, is, os);
            {
                ByteBuffer byteBuf = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
                byteBuf.order(ByteOrder.LITTLE_ENDIAN);
                byteBuf.putInt(entry.getOffset());
                os.write(byteBuf.array());
                os.write(snrBuf);
            }
            skip(12, is);
            skipSize += 12;
            for (int i = soundIndex + 1; i < sth.getSTHEntries().length; i++) {
                sth.getSTHEntries()[i].setOffset(
                        sth.getSTHEntries()[i].getOffset() + sizeDif
                );
                ByteBuffer byteBuf = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
                byteBuf.order(ByteOrder.LITTLE_ENDIAN);
                byteBuf.putInt(
                        sth.getSTHEntries()[i].getOffset()
                );
                os.write(byteBuf.array());
                skip(4, is);
                buf = new byte[8];
                is.read(buf);
                os.write(buf);
                skipSize += 12;
            }
            skipSize = fc.getSpeechFiles()[1].getFileSize() - skipSize;
            skipWrite(skipSize, is, os);
            is.close();
            os.flush();
            os.close();
            Files.delete(src.toPath());
            Files.move(temp.toPath(), src.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        // writing dat
        {
            File src = new File(fc.getSpeechFiles()[2].getFileDir());
            File temp = new File(fc.getSpeechFiles()[2].getFileDir() + ".temp");
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(
                    src
            ));
            BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(
                    temp
            ));
            // calculate new offsets and sizes
            fc.getSpeechFiles()[2].setFileSize(
                    fc.getSpeechFiles()[2].getFileSize() + sizeDif
            );
            fc.getSpeechFiles()[2].getFiles()[fileIndex].setFileSize(
                    fc.getSpeechFiles()[2].getFiles()[fileIndex].getFileSize() + sizeDif
            );
            for(int i = fileIndex + 1; i < fc.getSpeechFiles()[2].getFiles().length; i ++){
                fc.getSpeechFiles()[2].getFiles()[i].setFileOffset(
                        fc.getSpeechFiles()[2].getFiles()[i].getFileOffset() + sizeDif
                );
            }
            buf = new byte[4];
            is.read(buf);
            os.write(buf);
            {
                ByteBuffer byteBuf = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
                byteBuf.order(ByteOrder.LITTLE_ENDIAN);
                byteBuf.putInt(
                        fc.getSpeechFiles()[2].getFileSize()
                );
                os.write(byteBuf.array());
            }
            skip(4, is);
            buf = new byte[8];
            is.read(buf);
            os.write(buf);
            // recreate table
            for(int i = 0; i < fc.getSpeechFiles()[2].getFileCount(); i ++){
                String dir = fc.getSpeechFiles()[2].getFiles()[i].getFileDir();
                ByteBuffer byteBuf = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
                byteBuf.order(ByteOrder.BIG_ENDIAN);
                byteBuf.putInt(
                        fc.getSpeechFiles()[2].getFiles()[i].getFileOffset()
                );
                os.write(byteBuf.array());
                byteBuf = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
                byteBuf.order(ByteOrder.BIG_ENDIAN);
                byteBuf.putInt(
                        fc.getSpeechFiles()[2].getFiles()[i].getFileSize()
                );
                os.write(byteBuf.array());
                os.write(
                        dir.getBytes(StandardCharsets.UTF_8)
                );
            }
            os.flush();
            int totalSkip = (int) temp.length();
            skip(totalSkip - 16, is);
            int skipSize = dat.getFileOffset() + entry.getOffset() - totalSkip;
            skipWrite(skipSize, is, os);
            totalSkip += skipSize;
            BufferedInputStream isSns = new BufferedInputStream(
                    new FileInputStream(sns)
            );
            skipWrite((int) sns.length(), isSns, os);
            isSns.close();
            skip(oldSize, is);
            totalSkip += oldSize + sizeDif;
            skipWrite(fc.getSpeechFiles()[2].getFileSize() - totalSkip, is, os);
            is.close();
            os.flush();
            os.close();
            Files.delete(src.toPath());
            Files.move(temp.toPath(), src.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        snr.delete();
        sns.delete();
    }

    public static void replaceSound(FileCombo fc, int fileIndex, File replaceWith) throws IOException, InterruptedException {
        STHSpeechFile sth = (STHSpeechFile) fc.getSpeechFiles()[1].getFiles()[fileIndex];
        DATSpeechFile dat = (DATSpeechFile) fc.getSpeechFiles()[2].getFiles()[fileIndex];
        List<File> files = Arrays.asList(replaceWith.listFiles());
        MassEncoder massEncoder = new MassEncoder(35, files);
        massEncoder.massEncode();
        List<File> snrList = Arrays.asList(Objects.requireNonNull(new File("temp")
                .listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".snr"))));
        snrList.sort(new FileNameComparator());
        List<File> snsList = Arrays.asList(Objects.requireNonNull(new File("temp")
                .listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".sns"))));
        snsList.sort(new FileNameComparator());
        int totalSize = 0;
        File datOut = new File("result.dat");
        File sthOut = new File("result.sth");
        BufferedOutputStream osSth = new BufferedOutputStream(new FileOutputStream(sthOut));
        BufferedOutputStream osDat = new BufferedOutputStream(new FileOutputStream(datOut));
        for(int i = 0; i < snrList.size(); i++){
            BufferedInputStream isSnr = new BufferedInputStream(new FileInputStream(snrList.get(i)));
            BufferedInputStream isSns = new BufferedInputStream(new FileInputStream(snsList.get(i)));
            byte[] buf = new byte[8];
            isSnr.read(buf);
            int skipSize = (int) snsList.get(i).length();
            skipWrite(skipSize, isSns, osDat);
            ByteBuffer byteBuf = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
            byteBuf.order(ByteOrder.LITTLE_ENDIAN);
            byteBuf.putInt(totalSize);
            osSth.write(byteBuf.array());
            osSth.write(buf);
            osSth.flush();
            osDat.flush();
            isSnr.close();
            isSns.close();
            totalSize += skipSize;
        }
        osSth.close();
        osDat.close();
        int oldSize = dat.getFileSize();
        int sizeDif = (int) datOut.length() - oldSize;
        {
            File src = new File(fc.getSpeechFiles()[2].getFileDir());
            File temp = new File(fc.getSpeechFiles()[2].getFileDir() + ".temp");
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(
                    src
            ));
            BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(
                    temp
            ));
            BufferedInputStream isDat = new BufferedInputStream(new FileInputStream(datOut));
            byte[] buf;
            // calculate new offsets and sizes
            fc.getSpeechFiles()[2].setFileSize(
                    fc.getSpeechFiles()[2].getFileSize() + sizeDif
            );
            fc.getSpeechFiles()[2].getFiles()[fileIndex].setFileSize(
                    fc.getSpeechFiles()[2].getFiles()[fileIndex].getFileSize() + sizeDif
            );
            for(int i = fileIndex + 1; i < fc.getSpeechFiles()[2].getFiles().length; i ++){
                fc.getSpeechFiles()[2].getFiles()[i].setFileOffset(
                        fc.getSpeechFiles()[2].getFiles()[i].getFileOffset() + sizeDif
                );
            }
            buf = new byte[4];
            is.read(buf);
            os.write(buf);
            {
                ByteBuffer byteBuf = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
                byteBuf.order(ByteOrder.LITTLE_ENDIAN);
                byteBuf.putInt(
                        fc.getSpeechFiles()[2].getFileSize()
                );
                os.write(byteBuf.array());
            }
            skip(4, is);
            buf = new byte[8];
            is.read(buf);
            os.write(buf);
            for(int i = 0; i < fc.getSpeechFiles()[2].getFileCount(); i ++){
                String dir = fc.getSpeechFiles()[2].getFiles()[i].getFileDir();
                ByteBuffer byteBuf = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
                byteBuf.order(ByteOrder.BIG_ENDIAN);
                byteBuf.putInt(
                        fc.getSpeechFiles()[2].getFiles()[i].getFileOffset()
                );
                os.write(byteBuf.array());
                byteBuf = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
                byteBuf.order(ByteOrder.BIG_ENDIAN);
                byteBuf.putInt(
                        fc.getSpeechFiles()[2].getFiles()[i].getFileSize()
                );
                os.write(byteBuf.array());
                os.write(
                        dir.getBytes(StandardCharsets.UTF_8)
                );
            }
            os.flush();
            int totalSkip = (int) temp.length();
            skip(totalSkip - 16, is);
            int skipSize = dat.getFileOffset() - totalSkip;
            skipWrite(skipSize, is, os);
            os.flush();
            totalSkip += skipSize;
            skipWrite((int) datOut.length(), isDat, os);
            isDat.close();
            skip(oldSize, is);
            totalSkip += oldSize + sizeDif;
            skipWrite(fc.getSpeechFiles()[2].getFileSize() - totalSkip, is, os);
            os.flush();
            os.close();
            is.close();
            Files.delete(src.toPath());
            Files.move(temp.toPath(), src.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        {
            File src = new File(fc.getSpeechFiles()[1].getFileDir());
            File temp = new File(fc.getSpeechFiles()[1].getFileDir() + ".temp");
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(
                    src
            ));
            BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(
                    temp
            ));
            BufferedInputStream isSth = new BufferedInputStream(new FileInputStream(sthOut));
            int skipSize = sth.getFileOffset();
            skipWrite(skipSize, is, os);
            skipSize = sth.getFileSize();
            skipWrite(skipSize, isSth, os);
            isSth.close();
            skip(skipSize, is);
            skipSize = fc.getSpeechFiles()[1].getFileSize() - sth.getFileSize() - sth.getFileOffset();
            skipWrite(skipSize, is, os);
            isSth = new BufferedInputStream(new FileInputStream(sthOut));
            byte[] buf = new byte[sth.getFileSize()];
            isSth.read(buf);
            sth.fromBuf(buf);
            isSth.close();
            os.flush();
            os.close();
            is.close();
            isSth.close();
            FileUtils.cleanDirectory(new File("temp"));
//            Files.delete(src.toPath());
            Files.move(temp.toPath(), src.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        datOut.delete();
        sthOut.delete();
    }

    public static void skipWrite(int skipSize, BufferedInputStream is, BufferedOutputStream os) throws IOException {
        byte[] buf;
        int tempJumpSize;
        while (skipSize > 0) {
            tempJumpSize = (skipSize % jumpSize == 0) ? jumpSize : skipSize % jumpSize;
            buf = new byte[tempJumpSize];
            is.read(buf);
            os.write(buf);
            skipSize -= tempJumpSize;
        }
    }

    public static void skip(int skipSize, BufferedInputStream is) throws IOException {
        byte [] buf;
        int tempJumpSize;
        while (skipSize > 0) {
            tempJumpSize = (skipSize % jumpSize == 0) ? jumpSize : skipSize % jumpSize;
            buf = new byte[tempJumpSize];
            is.read(buf);
            skipSize -= tempJumpSize;
        }
    }

}
