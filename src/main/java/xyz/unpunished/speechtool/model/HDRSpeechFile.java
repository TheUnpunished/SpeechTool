package xyz.unpunished.speechtool.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.unpunished.speechtool.model.util.Endianness;
import xyz.unpunished.speechtool.util.HexReader;

import java.util.Arrays;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class HDRSpeechFile extends SpeechFile {

    private byte id;
    private byte userDataSize;
    private int numberOfFiles;
    private int subId;
    private byte blockSize;
    private int numberOfBlocks;
    private int numberOfSubBanks;
    private HDREntry[] HDREntries;

    public void fromBuf(byte[] buf){
        id = buf[0];
        userDataSize = buf[2];
        numberOfFiles = buf[3] & 0xff;
        subId = buf[4];
        blockSize = buf[9];
        numberOfBlocks = HexReader.read16(Arrays.copyOfRange(buf, 10, 12), Endianness.LITTLE);
        numberOfSubBanks = HexReader.read16(Arrays.copyOfRange(buf, 12, 14), Endianness.BIG);
        HDREntries = new HDREntry[numberOfFiles];
        for(int i = 0; i < numberOfFiles; i++){
            int offset = 16 + i * (2 + userDataSize);
            HDREntries[i] = new HDREntry();
            HDREntries[i].setOffset(HexReader.read16(Arrays.copyOfRange(buf,
                    offset, offset + 2), Endianness.BIG));
            HDREntries[i].setUserData(Arrays.copyOfRange(buf, offset + 2, offset + 2 + userDataSize));
        }
    }

    public HDRSpeechFile(SpeechFile entry){
        setFileDir(entry.getFileDir());
        setFileType(entry.getFileType());
        setFileOffset(entry.getFileOffset());
        setFileSize(entry.getFileSize());
    }
}
