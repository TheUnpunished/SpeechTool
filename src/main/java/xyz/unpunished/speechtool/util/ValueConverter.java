package xyz.unpunished.speechtool.util;

public class ValueConverter {

    public static String codecShortName(byte codec){
        switch (codec){
            case 5:
                return "EL31";
            case 6:
                return "L32P";
            case 7:
                return "L32S";
            default:
                return "Unknown codec: " + codec;
        }
    }

    public static String codecLongName(byte codec){
        switch (codec){
            case 5:
                return "EALAYER3 V1";
            case 6:
                return "EALAYER3 V2 PCM";
            case 7:
                return "EALAYER3 V3 SPIKE";
            default:
                return "Unknown codec: " + codec;
        }
    }

    public static String codecFullName(byte codec){
        switch (codec){
            case 5:
            case 6:
            case 7:
                return codecLongName(codec) + " (" + codecShortName(codec) + ")";
            default:
                return "Unknown codec: " + codec;
        }
    }

    public static String type(byte type){
        switch (type){
            case 0:{
                return "RAM";
            }
            case 1:{
                return "STREAM";
            }
            case 2:{
                return "GIGASAMPLE";
            }
            default:{
                return "Unknown type: " + type;
            }
        }
    }

    public static String loop(boolean loop){
        return loop ? "yes" : "no";
    }

    public static String version(byte version){
        return "V" + version;
    }

    public static String channels(byte channels){
        switch (channels + 1){
            case 1:
                return "mono";
            case 2:
                return "stereo";
            case 4:
                return "quad";
            case 6:
                return "5.1";
            case 8:
                return "7.1";
            default:
                return "Unknown channel configuration: " + channels;
        }
    }

}
