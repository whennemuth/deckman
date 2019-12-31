package deckman.tests;
import java.io.*;
import java.util.regex.*;

public class myIOTest {

    public myIOTest() {
    }
    
    public static void main(String[] args){
        File dir = new File("C:\\Documents and Settings\\Warren\\Desktop\\deckman images\\thumbnails");
        File[] files = dir.listFiles();
        Pattern pat = Pattern.compile("\\x20*_\\x20*jpg\\x20*\\.\\x20*jpg\\x20*$", Pattern.CASE_INSENSITIVE);
        for(int i=0; i<files.length; i++){
            File f1 = files[i];
            String sPath = f1.getPath();
            f1.renameTo(new File(pat.matcher(sPath).replaceAll(".jpg")));
        }
    }
}
