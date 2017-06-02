package sample;

import java.io.File;
import java.util.HashMap;

/**
 * Author: Evan Putnam
 * Language: Java 8
 * Description: A rescursive file search to obtain all mp3's in a given file.
 */
public class FileSearch {


    //Name string maps to absolute path.
    private HashMap<String, String> songs;
    private String startingDirectory;


    /**
     * Object that is instantiated by asking for starting directory path.
     * @param startingDirectory
     */
    public FileSearch(String startingDirectory){
        this.startingDirectory = startingDirectory;
        this.songs = new HashMap<>();
        addSongs(startingDirectory);
    }


    /**
     * Prints the song names in a given directory.
     * @param directory
     * @param offset
     */
    public void printDirectory(String directory,String offset){
        File file = new File(directory);
        if(file.isDirectory() == true){
            System.out.println(offset+file.getName());
            for (File f: file.listFiles()) {
                printDirectory(f.getAbsolutePath(), offset+"      ");
            }
        }else{
            System.out.println(offset+file.getName());
        }
    }


    /**
     * Adds the songs to the hash map object recursively
     * @param directory
     */
    private void addSongs(String directory){
        File file = new File(directory);
        if(file.isDirectory() == true){
            for (File f: file.listFiles()) {
                addSongs(f.getAbsolutePath());
            }
        }else if (file.getAbsolutePath().endsWith(".mp3")){
            System.out.println(file.getAbsolutePath());
            //TODO add files...
            songs.put(file.getName(), file.getAbsolutePath());
        }
    }

    /**
     * Returns songs.
     * @return
     */
    public HashMap<String, String> getSongs() {
        return songs;
    }

    public String getStartingDirectory() {
        return startingDirectory;
    }







    public static void main(String[] args){
        System.out.println(System.getProperty("user.dir"));
        String s = "/Users/evanputnam/Desktop/MusicTest";

        FileSearch src = new FileSearch(s);
        src.printDirectory(s,"");


    }

}
