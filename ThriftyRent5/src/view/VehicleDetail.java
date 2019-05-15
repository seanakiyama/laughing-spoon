/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import controller.Car;
import controller.DateTime;
import controller.FileHandling;
import controller.RentException;
import controller.RentalRecord;
import controller.Van;
import controller.VehicleType;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.Database;

public class VehicleDetail extends Application {
    //create the objects that will hold data about the vehicle 

    private FileInputStream input;
    private Image image;
    private ImageView imageView;
    private controller.DateTime dateVehicleReturned = null;
    private String Rentalsql = "SELECT record_id, record_vehicleid, record_customerid, record_rentDate, "
            + "record_estimatedReturnDate, record_actualReturnDate, record_rentalFee, "
            + "record_lateFee FROM RENTAL_RECORDS WHERE record_vehicleid = ?";

    List<controller.BaseVehicle> list = new ArrayList<>();
    ObservableList<controller.BaseVehicle> observableVehicleList = FXCollections.observableList(list);

    List<controller.BaseVehicle> refreshList = new ArrayList<>();
    ObservableList<controller.BaseVehicle> refreshObservableVehicleList = FXCollections.observableList(refreshList);
//create the menu bar

    private MenuBar createMenu(Stage prStage) {
        MenuBar menuBar = new MenuBar();
        Menu actionsMenu = new Menu("Actions");
        MenuItem addMenuItem = new MenuItem("Add vehicle");
        actionsMenu.getItems().add(addMenuItem);
        menuBar.getMenus().add(actionsMenu);
        Menu dataMenu = new Menu("Data");
        MenuItem importMenuItem = new MenuItem("Import");
        MenuItem exportMenuItem = new MenuItem("Export");
        importMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                FileHandling._import(prStage);
            }
        });
        exportMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                FileHandling.export(prStage);
            }
        });
        dataMenu.getItems().add(importMenuItem);
        dataMenu.getItems().add(exportMenuItem);
        menuBar.getMenus().add(dataMenu);
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
//create the dialog of the result converter

    private void dialogResultConverter(Dialog dialog, GridPane grid) {
        dialog.getDialogPane().setContent(grid);
        ButtonType buttonTypeOk = new ButtonType("Okay", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.setResultConverter(new Callback<ButtonType, String>() {
            @Override
            public String call(ButtonType b) {
                if (b == buttonTypeOk) {
                    controller.DateTime dateTime = new controller.DateTime();
                    DatePicker returnDate = new DatePicker((new Date()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                    LocalDate localDate = returnDate.getValue();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    dateVehicleReturned = dateTime.formatDate(localDate.format(formatter));

                }
                return null;
            }
        });
    }
//add grid objects

    private void addGridObjects(GridPane grid, Label customerLabel, Label customerID, Label rentDateLabel, Label rentedDate, Label returnDateLabel, DatePicker returnDate) {
        grid.add(customerLabel, 1, 1);
        grid.add(customerID, 2, 1);
        grid.add(rentDateLabel, 1, 2);
        grid.add(rentedDate, 2, 2);
        grid.add(returnDateLabel, 1, 3);
        grid.add(returnDate, 2, 3);
        grid.setMargin((Node) customerLabel, (Insets) new Insets(20.0, 20.0, 20.0, 20.0));
        grid.setMargin((Node) rentDateLabel, (Insets) new Insets(20.0, 20.0, 20.0, 20.0));
        grid.setMargin((Node) returnDateLabel, (Insets) new Insets(20.0, 20.0, 20.0, 20.0));
        grid.setMargin((Node) customerID, (Insets) new Insets(20.0, 20.0, 20.0, 20.0));
        grid.setMargin((Node) rentedDate, (Insets) new Insets(20.0, 20.0, 20.0, 20.0));
        grid.setMargin((Node) returnDate, (Insets) new Insets(20.0, 20.0, 20.0, 20.0));

    }
//create return button event

    private void createReturnButtonEvents(Button returnButton) {
        returnButton.setOnAction((ActionEvent event) -> {
            controller.BaseVehicle vehicle = observableVehicleList.get(0);
            String _recordID = null;
            String _vehicleID = null;
            String _customerID = null;
            String _rentDate = null;
            String _estReturnDate = null;
            String _actualReturnDate = null;
            double _rentalFee = 0;
            double _lateFee;
            controller.DateTime rentDate;
            controller.DateTime estReturnDate;
            controller.DateTime actualReturnDate = null;
            try {
                PreparedStatement stmt = Database.initialize("database.db").prepareStatement(Rentalsql);
                stmt.setString(1, vehicle.getVehicleID());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    _recordID = rs.getString("record_id");
                    _vehicleID = rs.getString("record_vehicleid");
                    _customerID = rs.getString("record_customerid");
                    _rentDate = rs.getString("record_rentDate");
                    _estReturnDate = rs.getString("record_estimatedReturnDate");
                    _actualReturnDate = rs.getString("record_actualReturnDate");
                    _rentalFee = rs.getDouble("record_rentalFee");
                    _lateFee = rs.getDouble("record_lateFee");
                }
            } catch (SQLException e) {
            }
            rentDate = controller.DateTime.formatDate(_rentDate);
            estReturnDate = controller.DateTime.formatDate(_estReturnDate);
            Dialog dialog = new Dialog<>();
            dialog.setTitle("Return Vehicle");
            dialog.setHeaderText(("Enter the details below to return vehicle ") + vehicle.getVehicleID());
            dialog.setResizable(true);
            Label customerLabel = new Label("Customer ID: ");
            Label rentDateLabel = new Label("Rent Date: ");
            Label returnDateLabel = new Label("Return Date: ");
            Date input = new Date();
            LocalDate date = input.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            DatePicker returnDate = new DatePicker(date);
            Label customerID = new Label();
            Label rentedDate = new Label();
            GridPane grid = new GridPane();
            customerID.setText(_customerID);
            rentedDate.setText(_rentDate);
            addGridObjects(grid, customerLabel, customerID, rentDateLabel, rentedDate, returnDateLabel, returnDate);
            dialogResultConverter(dialog, grid);
            Optional result = dialog.showAndWait();

            System.out.println(returnDate.getValue().toString());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            DateTime _lastMaitenance = DateTime.formatDate(formatter.format(returnDate.getValue()));

            dateVehicleReturned = _lastMaitenance;
            returnVehicle(vehicle, _recordID, _vehicleID, _customerID, rentDate, estReturnDate, _rentalFee);
        });
    }
//return the vehicle

    private void returnVehicle(controller.BaseVehicle vehicle, String _recordID, String _vehicleID, String _customerID, DateTime rentDate, DateTime estReturnDate, double _rentalFee) {
        RentalRecord rentalRecord = new RentalRecord(_vehicleID, _customerID, rentDate, estReturnDate, _rentalFee);
        if (dateVehicleReturned != null) {
            rentalRecord.setActualReturnDate(dateVehicleReturned);
        }
        rentalRecord.setRecordID(_recordID);
        if (vehicle.getVehicleType() == VehicleType.CAR) {
            controller.Car car = (controller.Car) vehicle;
            car.returnVehicle(dateVehicleReturned, rentalRecord);
        } else {
            controller.Van van = (controller.Van) vehicle;
            van.returnVehicle(dateVehicleReturned, rentalRecord);
        }
        refresh();
    }
//create the rent button events

    private void createRentButtonEvents(Button rentButton) {
        rentButton.setOnAction((ActionEvent event) -> {
            controller.BaseVehicle vehicle = observableVehicleList.get(0);
            Dialog dialog = new Dialog<>();
            dialog.setTitle("Rent Vehicle");
            dialog.setHeaderText(("Enter the details below to rent vehicle ") + vehicle.getVehicleID());
            dialog.setResizable(true);
            Label customerLabel = new Label("Customer ID: ");
            Label dateLabel = new Label("Rent Date: ");
            Label daysLabel = new Label("How many days: ");
            TextField customerID = new TextField();
            DatePicker rentDate = new DatePicker(((new Date()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
            TextField daysToRent = new TextField();
            GridPane grid = new GridPane();
            grid.add(customerLabel, 1, 1);
            grid.add(customerID, 2, 1);
            grid.add(dateLabel, 1, 2);
            grid.add(rentDate, 2, 2);
            grid.add(daysLabel, 1, 3);
            grid.add(daysToRent, 2, 3);
            grid.setMargin((Node) customerLabel, (Insets) new Insets(20.0, 20.0, 20.0, 20.0));
            grid.setMargin((Node) customerID, (Insets) new Insets(20.0, 20.0, 20.0, 20.0));
            grid.setMargin((Node) dateLabel, (Insets) new Insets(20.0, 20.0, 20.0, 20.0));
            grid.setMargin((Node) rentDate, (Insets) new Insets(20.0, 20.0, 20.0, 20.0));
            grid.setMargin((Node) daysLabel, (Insets) new Insets(20.0, 20.0, 20.0, 20.0));
            grid.setMargin((Node) daysToRent, (Insets) new Insets(20.0, 20.0, 20.0, 20.0));
            dialog.getDialogPane().setContent(grid);
            ButtonType buttonTypeOk = new ButtonType("Okay", ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
            setResultConverterEvent(dialog,buttonTypeOk, vehicle, customerID, rentDate, daysToRent);
            Optional result = dialog.showAndWait();
            refresh();
        });
    }
//define the result converter event

    private void setResultConverterEvent(Dialog dialog, ButtonType btype,controller.BaseVehicle vehicle, TextField customerID, DatePicker rentDate, TextField daysToRent) {
        final Button okButton = (Button) dialog.getDialogPane().lookupButton(btype);
        
        okButton.addEventFilter(ActionEvent.ACTION, ae -> {
            if (vehicle.getVehicleType() == VehicleType.CAR) {

                controller.DateTime dateTime = new controller.DateTime();
                LocalDate localDate = rentDate.getValue();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                controller.DateTime date = dateTime.formatDate(localDate.format(formatter));

                if (vehicle.getVehicleType() == VehicleType.CAR) {
                    Car car = (Car) vehicle;
                    try {
                        car.rent(customerID.getText(), date, Integer.valueOf(daysToRent.getText()));
                    } catch (RentException ex) {
                        ae.consume();
                    }
                } else {
                    Van van = (Van) vehicle;
                    try {
                        van.rent(customerID.getText(), date, Integer.valueOf(daysToRent.getText()));
                    } catch (RentException ex) {
                        ae.consume();
                    }
                }

            } else {
                if (vehicle.getVehicleType() == VehicleType.VAN) {
                    double estimatedRentalFees = ((controller.Van) vehicle).calculateEstimatedRentalFee(Integer.valueOf(daysToRent.getText()));
                    controller.DateTime dateTime = new controller.DateTime();
                    LocalDate localDate = rentDate.getValue();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    controller.DateTime date = dateTime.formatDate(localDate.format(formatter));
                    vehicle.addRental(customerID.getText(), date, Integer.valueOf(daysToRent.getText()), estimatedRentalFees);
                }
            }

        });
    }
//create the vehicle status label based on requisites

    private void createVehicleStatus(controller.BaseVehicle vehicle) {
        if (null == vehicle.getVehicleStatus()) {
            status.setText("MAINTENANCE");
            status.setTextFill(Color.web("#8B0000"));
            completeMaintenanceButton.setDisable(false);
            rentButton.setDisable(true);
            returnButton.setDisable(true);
            startMaintenanceButton.setDisable(true);
        } else {
            switch (vehicle.getVehicleStatus()) {
                case AVALIABLE:
                    status.setText("AVALIABLE");
                    status.setTextFill(Color.web("#3CB371"));
                    rentButton.setDisable(false);
                    startMaintenanceButton.setDisable(false);
                    returnButton.setDisable(true);
                    completeMaintenanceButton.setDisable(true);
                    break;
                case RENTED:
                    status.setText("RENTED");
                    status.setTextFill(Color.web("#696969"));
                    returnButton.setDisable(false);
                    rentButton.setDisable(true);
                    startMaintenanceButton.setDisable(true);
                    completeMaintenanceButton.setDisable(true);
                    break;
                default:
                    status.setText("MAINTENANCE");
                    status.setTextFill(Color.web("#8B0000"));
                    completeMaintenanceButton.setDisable(false);
                    rentButton.setDisable(true);
                    returnButton.setDisable(true);
                    startMaintenanceButton.setDisable(true);
                    break;
            }
        }
    }
//create the observable list of objects

    private void createObservableList() {
        observableVehicleList.addListener(new ListChangeListener<controller.BaseVehicle>() {
            public void onChanged(ListChangeListener.Change<? extends controller.BaseVehicle> c) {
                if (!observableVehicleList.isEmpty()) {
                    controller.BaseVehicle vehicle = observableVehicleList.get(0);
                    ObservableList<String> items = FXCollections.observableArrayList(vehicle.getRRecordDetais());
                    recordsArea.setItems(items);
                    type.setText(vehicle.getVehicleType().toString());
                    createVehicleStatus(vehicle);
                    id.setText(vehicle.getVehicleID());
                    year.setText(vehicle.getYear() + "");
                    model.setText(vehicle.getModel());
                    make.setText(vehicle.getMake());
                    if (vehicle.getVehicleType() == VehicleType.VAN) {
                        lastMntc.setText(vehicle.getLastMaintenanceDate().toString());
                    } else {
                        lastMntc.setText("**");
                    }
                    vehicleImage = (vehicle.getImage());
                    try {
                        input = new FileInputStream(vehicleImage);
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(VehicleDetail.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    image = new Image((InputStream) input);
                    imageView.setImage(image);
                    imageView.setFitHeight(300.0);
                    imageView.setFitWidth(300.0);
                    imageView.setPreserveRatio(true);
                }
            }
        });

    }
//add event to the startmaintenance button

    private void addEventStrtMaintenanceButton(Button startMaintenanceButton) {
        startMaintenanceButton.setOnAction((ActionEvent event) -> {
            controller.BaseVehicle vehicle = observableVehicleList.get(0);
            vehicle.performMaintenance();
            refresh();
        });
    }
//add event to the complete maintenance button

    private void addEventCompMaintenanceButton(Button completeMaintenanceButton) {
        completeMaintenanceButton.setOnAction((ActionEvent event) -> {
            controller.BaseVehicle vehicle = observableVehicleList.get(0);
            vehicle.completeMaintenance(dateVehicleReturned);
            refresh();
        });
    }
//declaration of the objects that will be on screen
    private Label type = new Label();
    private Label id = new Label();
    private Label lastMntc = new Label();
    private Label year = new Label();
    private Label make = new Label();
    private Label model = new Label();
    private Label status = new Label();
    private Label typeLabel = new Label("Type");
    private Label idLabel = new Label("ID");
    private Label lastMntcLabel = new Label("Last maintenance");
    private Label yearLabel = new Label("Year");
    private Label makeLabel = new Label("Make");
    private Label modelLabel = new Label("Model");
    private Label statusLabel = new Label("Status");
    private Button rentButton = new Button("Rent");
    private Button returnButton = new Button("Return");
    private Button startMaintenanceButton = new Button("Start Maintenance");
    private Button completeMaintenanceButton = new Button("Complete Maintenance");
    private VBox vBox = new VBox();
    private VBox vBox2 = new VBox();
    private VBox vBox3 = new VBox();
    private HBox buttonsHBox = new HBox();
    private HBox statusHBox = new HBox();
    private VBox bottomVBox = new VBox();
    private ObservableList<Node> list1 = vBox.getChildren();
    private ObservableList<Node> list2 = vBox2.getChildren();
    private ObservableList<Node> list3 = buttonsHBox.getChildren();
    private ObservableList<Node> list4 = vBox3.getChildren();
    private ObservableList<Node> list5 = statusHBox.getChildren();
    private ObservableList<Node> list6 = bottomVBox.getChildren();
    private ListView<String> recordsArea = new ListView<String>();
    private BorderPane border = new BorderPane();
    private String vehicleImage = "images/no_car.jpg";
//main method

    public void start(Stage primaryStage) throws FileNotFoundException {
        input = new FileInputStream(vehicleImage);
        image = new Image((InputStream) input);
        imageView = new ImageView(image);
        imageView.setFitHeight(300.0);
        imageView.setFitWidth(300.0);
        imageView.setPreserveRatio(true);
        recordsArea.setPrefHeight(270);
        border.setTop(createMenu(primaryStage));
        border.setLeft((Node) vBox);
        border.setCenter((Node) vBox2);
        border.setRight((Node) vBox3);
        border.setBottom(bottomVBox);
        vBox.setSpacing(10.0);
        vBox2.setSpacing(10.0);
        vBox3.setSpacing(10.0);
        VBox.setMargin((Node) typeLabel, (Insets) new Insets(20.0, 0.0, 0.0, 20.0));
        VBox.setMargin((Node) idLabel, (Insets) new Insets(20.0, 0.0, 0.0, 20.0));
        VBox.setMargin((Node) lastMntcLabel, (Insets) new Insets(20.0, 0.0, 0.0, 20.0));
        VBox.setMargin((Node) yearLabel, (Insets) new Insets(20.0, 0.0, 20.0, 20.0));
        VBox.setMargin((Node) makeLabel, (Insets) new Insets(10.0, 10.0, 10.0, 20.0));
        VBox.setMargin((Node) modelLabel, (Insets) new Insets(10.0, 10.0, 10.0, 20.0));
        VBox.setMargin((Node) type, (Insets) new Insets(20.0, 0.0, 0.0, 20.0));
        VBox.setMargin((Node) id, (Insets) new Insets(20.0, 10.0, 0.0, 20.0));
        VBox.setMargin((Node) lastMntc, (Insets) new Insets(20.0, 5.0, 5.0, 10.0));
        VBox.setMargin((Node) year, (Insets) new Insets(20.0, 5.0, 5.0, 10.0));
        VBox.setMargin((Node) make, (Insets) new Insets(20.0, 5.0, 5.0, 10.0));
        VBox.setMargin((Node) model, (Insets) new Insets(20.0, 5.0, 5.0, 10.0));
        VBox.setMargin((Node) imageView, (Insets) new Insets(20.0, 20.0, 20.0, 20.0));
        HBox.setMargin((Node) rentButton, (Insets) new Insets(20.0, 20.0, 20.0, 20.0));
        HBox.setMargin((Node) returnButton, (Insets) new Insets(20.0, 20.0, 20.0, 20.0));
        HBox.setMargin((Node) startMaintenanceButton, (Insets) new Insets(20.0, 20.0, 20.0, 20.0));
        HBox.setMargin((Node) completeMaintenanceButton, (Insets) new Insets(20.0, 20.0, 20.0, 20.0));
        HBox.setMargin((Node) statusLabel, (Insets) new Insets(20.0, 20.0, 20.0, 20.0));
        HBox.setMargin((Node) status, (Insets) new Insets(20.0, 20.0, 20.0, 20.0));
        VBox.setMargin((Node) recordsArea, (Insets) new Insets(20.0, 20.0, 20.0, 20.0));
        list1.addAll(typeLabel, idLabel, lastMntcLabel, yearLabel, makeLabel, modelLabel);
        list2.addAll(type, id, lastMntc, year, make, model);
        list3.addAll(rentButton, returnButton, startMaintenanceButton, completeMaintenanceButton);
        list4.addAll(imageView, statusHBox);
        list5.addAll(statusLabel, status);
        list6.addAll(recordsArea, buttonsHBox);
        createObservableList();
        createRentButtonEvents(rentButton);
        createReturnButtonEvents(returnButton);
        addEventCompMaintenanceButton(completeMaintenanceButton);
        addEventStrtMaintenanceButton(startMaintenanceButton);
        showStage(primaryStage);
    }
//Show stage method

    private void showStage(Stage primaryStage) {
        Scene scene = new Scene((Parent) border, 800.0, 820.0);
        primaryStage.setTitle("ThriftyRent");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    //refresh the vehicle data
    public void refresh() {
        observableVehicleList.forEach((vehicle) -> {
            refreshObservableVehicleList.add(vehicle);
        });

        observableVehicleList.clear();
        observableVehicleList.setAll(refreshList);
        refreshObservableVehicleList.clear();
        refreshList.clear();
    }

    //add set the vehicle to be shown
    public void setVehicle(controller.BaseVehicle vehicle) {
        observableVehicleList.add(vehicle);
    }

    public static void main(String[] args) {
        AddVehicle.launch((String[]) args);
    }
}
