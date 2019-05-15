/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import controller.FileHandling;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class MainWindow
        extends Application {

    private Stage primaryStageBackup;
    //create the database connections
    private model.Database database = new model.Database();
    private Connection conn = database.initialize("database.db");
    //create the variables to hold the vehicle
    private ObservableList<controller.BaseVehicle> observableVehicleList;

    private List<controller.BaseVehicle> refreshList = new ArrayList<>();
    private ObservableList<controller.BaseVehicle> refreshObservableVehicleList = FXCollections.observableList(refreshList);
    private String where = null;
//create the menu bar
    private MenuBar createMenuBar() {
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
                FileHandling._import(primaryStageBackup);
            }
        });
        exportMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                FileHandling.export(primaryStageBackup);
            }
        });
        dataMenu.getItems().add(importMenuItem);
        dataMenu.getItems().add(exportMenuItem);
        menuBar.getMenus().add(dataMenu);

        Label menuLabel = new Label("Refresh");
        menuLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                refresh();
            }
        });

        Menu refreshMenu = new Menu();
        refreshMenu.setGraphic(menuLabel);
        menuBar.getMenus().add(refreshMenu);

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
//main method to run 
    public void start(Stage primaryStage) {
        //create the design of the gui like the car list menus and take care of the events 
        primaryStageBackup = primaryStage;
        observableVehicleList = model.Database.getVehicles(conn, where);
        where = null;
        TilePane tile = new TilePane();
        tile.setPadding(new Insets(15, 750, 15, 15));
        tile.setVgap(15);
        tile.setHgap(15);
        tile.setPrefColumns(1);
        //might bring problems
        VBox pages[] = new VBox[20];
        createCarList(pages, tile);
        ScrollPane scrollPane = new ScrollPane(tile);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox vBox2 = new VBox();
        ObservableList<Node> list = vBox2.getChildren();
        Label idLabel = new Label("ID");
        VBox.setMargin((Node) idLabel, (Insets) new Insets(20.0, 20.0, 20.0, 20.0));
        list.addAll(createSearchBar());
        BorderPane root2 = new BorderPane(vBox2);
        root2.setTop(createMenuBar());
        vBox2.getChildren().add(scrollPane);
        Scene scene = new Scene((Parent) root2, 1000.0, 800.0);
        primaryStage.setTitle("ThriftyRent");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
//create the car list
    private void createCarList(VBox pages[], TilePane tile) {
        observableVehicleList.forEach((vehicle) -> {
            FileInputStream input = null;
            try {
                input = new FileInputStream(vehicle.getImage());
                Image image = new Image((InputStream) input);
                ImageView imageView = new ImageView(image);
                imageView.setFitHeight(200.0);
                imageView.setFitWidth(200.0);
                VBox vbox = new VBox();
                vbox.setPadding(new Insets(10));
                vbox.setSpacing(8);
                Text title = new Text(vehicle.getVehicleID());
                Text makemodel = new Text(vehicle.getMake() + " - " + vehicle.getModel());
                Text description = new Text(vehicle.getYear() + "");
                Text passengers = new Text("No. of Passengers: " + vehicle.getNumPassengerSeats());
                Label status = new Label();
                fillStatusLabel(vehicle, status);
                title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                vbox.getChildren().add(createDescription(vehicle, imageView, title, makemodel, description, passengers, status));
                VBox.setMargin(title, new Insets(10, 0, 0, 8));
                VBox.setMargin(makemodel, new Insets(10, 0, 0, 8));
                VBox.setMargin(description, new Insets(10, 0, 0, 8));
                VBox.setMargin(passengers, new Insets(10, 0, 0, 8));
                VBox.setMargin(status, new Insets(60, 0, 0, 8));
                VBox.setMargin(imageView, new Insets(0, 0, 0, 8));
                pages[observableVehicleList.indexOf(vehicle)] = vbox;
                tile.getChildren().add(pages[observableVehicleList.indexOf(vehicle)]);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    input.close();
                } catch (IOException ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
//set the status label with condition
    private void fillStatusLabel(controller.BaseVehicle vehicle, Label status) {
        if (null == vehicle.getVehicleStatus()) {
            status.setText("MAINTENANCE");
            status.setTextFill(Color.web("#8B0000"));
        } else {
            switch (vehicle.getVehicleStatus()) {
                case AVALIABLE:
                    status.setText("AVALIABLE");
                    status.setTextFill(Color.web("#3CB371"));
                    break;
                case RENTED:
                    status.setText("RENTED");
                    status.setTextFill(Color.web("#696969"));
                    break;
                default:
                    status.setText("MAINTENANCE");
                    status.setTextFill(Color.web("#8B0000"));
                    break;
            }
        }
    }
//create the descriptions data
    private HBox createDescription(controller.BaseVehicle vehicle, ImageView imageView, Text title, Text makemodel, Text description, Text passengers,
            Label status) {
        HBox hbox = new HBox();
        hbox.setStyle("-fx-background-color: DCDCDC;");
        hbox.getChildren().add(imageView);
        VBox vboxer = new VBox();
        vboxer.getChildren().add(title);
        vboxer.getChildren().add(makemodel);
        vboxer.getChildren().add(description);
        vboxer.getChildren().add(passengers);
        vboxer.getChildren().add(status);
        hbox.getChildren().add(vboxer);
        final Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setMinSize(490, 1);
        final Pane spacer2 = new Pane();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        spacer2.setMinSize(10, 1);
        final Button right = new Button("Details");
        right.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        right.setPrefSize(100, 60);
        right.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
        right.setOnMouseClicked(e -> {
            VehicleDetail vehicleDetail = new VehicleDetail();
            Stage stage = new Stage();
            //refresh on close
            stage.setOnHiding(event -> {
                refresh();
            });
            try {
                vehicleDetail.start(stage);
                vehicleDetail.setVehicle(vehicle);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        hbox.getChildren().addAll(spacer, right, spacer2);
        hbox.setMargin(right, new Insets(70, 0, 0, 8));
        return hbox;
    }
//create the search bar
    private HBox createSearchBar() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        Button buttonSearch = new Button("Search");
        buttonSearch.setPrefSize(100, 20);
        Label typeLabel = new Label("Type: ");
        ComboBox<String> typeCombo = new ComboBox<String>();
        typeCombo.setPrefSize(100, 20);
        typeCombo.getItems().add("Car");
        typeCombo.getItems().add("Van");
        Label seatsLabel = new Label("Seats: ");
        ComboBox<String> seatsCombo = new ComboBox<String>();
        seatsCombo.setPrefSize(100, 20);
        seatsCombo.getItems().add("4");
        seatsCombo.getItems().add("7");
        seatsCombo.getItems().add("15");
        Label statusLabel = new Label("Status: ");
        ComboBox<String> statusCombo = new ComboBox<String>();
        statusCombo.setPrefSize(100, 20);
        statusCombo.getItems().add("Avaliable");
        statusCombo.getItems().add("Rented");
        statusCombo.getItems().add("Maintenance");
        Label makeLabel = new Label("Make: ");
        ComboBox<String> makeCombo = new ComboBox<String>();
        makeCombo.setPrefSize(100, 20);
        makeCombo.getItems().add("Honda");
        makeCombo.getItems().add("Toyota");
        makeCombo.getItems().add("Vokswagen");
        makeCombo.getItems().add("Chevy");

        buttonSearch.setOnAction(createDetailButtonEvent(typeCombo, seatsCombo, statusCombo, makeCombo));
        hbox.getChildren().addAll(typeLabel, typeCombo, seatsLabel, seatsCombo, statusLabel, statusCombo, makeLabel, makeCombo, buttonSearch);
        return hbox;
    }
//create detail button events
    private EventHandler<ActionEvent> createDetailButtonEvent(ComboBox<String> typeCombo,
            ComboBox<String> seatsCombo,
            ComboBox<String> statusCombo,
            ComboBox<String> makeCombo) {
        EventHandler<ActionEvent> buttonHandler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String mountedWhere = "";
                if (typeCombo.getValue() != null) {
                    mountedWhere += " and vehicle_type = '" + typeCombo.getValue() + "' ";
                }
                if (seatsCombo.getValue() != null) {
                    mountedWhere += " and vehicle_numPassengerSeats = " + seatsCombo.getValue() + " ";
                }
                if (statusCombo.getValue() != null) {
                    mountedWhere += " and vehicle_status = '" + statusCombo.getValue().toUpperCase() + "' ";
                }
                if (makeCombo.getValue() != null) {
                    mountedWhere += " and vehicle_make = '" + makeCombo.getValue() + "' ";
                }
                where = mountedWhere;
                refresh();
                event.consume();
            }
        };
        return buttonHandler;
    }

    //refresh the list of cars 
    public void refresh() {
        start(primaryStageBackup);
        observableVehicleList.forEach((vehicle) -> {
            refreshObservableVehicleList.add(vehicle);
        });

        observableVehicleList.clear();
        observableVehicleList.setAll(refreshList);
        refreshObservableVehicleList.clear();
        refreshList.clear();
    }

    ;
    //launch
    public static void main(String[] args) {
        MainWindow.launch((String[]) args);
    }
}
