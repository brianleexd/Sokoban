package sokoban.ui;

import java.io.IOException;
import java.util.ArrayList;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import application.Main.SokobanPropertyType;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import properties_manager.PropertiesManager;
import xml_utilities.InvalidXMLFileFormatException;
import sokoban.file.SokobanFileLoader;
import sokoban.game.SokobanGameStateManager;
import java.io.File;

public class SokobanEventHandler {

    private SokobanUI ui;

    /**
     * Constructor that simply saves the ui for later.
     *
     * @param initUI
     */
    public SokobanEventHandler(SokobanUI initUI) {
        ui = initUI;
    }

    /**
     * This method responds to when the user wishes to switch between the Game,
     * Stats, and Help screens.
     *
     * @param uiState The ui state, or screen, that the user wishes to switch
     * to.
     */
    public void respondToSwitchScreenRequest(SokobanUI.SokobanUIState uiState) {

        ui.changeWorkspace(uiState);
    }

    /**
     * This method responds to when the user presses the new game method.
     */
    public void respondToNewGameRequest() {
        SokobanGameStateManager gsm = ui.getGSM();
        gsm.startNewGame();
    }
    
     /**
     * This method responds to when the user presses the new game method.
     */
    public void respondToSelectLevelRequest(String level) {
        SokobanGameStateManager gsm = ui.getGSM();
        PropertiesManager props = PropertiesManager.getPropertiesManager();

        // GET THE SELECTED LANGUAGE & IT'S XML FILE
        ArrayList<String> SokobanLevels = props.getPropertyOptionsList(SokobanPropertyType.LEVEL_OPTIONS);
        ArrayList<String> SokobanData = props.getPropertyOptionsList(SokobanPropertyType.LEVEL_FILES);
        int SokobanIndex = SokobanLevels.indexOf(level);
        String SokobanDataFile = SokobanData.get(SokobanIndex);
        String SokobanFileLocation = "./data/" + SokobanDataFile;  
        File fileToOpen = new File(SokobanFileLocation);
        //String SokobanSchema = props.getProperty(SokobanPropertyType.PROPERTIES_SCHEMA_FILE_NAME);
        try {
                if (fileToOpen != null) {
                    // LET'S USE A FAST LOADING TECHNIQUE. WE'LL LOAD ALL OF THE
                    // BYTES AT ONCE INTO A BYTE ARRAY, AND THEN PICK THAT APART.
                    // THIS IS FAST BECAUSE IT ONLY HAS TO DO FILE READING ONCE
                    byte[] bytes = new byte[Long.valueOf(fileToOpen.length()).intValue()];
                    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                    FileInputStream fis = new FileInputStream(fileToOpen);
                    BufferedInputStream bis = new BufferedInputStream(fis);

                    // HERE IT IS, THE ONLY READY REQUEST WE NEED
                    bis.read(bytes);
                    bis.close();

                    // NOW WE NEED TO LOAD THE DATA FROM THE BYTE ARRAY
                    DataInputStream dis = new DataInputStream(bais);

                    // NOTE THAT WE NEED TO LOAD THE DATA IN THE SAME
                    // ORDER AND FORMAT AS WE SAVED IT
                    // FIRST READ THE GRID DIMENSIONS
                    int initGridColumns = dis.readInt();
                    int initGridRows = dis.readInt();
                    int[][] newGrid = new int[initGridColumns][initGridRows];

                    // AND NOW ALL THE CELL VALUES
                    for (int i = 0; i < initGridColumns; i++) {
                        for (int j = 0; j < initGridRows; j++) {
                            newGrid[i][j] = dis.readInt();
                        }
                    }
                    ui.grid = newGrid;
                    ui.gridColumns = initGridColumns;
                    ui.gridRows = initGridRows;
                    ui.gridCopy= new int[ui.gridColumns][ui.gridRows];
                    ui.initSokobanUI();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    
    


    /**
     * This method responds to when the user requests to exit the application.
     *
     * @param window The window that the user has requested to close.
     */
    public void respondToExitRequest(Stage primaryStage) {
        // ENGLIS IS THE DEFAULT
        String options[] = new String[]{"Yes", "No"};
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        options[0] = props.getProperty(SokobanPropertyType.DEFAULT_YES_TEXT);
        options[1] = props.getProperty(SokobanPropertyType.DEFAULT_NO_TEXT);
        String verifyExit = props.getProperty(SokobanPropertyType.DEFAULT_EXIT_TEXT);

        // NOW WE'LL CHECK TO SEE IF LANGUAGE SPECIFIC VALUES HAVE BEEN SET
        if (props.getProperty(SokobanPropertyType.YES_TEXT) != null) {
            options[0] = props.getProperty(SokobanPropertyType.YES_TEXT);
            options[1] = props.getProperty(SokobanPropertyType.NO_TEXT);
            verifyExit = props.getProperty(SokobanPropertyType.EXIT_REQUEST_TEXT);
        }

        // FIRST MAKE SURE THE USER REALLY WANTS TO EXIT
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);
        BorderPane exitPane = new BorderPane();
        HBox optionPane = new HBox();
        Button yesButton = new Button(options[0]);
        Button noButton = new Button(options[1]);
        optionPane.setSpacing(10.0);
        optionPane.getChildren().addAll(yesButton, noButton);
        Label exitLabel = new Label(verifyExit);
        exitPane.setCenter(exitLabel);
        exitPane.setBottom(optionPane);
        Scene scene = new Scene(exitPane, 50, 100);
        dialogStage.setScene(scene);
        dialogStage.show();
        // WHAT'S THE USER'S DECISION?
        yesButton.setOnAction(e -> {
            // YES, LET'S EXIT
            System.exit(0);
        });
        noButton.setOnAction(e -> {
            dialogStage.close();
        });

    }

}
