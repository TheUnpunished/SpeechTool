package xyz.unpunished.speechtool.model.ui;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.codec.binary.Hex;
import xyz.unpunished.speechtool.model.STHEntry;
import xyz.unpunished.speechtool.model.HDREntry;
import xyz.unpunished.speechtool.util.ValueConverter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileEntryUI {
    private short index;
    private byte[] userData;
    private byte version;
    private byte codec;
    private byte channelConfig;
    private int sampleRate;
    private byte type;
    private boolean loopFlag;
    private int sampleCount;

    @Override
    public String toString() {
        int s = (sampleCount / sampleRate / (channelConfig + 1));
        return index + ": (" + (s / 60) + ":" + (s % 60) + ") "
                + ValueConverter.codecFullName(codec) + " "
                + sampleRate + "Hz "
                + ValueConverter.channels(channelConfig) + " "
                + "[" + Hex.encodeHexString(userData) + "]";
    }

    public FileEntryUI(HDREntry hdrEntry, STHEntry sthEntry,
                       short index) {
        this.index = index;
        userData = new byte[hdrEntry.getUserData().length];
        for (int i = 0; i < userData.length; i++) {
            userData[i] = hdrEntry.getUserData()[i];
        }
        version = sthEntry.getVersion();
        codec = sthEntry.getCodec();
        channelConfig = sthEntry.getChannelConfig();
        sampleRate = sthEntry.getSampleRate();
        type = sthEntry.getType();
        loopFlag = sthEntry.isLoopFlag();
        sampleCount = sthEntry.getSampleCount();
    }
}
