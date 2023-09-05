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
public class STHSpeechFile extends SpeechFile {

    private STHEntry[] STHEntries;

    public void fromBuf(byte[] buf){
        int fileCount = buf.length / 12;
        STHEntries = new STHEntry[fileCount];
        for(int i = 0; i < fileCount; i ++){
            int offset = 12 * i;
            byte[] tempBuf = Arrays.copyOfRange(buf, offset, offset + 12);
            STHEntries[i] = STHEntry.fromBuf(tempBuf);
            STHEntries[i].setOffset(HexReader.read32(Arrays.copyOfRange(buf, offset, offset + 4),
                    Endianness.LITTLE));
            String temp = HexReader.binaryStringFromByte(buf[offset + 4]);
            temp = temp.substring(0, 4);
            STHEntries[i].setVersion(HexReader.parseBinaryByteString(temp));
            temp = HexReader.binaryStringFromByte(buf[offset + 4]);
            temp = temp.substring(4, 8);
            STHEntries[i].setCodec(HexReader.parseBinaryByteString(temp));
            temp = HexReader.binaryStringFromByte(buf[offset + 5]);
            temp = temp.substring(0, 6);
            STHEntries[i].setChannelConfig(HexReader.parseBinaryByteString(temp));
            temp = HexReader.binaryStringFromInt(Arrays.copyOfRange(buf, offset + 5, offset + 8));
            temp = temp.substring(14); // 8 + 6
            STHEntries[i].setSampleRate(HexReader.parseBinaryIntegerString(temp));
            temp = HexReader.binaryStringFromByte(buf[offset + 8]);
            temp = temp.substring(2);
            STHEntries[i].setType(HexReader.parseBinaryByteString(temp));
            temp = HexReader.binaryStringFromByte(buf[offset + 8]);
            temp = temp.substring(2, 3);
            STHEntries[i].setLoopFlag(temp.equals("1"));
            temp = HexReader.binaryStringFromInt(Arrays.copyOfRange(buf, offset + 8, offset + 12));
            temp = temp.substring(3);
            STHEntries[i].setSampleCount(HexReader.parseBinaryIntegerString(temp));
        }
    }

    public STHSpeechFile(SpeechFile entry){
        setFileDir(entry.getFileDir());
        setFileType(entry.getFileType());
        setFileOffset(entry.getFileOffset());
        setFileSize(entry.getFileSize());
    }
}
