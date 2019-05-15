/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import view.MainWindow;

public class FileHandling {
    
    //Method to acess and return the file
    private static File openFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        File selectedFile = fileChooser.showOpenDialog(stage);
        return selectedFile;
    }
    
    //Create a rental record with the data provided
    private static void createRentalRecord(String data[],String id){
        controller.RentalRecord rr = new RentalRecord();
                        rr.setRecordID(data[0]);
                        rr.setVehicleID(id);
                        rr.setCustomerID(data[1]);
                        rr.setRentDate(DateTime.formatDate(data[2]));
                        rr.setEstimatedReturnDate(DateTime.formatDate(data[3]));
                        rr.setActualReturnDate(DateTime.formatDate(data[4]));
                        rr.setRentalFee(Double.parseDouble(data[5].replaceAll(",", ".")));
                        rr.setLateFee(Double.parseDouble(data[6].replaceAll(",", ".")));
                        rr.save();
    }
    
    //Create a vehicle with the data provided
    private static void createVehicle(String data[],String id){
                            id = data[0].toUpperCase();
                        int year = Integer.parseInt(data[1]);
                        String make = data[2];
                        String mode = data[3];
                        int passangers = Integer.parseInt(data[5]);
                        String status = data[6];
                        if (data[0].toUpperCase().startsWith("C")) {
                            String image = data[7];
                            controller.Car car = null;
                            try {
                                car = new controller.Car(id, year, make, mode, passangers, image);
                                if (status.equals("AVALIABLE")) {
                                    car.setVehicleStatus(VehicleStatus.AVALIABLE);
                                } else {
                                    if (status.equals("RENTED")) {
                                        car.setVehicleStatus(VehicleStatus.RENTED);
                                    } else {
                                        car.setVehicleStatus(VehicleStatus.MAINTENANCE);
                                    }
                                }
                                car.saveVehicle();
                            } catch (InvalidIdException ex) {
                                Logger.getLogger(FileHandling.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            //car
                        } else {
                            if (data[0].toUpperCase().startsWith("V")) {
                                DateTime lastmaintenance = DateTime.formatDate(data[7]);
                                String image = data[8];
                                controller.Van van = null;
                                try {
                                    van = new controller.Van(id, year, make, mode, lastmaintenance, image);
                                    if (status.equals("AVALIABLE")) {
                                        van.setVehicleStatus(VehicleStatus.AVALIABLE);
                                    } else {
                                        if (status.equals("RENTED")) {
                                            van.setVehicleStatus(VehicleStatus.RENTED);
                                        } else {
                                            van.setVehicleStatus(VehicleStatus.MAINTENANCE);
                                        }
                                    }
                                    van.saveVehicle();
                                } catch (InvalidIdException ex) {
                                    Logger.getLogger(FileHandling.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }                            //van
                        }
}
    
   //import the file main method
    public static void _import(Stage stage) {
        //Create a file chooser object and add a file restriction to it and show
        File selectedFile = openFile(stage);
        if (selectedFile != null) {
            //if a file was selected read the line split and organize the data then set it to a object and save it
            try (BufferedReader br = Files.newBufferedReader(Paths.get(selectedFile.getAbsolutePath()))) {
                String line;
                String id = null;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(":");
                    if (data.length == 7) {
                        createRentalRecord(data, id);
                    } else {
                        createVehicle(data, id);
                    }
                }
            } catch (IOException e) {
                ExceptionHandler.ShowExceptionAlert("Couldn´t open the file!");
            }
        }
    }
   //export the file main method
    public static void export(Stage stage) {
        //create a directory chooser and show the dialog
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory != null) {
            //if a directory is selected
            try {
                //Create the text file retrieve the current objects from database and write the file
                PrintWriter writer = new PrintWriter(selectedDirectory.getAbsolutePath() + "\\export_data.txt", "UTF-8");
                model.Database database = new model.Database();
                Connection conn = database.initialize("database.db");
                ObservableList<controller.BaseVehicle> exportList = model.Database.getVehicles(conn);
                for (BaseVehicle baseVehicle : exportList) {

                    if (baseVehicle instanceof Car) {
                        Car acar = (Car) baseVehicle;
                        ObservableList<controller.RentalRecord> observableRecordList = acar.getRentalRecords();
                        writer.println(acar.toString());
                        for (RentalRecord rentalRecord : observableRecordList) {
                            writer.println(rentalRecord.toString());

                        }
                    } else {

                        Van avan = (Van) baseVehicle;
                        ObservableList<controller.RentalRecord> observableRecordList = avan.getRentalRecords();
                        writer.println(avan.toString());
                        for (RentalRecord rentalRecord : observableRecordList) {
                            writer.println(rentalRecord.toString());

                        }
                    }

                }
                writer.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

}
