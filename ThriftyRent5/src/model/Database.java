/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import controller.DateTime;
import controller.InvalidIdException;
import controller.VehicleStatus;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Database {

    
    public static Connection initialize(String fileName) {
        //defines the path to the database
        String url = "jdbc:sqlite:database/" + fileName;
        //create the connections object
        Connection conn = null;
        try {
            //try create the connection to the database using the drive manager
            conn = DriverManager.getConnection(url);
            //verify if the connection was created successfully
            if (conn != null) {
                //Create a meta object with the meta that is on the connection
                DatabaseMetaData meta = conn.getMetaData();
                //create tables if they do not exist
                //Create a statement
                Statement stmt = conn.createStatement();
                //Run SQL to create table vehicles
                stmt.execute("CREATE TABLE IF NOT EXISTS VEHICLES(vehicle_id STRING PRIMARY KEY,vehicle_year int,vehicle_model STRING,vehicle_make STRING,vehicle_numPassengerSeats INT,vehicle_lastMaintenanceDate STRING,vehicle_type STRING,vehicle_status STRING,vehicle_image STRING)");
                //Run SQL to create table RENTAL_RECORDS
                stmt.execute("CREATE TABLE IF NOT EXISTS RENTAL_RECORDS(record_id STRING PRIMARY KEY,record_vehicleid STRING,record_customerid STRING,record_rentDate STRING,record_estimatedReturnDate STRING,record_actualReturnDate STRING,record_rentalFee REAL,record_lateFee REAL,FOREIGN KEY(record_vehicleid) REFERENCES VEHICLES(vehicle_id))");
            }
        } catch (SQLException e) {
            //System.out.println(e.getMessage());
        }
        //return database connection
        return conn;
    }

    public static Statement getStatement(Connection conn){
        //define statement variable
        Statement stmt = null;
        try {
            //check if the connection exists
            if (conn != null) {
                //create a statement 
                stmt = conn.createStatement();
            }
        } catch (SQLException e) {
            //System.out.println(e.getMessage());
        }
        //return statement
        return stmt;
    }
    //create a variable to hold the were clause to the getVehicles method
    private static String where = null;
    //return all vehicles with a requisite
    public static ObservableList getVehicles(Connection conn,String _where){
        //set the where clause on the local variable
        where = _where;
        //return the vehicles
        return  getVehicles(conn);
    }
   //return all vehicles
    public static ObservableList getVehicles(Connection conn) {
        List<controller.BaseVehicle> list = new ArrayList<>();
        ObservableList<controller.BaseVehicle> observableVehicleList = FXCollections.observableList(list);
        String sql = "";
        if(where == null){
        sql = "SELECT vehicle_id,vehicle_year, vehicle_make, vehicle_model, vehicle_numPassengerSeats, vehicle_lastMaintenanceDate, vehicle_type, vehicle_status, vehicle_image FROM VEHICLES";
        }else{
             sql = "SELECT vehicle_id,vehicle_year, vehicle_make, vehicle_model, vehicle_numPassengerSeats, vehicle_lastMaintenanceDate, vehicle_type, vehicle_status, vehicle_image FROM VEHICLES where 1=1  " + where;
             where = null;
        }
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String _vehicleID = rs.getString("vehicle_id");
                String _make = rs.getString("vehicle_make");
                String _model = rs.getString("vehicle_model");
                int _numseats = rs.getInt("vehicle_numPassengerSeats");
                DateTime _lastmaintenance = DateTime.formatDate(rs.getString("vehicle_lastMaintenanceDate"));
                int  _year = rs.getInt("vehicle_year");
                String _type = rs.getString("vehicle_type");
                String _status = rs.getString("vehicle_status");
                String _vehicleImage = rs.getString("vehicle_image");
                controller.BaseVehicle vehicle = null;
                if (_type.equals("Car")) {
                    try {
                        vehicle = new controller.Car(_vehicleID, _year, _make, _model, _numseats,  _vehicleImage);
                    } catch (InvalidIdException ex) {
                        Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    try {
                        vehicle = new controller.Van(_vehicleID, _year, _make, _model, _lastmaintenance, _vehicleImage);
                    } catch (InvalidIdException ex) {
                        Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (_status.equals("AVALIABLE")) {
                    vehicle.setVehicleStatus(VehicleStatus.AVALIABLE);
                } else if (_status.equals("RENTED")) {
                    vehicle.setVehicleStatus(VehicleStatus.RENTED);
                } else {
                    vehicle.setVehicleStatus(VehicleStatus.MAINTENANCE);
                }
                observableVehicleList.add(vehicle);
            }
        } catch (SQLException e) {
        }
        return observableVehicleList;
    }

}
