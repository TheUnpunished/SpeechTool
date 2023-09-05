package xyz.unpunished.speechtool.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import xyz.unpunished.speechtool.model.util.Endianness;
import xyz.unpunished.speechtool.util.HexReader;

import java.util.Arrays;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class STHEntry {

    private int offset;
    private byte version;
    private byte codec;
    private byte channelConfig;
    private int sampleRate;
    private byte type;
    private boolean loopFlag;
    private int sampleCount;
    
    public static STHEntry fromBuf(byte[] buf){
        STHEntry entry = new STHEntry();
        entry.setOffset(HexReader.read32(Arrays.copyOfRange(buf, 0, 4),
                Endianness.LITTLE));
        String temp = HexReader.binaryStringFromByte(buf[4]);
        temp = temp.substring(0, 4);
        entry.setVersion(HexReader.parseBinaryByteString(temp));
        temp = HexReader.binaryStringFromByte(buf[4]);
        temp = temp.substring(4, 8);
        entry.setCodec(HexReader.parseBinaryByteString(temp));
        temp = HexReader.binaryStringFromByte(buf[5]);
        temp = temp.substring(0, 6);
        entry.setChannelConfig(HexReader.parseBinaryByteString(temp));
        temp = HexReader.binaryStringFromInt(Arrays.copyOfRange(buf, 5, 8));
        temp = temp.substring(14); // 8 + 6
        entry.setSampleRate(HexReader.parseBinaryIntegerString(temp));
        temp = HexReader.binaryStringFromByte(buf[8]);
        temp = temp.substring(2);
        entry.setType(HexReader.parseBinaryByteString(temp));
        temp = HexReader.binaryStringFromByte(buf[8]);
        temp = temp.substring(2, 3);
        entry.setLoopFlag(temp.equals("1"));
        temp = HexReader.binaryStringFromInt(Arrays.copyOfRange(buf, 8, 12));
        temp = temp.substring(3);
        entry.setSampleCount(HexReader.parseBinaryIntegerString(temp));
        return entry;
    }

}
