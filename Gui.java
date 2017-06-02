package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.Comparator;



/**
 * Author: Evan Putnam
 * Language: Java 8
 * Description: A simple media player using JavaFX
 */
public class Gui extends Application {

    //Recursive file search for object
    private FileSearch fileSearch;

    //List view
    private ListView<String> songLst = new ListView<>();

    //Label indicating current song selected
    private Label l1 = new Label("Current Song: ");


    //Label indicating playing
    private Label playLab = new Label("Now Playing: ");

    //Media Player
    private MediaPlayer mediaPlayer;


    //Data for scrolling
    Label currTimeL = new Label("0.0");
    Label endTimeL = new Label("0.0");
    double currTime = 0.0;
    double endTime = 0.0;


    //Slider functionality
    Slider slider;

    //Thread handling for the scroll bar...
    Thread sliderThread;
    boolean movingSlider = false;





    public void start(Stage primaryStage){
        //Setup screen size and title
        primaryStage.setMinHeight(400);
        primaryStage.setMinWidth(600);
        primaryStage.setTitle("MusicPlayer");


        //Main border panel
        BorderPane bPane = new BorderPane();


        //Slider
        slider = new Slider();
        slider.setMin(0.0);
        slider.setValue(currTime);
        slider.setMax(endTime);


        //Move slider if it is not auto moving.
        slider.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> obs, Boolean wasChanging, Boolean isNowChanging) {
                movingSlider = true;
                if (! isNowChanging) {
                    currTime = slider.getValue();
                    currTimeL.setText(String.valueOf(slider.getValue()));
                    mediaPlayer.seek(Duration.seconds(slider.getValue()));
                    movingSlider = false;
                }
            }
        });



        //Select directory and perform recursive file search
        Button b1 = new Button("Select Directory");
        b1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                File dir = directoryChooser.showDialog(primaryStage);
                if(dir != null){
                    try{
                        fileSearch = null;
                        fileSearch = new FileSearch(dir.getAbsolutePath());
                        populateLst();
                    }catch (Exception e){

                    }
                }
            }
        });


        //Functionality for play button
        Button play = new Button("Play");
        play.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //If no song selected from list
                if(l1.getText().split(": ").length == 1){
                    System.out.println("Select song");
                }
                //Checks to make sure song picked from list
                else if(fileSearch != null && fileSearch.getSongs().get(
                        l1.getText().split(": ")[1]) != null){

                    //Gets rid of old media player object if playing
                    if(mediaPlayer != null){
                        mediaPlayer.stop();
                        mediaPlayer = null;
                    }

                    //Instantiates new media player object to start song playback
                    mediaPlayer = new MediaPlayer(new Media(new File(fileSearch.getSongs().get(
                            l1.getText().split(": ")[1]).trim()).toURI().toString()));

                    //Repeats if runs over...
                    mediaPlayer.setOnEndOfMedia(new Runnable() {
                        public void run() {
                            mediaPlayer.seek(Duration.ZERO);
                        }
                    });

                    //Updates the length value of the song
                    mediaPlayer.setOnReady(new Runnable() {
                        @Override
                        public void run() {
                            endTime = mediaPlayer.getTotalDuration().toSeconds();
                            endTimeL.setText(String.valueOf(endTime));
                            slider.setMax(endTime);
                        }
                    });

                    //Now playing text update.
                    playLab.setText("Now Playing: "+l1.getText().split(": ")[1].trim());

                    //Play
                    mediaPlayer.play();

                    //Start thread stuff
                    handleThread();



                }
            }
        });


        //Button for resuming the player.
        Button resume = new Button("Resume");
        resume.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(mediaPlayer != null){
                    mediaPlayer.play();
                }
            }
        });


        //Button for pausing the player
        Button pause = new Button("Pause");
        pause.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(mediaPlayer != null){
                    mediaPlayer.pause();
                    System.out.println(mediaPlayer.getCurrentTime().toSeconds());
                }

            }
        });




        HBox bottom = new HBox();
        bottom.getChildren().addAll(play,currTimeL,slider,endTimeL, resume, pause);
        bottom.setAlignment(Pos.CENTER);




        //H-Box which holds buttons
        HBox hBox = new HBox();
        hBox.getChildren().addAll(b1, l1);

        //Border pane set objects
        bPane.setTop(hBox);
        bPane.setLeft(songLst);
        bPane.setBottom(bottom);
        bPane.setCenter(playLab);




        //New scene and set border pane...
        Scene scene = new Scene(bPane);

        //Primary Stage stuff
        primaryStage.setScene(scene);
        primaryStage.show();


    }


    /**
     * Function to populate the song list
     */
    private void populateLst(){
        //Get a javafx array list to put names into.
        ObservableList names = FXCollections.observableArrayList();
        for (String key: fileSearch.getSongs().keySet()) {
            names.add(key);
        }

        //Sort based on name
        names.sort(new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });

        //Set the song list to the array list
        songLst.setItems(names);
        //Handle on select
        songLst.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        System.out.println(newValue);
                        System.out.println(fileSearch.getSongs().get(newValue));
                        l1.setText("Current Song: "+newValue);
                    }
                }
        );
    }


    /**
     * Function to handle the progress bar thread as progresses over time.
     */
    private void handleThread(){

        //If slider thread does not equal null then interrupt the thread
        if(sliderThread != null){
            sliderThread.interrupt();
            sliderThread = null;
        }

        //Create new thread with specific functionality
        sliderThread = new Thread(){
            public void run(){
                System.out.println("Thread running");
                //Run until break
                while(true){
                    //If interrupted outside
                    if(this.isInterrupted()){
                        System.out.println("Interupt thread");
                        break;
                        //Else
                    }else{
                        //If media player exists then update slider
                        if(mediaPlayer != null){

                            try {
                                //Runnable to run slider
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        currTime = mediaPlayer.getCurrentTime().toSeconds();
                                        if(movingSlider == false){
                                            slider.setValue(currTime);
                                            currTimeL.setText(String.valueOf(currTime));
                                        }
                                    }
                                });
                                sleep(1000);
                                //Catch interrupted exception and break out of loop essentially ending thread.
                            } catch (InterruptedException ex) {
                                System.out.println("Interrupt Thread");
                                break;
                            }

                        }
                    }
                }
            }
        };
        sliderThread.setDaemon(true);

        //Start thread
        sliderThread.start();
    }

}
