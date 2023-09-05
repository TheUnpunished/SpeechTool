package xyz.unpunished.speechtool.model.util;

import java.io.File;
import java.text.ParseException;
import java.util.Comparator;

public class FileNameComparator implements Comparator<File> {

    public FileNameComparator(){

    }

    private int extractPart(String s){
        String[] split = s.split("part", 2);
        if(s.split("part").length < 2){
            return -1;
        }
        else{
            return parseNumber(split[1]);
        }
    }

    @Override
    public int compare(File o1, File o2) {
        Integer i1 = extractPart(o1.getName());
        Integer i2 = extractPart(o2.getName());
        if(i1 > 0 && i2 > 0){
            return Integer.compare(i1, i2);
        }
        else {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }

    private int parseNumber(String stringToParse){
        int i = 0;
        boolean isANumber = true;
        int number = 0;
        while (isANumber && i < stringToParse.length()){
            short currentNum = 0;
            try{
                currentNum = Short.parseShort(Character.toString(stringToParse.charAt(i)));
            }
            catch (Exception e){
                isANumber = false;
                continue;
            }
            number = number * 10 + currentNum;
            i ++;
        }
        return number;
    }
}
