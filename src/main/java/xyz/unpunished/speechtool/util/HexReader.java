package xyz.unpunished.speechtool.util;

import xyz.unpunished.speechtool.model.util.Endianness;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HexReader {

    public static Integer read32(byte[] hexVal, Endianness endianness){
        switch (endianness){
            case BIG:{
                return ByteBuffer.wrap(extend((byte) 0, hexVal, endianness, 4)).getInt();
            }
            case LITTLE:{
                return ByteBuffer.wrap(extend((byte) 0, convertLEBE(hexVal), endianness, 4)).getInt();
            }
            default:{
                return 0;
            }
        }
    }

    public static Short read16(byte[] hexVal, Endianness endianness){
        switch (endianness){
            case BIG:{
                return ByteBuffer.wrap(extend((byte) 0, hexVal, endianness, 2)).getShort();
            }
            case LITTLE:{
                return ByteBuffer.wrap(extend((byte) 0, convertLEBE(hexVal), endianness, 2)).getShort();
            }
            default:{
                return 0;
            }
        }
    }

    private static byte[] extend(byte extendBy, byte[] hexVal, Endianness endianness, int length){
        int diff = length - hexVal.length;
        byte[] result = new byte[length];
        switch (endianness){
            case BIG:{
                for(int i = 0; i < diff; i ++)
                    result[i] = extendBy;
                for(int i = 0; i < hexVal.length; i ++)
                    result[diff + i] = hexVal[i];
                break;
            }
            case LITTLE:{
                for(int i = 0; i < hexVal.length; i ++)
                    result[diff + i] = hexVal[i];
                for(int i = 0; i < diff; i ++)
                    result[i] = extendBy;
                break;
            }
        }
        return result;
    }

    public static String readS(InputStream stream) throws IOException {
        byte[] curChar = new byte[1];
        List<Byte> characters = new ArrayList<>();
        do{
            stream.read(curChar);
            characters.add(curChar[0]);
        }
        while (curChar[0] != 0);
        int size = characters.size();
        byte[] primitiveArray = new byte[size];
        for(int i = 0; i < size; i++){
            primitiveArray[i] = characters.get(i);
        }
        return new String(primitiveArray, StandardCharsets.UTF_8);
    }


    public static byte[] convertLEBE(byte[] hexVal){
        int size = hexVal.length;
        byte[] result = new byte[size];
        for(int i = size - 1; i >=0; i--)
            result[size - i - 1] = hexVal[i];
        return result;
    }

    private static String appendBinaryBy(char appendBy, String binaryString, int length){
        for(int i = 0; i < length - binaryString.length(); i ++){
            binaryString = appendBy + binaryString;
        }
        return binaryString;
    }

    public static Byte parseBinaryByteString(String binaryString){
        binaryString = appendBinaryBy('0', binaryString, 8);
        return Byte.parseByte(binaryString ,2);
    }

    private static byte[] appendByteArrayBy(byte appendBy, byte[] original, int length){
        byte[] result = new byte[length];
        int diff = length - original.length;
        for(int i = 0; i < diff; i ++)
            result[i] = appendBy;
        for (int i = 0; i < original.length; i ++){
            result[diff + i] = original[i];
        }
        return result;
    }

    public static int byteArrayToInt(byte[] hexVal)
    {
        int result = 0;
        for (byte b: hexVal) {
            result = (result << 8) + (b & 0xFF);
        }
        return result;
    }

    public static int parseBinaryIntegerString(String binaryString){
        binaryString = appendBinaryBy('0', binaryString, 32);
        return Integer.parseInt(binaryString, 2);
    }

    public static String binaryStringFromByte(byte num){
        return String.format("%8s",
                Integer.toBinaryString(num & 0xFF))
                .replace(' ', '0');
    }

    public static String binaryStringFromInt(byte [] num){
        return String.format("%32s", Integer.toBinaryString(byteArrayToInt(num)))
                .replace(" ", "0");
    }

}
