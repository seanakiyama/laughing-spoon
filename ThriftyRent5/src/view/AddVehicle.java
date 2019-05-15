/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import controller.DateTime;
import controller.ExceptionHandler;
import controller.FileHandling;
import controller.InvalidIdException;
import controller.VehicleStatus;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class AddVehicle
        extends Application {

    private String vehicleImage;
    private FileInputStream input;
    private Image image;
    private ImageView imageView;
//Create the year input listener to only accept numeric characters
    private void createYearListerner(TextField yearTxt) {
        //add an event to the year input to only allow numbers to by type in
        yearTxt.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    yearTxt.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
    }
//Create type listener to set if the maintenance date will be needed
    private void createTypeListener(ComboBox<String> typeComboBox, DatePicker lastmaitenanceTxt) {
        //event to check if the vehicle is a car or a van
        typeComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null){
            if (newValue.equals("Car")) {
                //car disable last maintenance field
                lastmaitenanceTxt.setDisable(true);
            } else {
                //van enable last maintenance field
                lastmaitenanceTxt.setDisable(false);
            }
            }
            
        });
    }
//create the menu bar
    private MenuBar createMenu(Stage primaryStage) {
        MenuBar menuBar = new MenuBar();
        Menu actionsMenu = new Menu("Actions");
        MenuItem addMenuItem = new MenuItem("Add vehicle");
        actionsMenu.getItems().add(addMenuItem);
        menuBar.getMenus().add(actionsMenu);
        Menu dataMenu = new Menu("Data");
        MenuItem importMenuItem = new MenuItem("Import");
        MenuItem exportMenuItem = new MenuItem("Export");
        //create the showing objects
        dataMenu.getItems().add(importMenuItem);
        dataMenu.getItems().add(exportMenuItem);
        menuBar.getMenus().add(dataMenu);
        //import and export click events
        importMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                FileHandling._import(primaryStage);
            }
        });

        exportMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                FileHandling.export(primaryStage);
            }
        });
        addMenuItem.setOnAction(e -> {
            AddVehicle addVehicle = new AddVehicle();
            VBox root2 = new VBox();
            Stage stage = new Stage();
            stage.setTitle("Add Vehicle");
            stage.setScene(new Scene((Parent) root2, 1000.0, 800.0));
            stage.show();
            try {
                addVehicle.start(stage);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        return menuBar;
    }
//create the button save action events
    private void addSaveButtonAction(Button saveButton, ComboBox<String> typeComboBox, TextField idTxt, TextField yearTxt, ComboBox<String> makeTxt,
            ComboBox<String> passengerTxt, TextField modelTxt, DatePicker lastmaitenanceTxt) {
        saveButton.setOnAction((ActionEvent event) -> {
            if ((typeComboBox.getValue() == null)
                    || (idTxt.getText() == null)
                    || (yearTxt.getText() == null)
                    || (makeTxt.getValue() == null)
                    || (modelTxt.getText() == null)
                    || (passengerTxt.getValue() == null)) {
                ExceptionHandler.ShowExceptionAlert("All fields must be filled");
                return;
            }
            if ((idTxt.getText().trim().equals(""))
                    || (makeTxt.getValue().trim().equals(""))
                    || (modelTxt.getText().trim().equals(""))) {
                ExceptionHandler.ShowExceptionAlert("All fields must be filled");
                return;
            }
            String _vehicleID = idTxt.getText();
            int _year = Integer.parseInt(yearTxt.getText());
            String _make = makeTxt.getValue();
            String _model = modelTxt.getText();
            int _numPassengerSeats = Integer.parseInt(passengerTxt.getValue());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            DateTime _lastMaitenance = DateTime.formatDate(formatter.format(lastmaitenanceTxt.getValue()));
            String _type = typeComboBox.getValue();
            String _status = VehicleStatus.AVALIABLE.toString();
            String _Image = vehicleImage;
            if (typeComboBox.getValue().equals("Car")) {
                controller.Car car;
                try {
                    car = new controller.Car(_vehicleID, _year, _make, _model, _numPassengerSeats, _Image);
                    car.saveVehicle();
                } catch (InvalidIdException ex) {
                    Logger.getLogger(AddVehicle.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                controller.Van van;
                try {
                    van = new controller.Van(_vehicleID, _year, _make, _model, _lastMaitenance, _Image);
                    van.saveVehicle();
                } catch (InvalidIdException ex) {
                    Logger.getLogger(AddVehicle.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    //add exit button event
    private void addExitButtonEvent(){
        exitButton.setOnAction((ActionEvent event) -> {
              Stage stage = (Stage) exitButton.getScene().getWindow();
            stage.close();
            
        });
    }
//create the image action events
    private void addImageEvent(Stage primaryStage) {
        imageView.setOnMouseClicked((MouseEvent event) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            fileChooser.getExtensionFilters().addAll(
                    new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
                    new ExtensionFilter("All Files", "*.*"));
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                vehicleImage = "images/" + selectedFile.getName();
                imageView.setImage(new Image(selectedFile.toURI().toString()));
            }
        });
    }
//create the new button action events
    private void addNewButtonEvent(Button newButton, ComboBox<String> typeComboBox, TextField idTxt, TextField yearTxt, ComboBox<String> makeTxt,
            ComboBox<String> passengerTxt, TextField modelTxt, DatePicker lastmaitenanceTxt) {
        newButton.setOnAction((ActionEvent event) -> {
            idTxt.clear();
            yearTxt.clear();
            typeComboBox.valueProperty().set(null);
            passengerTxt.valueProperty().set(null);
            makeTxt.valueProperty().set(null);
            modelTxt.clear();
            vehicleImage = "images/no_car.jpg";
            try {
                input = new FileInputStream(vehicleImage);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(AddVehicle.class.getName()).log(Level.SEVERE, null, ex);
            }
            image = new Image((InputStream) input);
            imageView.setImage(image);
            imageView.setFitHeight(300.0);
            imageView.setFitWidth(300.0);
            imageView.setPreserveRatio(true);
        });
    }
//Declaration of the objects that will be on the screen
    private Button exitButton = new Button("Exit");
    private Button newButton = new Button("New");
    private Button saveButton = new Button("Save");
    private Label typeLabel = new Label("Type");
    private Label idLabel = new Label("ID");
    private Label lmaintenanceLabel = new Label("Last maintenance");
    private Label yearLabel = new Label("Year");
    private Label makeLabel = new Label("Make");
    private Label modelLabel = new Label("Model");
    private Label passengerLabel = new Label("Nº passengers");
    private VBox vBox = new VBox();
    private VBox vBox2 = new VBox();
    private HBox buttonsHBox = new HBox();
    private ComboBox<String> typeComboBox = new ComboBox<String>();
    private TextField idTxt = new TextField();
    private Date dt = new Date();
    private DatePicker lastmaitenanceTxt = new DatePicker(dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    private TextField yearTxt = new TextField();
    private ComboBox<String> passengerTxt = new ComboBox<String>();
    private ComboBox<String> makeTxt = new ComboBox<String>();
    private TextField modelTxt = new TextField();

    //create the main method to execute
    public void start(Stage primaryStage) throws FileNotFoundException {
        //create the menu bar and it´s items
        addExitButtonEvent();
        lastmaitenanceTxt.setDisable(true);
        createYearListerner(yearTxt);
        //create the input objects
        passengerTxt.setPrefSize(100, 20);
        passengerTxt.getItems().add("4");
        passengerTxt.getItems().add("7");
        passengerTxt.getItems().add("15");
        makeTxt.setPrefSize(100, 20);
        makeTxt.getItems().add("Honda");
        makeTxt.getItems().add("Toyota");
        makeTxt.getItems().add("Vokswagen");
        makeTxt.getItems().add("Chevy");
        vehicleImage = "images/no_car.jpg";
        input = new FileInputStream(vehicleImage);
        image = new Image((InputStream) input);
        imageView = new ImageView(image);
        imageView.setFitHeight(300.0);
        imageView.setFitWidth(300.0);
        imageView.setPreserveRatio(true);
        createTypeListener(typeComboBox, lastmaitenanceTxt);
        //create lables to the fields
        typeComboBox.getItems().add("Car");
        typeComboBox.getItems().add("Van");
        //create the containers
        BorderPane border = new BorderPane();
        border.setTop(createMenu(primaryStage));
        //define the style of the objects
        border.setLeft((Node) vBox);
        border.setCenter((Node) vBox2);
        vBox.setSpacing(10.0);
        vBox2.setSpacing(10.0);
        setObjectsDesign();
        ObservableList<Node> list = vBox.getChildren();
        ObservableList<Node> list2 = vBox2.getChildren();
        ObservableList<Node> list3 = buttonsHBox.getChildren();
        //add the objects to a list
        list.addAll(typeLabel, idLabel, lmaintenanceLabel, yearLabel, makeLabel, modelLabel, passengerLabel);
        list2.addAll(typeComboBox, idTxt, lastmaitenanceTxt, yearTxt, makeTxt, modelTxt, passengerTxt, imageView, buttonsHBox);
        list3.addAll(exitButton, newButton, saveButton);
        //event to save the data
        addSaveButtonAction(saveButton, typeComboBox, idTxt, yearTxt, makeTxt, passengerTxt, modelTxt, lastmaitenanceTxt);
        //button to create a new vehicle
        addNewButtonEvent(newButton, typeComboBox, idTxt, yearTxt, makeTxt, passengerTxt, modelTxt, lastmaitenanceTxt);
        //event to add an image
        addImageEvent(primaryStage);
        //create scene and show it
        createScene(primaryStage, border);
    }
//create the scene
     private void createScene(Stage primaryStage, BorderPane border) {
        Scene scene = new Scene((Parent) border, 500.0, 900.0);
        primaryStage.setTitle("ThriftyRent");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
//set the objects design
     private  void setObjectsDesign() {
        VBox.setMargin((Node) typeLabel, (Insets) new Insets(20.0, 20.0, 20.0, 20.0));
        VBox.setMargin((Node) idLabel, (Insets) new Insets(20.0, 20.0, 20.0, 20.0));
        VBox.setMargin((Node) lmaintenanceLabel, (Insets) new Insets(1.0, 20.0, 20.0, 20.0));
        VBox.setMargin((Node) yearLabel, (Insets) new Insets(3.0, 20.0, 20.0, 20.0));
        VBox.setMargin((Node) makeLabel, (Insets) new Insets(1.0, 10.0, 10.0, 20.0));
        VBox.setMargin((Node) modelLabel, (Insets) new Insets(3.0, 10.0, 10.0, 20.0));
        VBox.setMargin((Node) passengerLabel, (Insets) new Insets(10.0, 10.0, 10.0, 20.0));
        VBox.setMargin((Node) typeComboBox, (Insets) new Insets(20.0, 20.0, 20.0, 20.0));
        VBox.setMargin((Node) idTxt, (Insets) new Insets(5.0, 5.0, 5.0, 10.0));
        VBox.setMargin((Node) lastmaitenanceTxt, (Insets) new Insets(5.0, 5.0, 5.0, 10.0));
        VBox.setMargin((Node) yearTxt, (Insets) new Insets(5.0, 5.0, 5.0, 10.0));
        VBox.setMargin((Node) passengerTxt, (Insets) new Insets(5.0, 5.0, 5.0, 10.0));
        VBox.setMargin((Node) makeTxt, (Insets) new Insets(5.0, 5.0, 5.0, 10.0));
        VBox.setMargin((Node) modelTxt, (Insets) new Insets(5.0, 5.0, 5.0, 10.0));
        VBox.setMargin((Node) imageView, (Insets) new Insets(20.0, 20.0, 20.0, 20.0));
        VBox.setMargin((Node) buttonsHBox, (Insets) new Insets(20.0, 20.0, 20.0, 20.0));
        HBox.setMargin((Node) exitButton, (Insets) new Insets(20.0, 20.0, 20.0, 20.0));
        HBox.setMargin((Node) newButton, (Insets) new Insets(20.0, 20.0, 20.0, 20.0));
        HBox.setMargin((Node) saveButton, (Insets) new Insets(20.0, 20.0, 20.0, 20.0));
    }

    public static void main(String[] args) {
        //launch the visual
        AddVehicle.launch((String[]) args);
    }
}
