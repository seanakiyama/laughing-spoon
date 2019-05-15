package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import view.VehicleDetail;

public abstract class BaseVehicle {

    String details = "RENTAL RECORD";
    //create the variable to hold the vehicle id
    protected String vehicleID;
    //create the variable to hold the vehicle year
    private int year;
    //create the variable to hold the vehicle makeid
    private String make;

    //getter and set methods to access the local variables
    public String getVehicleID() {
        return vehicleID;
    }
//getter and set methods to access the local variables
    public void setVehicleID(String vehicleID) {
        this.vehicleID = vehicleID;
    }
//getter and set methods to access the local variables
    public int getYear() {
        return year;
    }
//getter and set methods to access the local variables
    public void setYear(int year) {
        this.year = year;
    }
//getter and set methods to access the local variables
    public String getMake() {
        return make;
    }
//getter and set methods to access the local variables
    public void setMake(String make) {
        this.make = make;
    }
//getter and set methods to access the local variables
    public String getModel() {
        return model;
    }
//getter and set methods to access the local variables
    public void setModel(String model) {
        this.model = model;
    }
//getter and set methods to access the local variables
    public int getNumPassengerSeats() {
        return numPassengerSeats;
    }
//getter and set methods to access the local variables
    public void setNumPassengerSeats(int numPassengerSeats) {
        this.numPassengerSeats = numPassengerSeats;
    }
//getter and set methods to access the local variables
    public DateTime getLastMaintenanceDate() {
        return lastMaintenanceDate;
    }
//getter and set methods to access the local variables
    public void setLastMaintenanceDate(DateTime lastMaintenanceDate) {
        this.lastMaintenanceDate = lastMaintenanceDate;
    }
//getter and set methods to access the local variables
    public VehicleType getVehicleType() {
        return vehicleType;
    }
//getter and set methods to access the local variables
    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }
//getter and set methods to access the local variables
    public VehicleStatus getVehicleStatus() {
        return vehicleStatus;
    }
//getter and set methods to access the local variables
    public void setVehicleStatus(VehicleStatus vehicleStatus) {
        this.vehicleStatus = vehicleStatus;
    }
//getter and set methods to access the local variables
    public String getImage() {
        return image;
    }
//getter and set methods to access the local variables
    public void setImage(String image) {
        this.image = image;
    }
        //create the variable to hold the vehicle model
    private String model;
    //create the variable to hold the vehicle number of seats
    protected int numPassengerSeats;
    //create the variable to hold the last maintenance day
    protected DateTime lastMaintenanceDate;
    //create the variable to hold the vehicle type
    protected VehicleType vehicleType;
    //create the variable to hold the vehicle status
    protected VehicleStatus vehicleStatus;
    //create the variable to hold the vehicle image
    protected String image;

    //define the number of the day in the week as a constant
    public static final int FRIDAY = 6;
    public static final int SATURDAY = 7;

    //constructor of the class
    public BaseVehicle(String _vehicleID, int _year, String _make, String _model, int _numPassengerSeats, VehicleType _vehicleType, DateTime _lastMaintenanceDate, String _image) {
        vehicleID = _vehicleID;
        year = _year;
        make = _make;
        image = _image;
        model = _model;
        numPassengerSeats = _numPassengerSeats;
        vehicleType = _vehicleType;
        vehicleStatus = VehicleStatus.AVALIABLE;
        lastMaintenanceDate = _lastMaintenanceDate;
    }
    
    public void saveVehicle() {
        //Create a database object
        model.Database database = new model.Database();
        //create the connection with the database
        Connection conn = database.initialize("database.db");

        try {
            //create the statement to create the vehicle
            PreparedStatement stmt = conn.prepareStatement("insert into VEHICLES(vehicle_id,vehicle_year,vehicle_make,vehicle_model,vehicle_numPassengerSeats,vehicle_lastMaintenanceDate,vehicle_type,vehicle_status,vehicle_image) values(?,?,?,?,?,?,?,?,?)");
            //replace the data in the statement
            stmt.setString(1, this.vehicleID);      
            stmt.setInt(2, this.getYear());            
            stmt.setString(3, this.getMake());            
            stmt.setString(4, this.getModel());           
            stmt.setInt(5, this.numPassengerSeats);
            
            //check the vehicle type
            if (this.getVehicleType() == VehicleType.VAN) {
                stmt.setString(6, this.getLastMaintenanceDate().toString());
                stmt.setString(7, "Van");
            } else {
                stmt.setString(6, DateTime.getCurrentTime());
                stmt.setString(7, "Car");
            }

            stmt.setString(8, "AVALIABLE");
            stmt.setString(9, this.getImage());
            //execute the statement
            stmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(BaseVehicle.class.getName()).log(Level.SEVERE, null, ex);
            //System.out.println(ex);
        }
    }

    public void addRental(String customerID, DateTime rentDate, int numOfRentDay, double estimatedRentalFee) {
        //create a rental record to hold the data
        RentalRecord rentalRecord = new RentalRecord(this.vehicleID, customerID, rentDate, new DateTime(rentDate, numOfRentDay), estimatedRentalFee);
        //change the vehicle status
        this.vehicleStatus = VehicleStatus.RENTED;

        //create a database
        model.Database database = new model.Database();
        //create a connection with the database
        Connection conn = database.initialize("database.db");

        try {
            //update the vehicle status to rented in the database
            PreparedStatement stmt = conn.prepareStatement("UPDATE VEHICLES SET vehicle_status = ? WHERE vehicle_id = ?");
            stmt.setString(1, "RENTED");
            stmt.setString(2, this.vehicleID);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(VehicleType.class.getName()).log(Level.SEVERE, null, ex);
            //System.out.println(ex);
        }
       //save the rental record
        rentalRecord.save();

    }

    @Override
    public String toString() {
        //return the vehicle information formatted to be exported
        if (vehicleType == VehicleType.VAN) {
            return vehicleID + ":" + Integer.toString(year) + ":" + make + ":" + model + ":" + vehicleType.toString() + ":" + Integer.toString(numPassengerSeats) + ":" + vehicleStatus.toString()  + ":" + lastMaintenanceDate.toString() + ":" + getImage();
        } else {
            return vehicleID + ":" + Integer.toString(year) + ":" + make + ":" + model + ":" + vehicleType.toString() + ":" + Integer.toString(numPassengerSeats) + ":" + vehicleStatus.toString() + ":" + getImage();
        }
    }

//return all rental records of the vehicle
    public ObservableList<controller.RentalRecord> getRentalRecords() {
     //create a list to hold the rental records
        List<controller.RentalRecord> recordsList = new ArrayList<>();
        //create a observable list to hold the records
        ObservableList<controller.RentalRecord> observableRecordList = FXCollections.observableList(recordsList);
       //create the query
        String sql = " SELECT * "
                + " FROM RENTAL_RECORDS where record_vehicleid = ? ";
        //create the variables and initialize them
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
            //get the database connection
            model.Database database = new model.Database();
            Connection conn = database.initialize("database.db");
            //create the statement and replace the value to the id in the query
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, this.getVehicleID());
            //execute the query and put it in the result set
            ResultSet rs = stmt.executeQuery();
            // loop through the result set
            while (rs.next()) {
                //retrieve the data returned from database to variables
                _recordID = rs.getString("record_id");
                _vehicleID = rs.getString("record_vehicleid");
                _customerID = rs.getString("record_customerid");
                _rentDate = rs.getString("record_rentDate");
                _estReturnDate = rs.getString("record_estimatedReturnDate");
                _actualReturnDate = rs.getString("record_actualReturnDate");
                _rentalFee = rs.getDouble("record_rentalFee");
                _lateFee = rs.getDouble("record_lateFee");
                rentDate = controller.DateTime.formatDate(_rentDate);
                estReturnDate = controller.DateTime.formatDate(_estReturnDate);
                if (_actualReturnDate != null) {
                    actualReturnDate = controller.DateTime.formatDate(_actualReturnDate);
                }
               //create a rental record object with the information
                RentalRecord rentalRecord = new RentalRecord(_vehicleID, _customerID, rentDate, estReturnDate, _rentalFee);
                rentalRecord.actualReturnDate = actualReturnDate;
                rentalRecord.lateFee = _lateFee;
                //add the rental record to the list
                observableRecordList.add(rentalRecord);
            }
        } catch (SQLException e) {
        }
        //return the list
        return observableRecordList;
    }
//Return rentalrecords in string form
    public String getRRecordDetais(){
        return getRentalRecordDetails();
        
    }
  //create the rental record string  
    private String getRentalRecordDetails() {
          //create and observable list containing the rental records
        ObservableList<controller.RentalRecord> observableRecordList = getRentalRecords();
         //if the list is empty return will be empty
        if (observableRecordList.isEmpty()) {
            details = details.concat(":\t\t\t\tempty");
        }
        //create the return string
        observableRecordList.forEach((record) -> {
            details = details.concat("\n");
            details = details.concat(record.getDetails());
            details = details.concat("-------------------------------------\n");
        });
        //return the variable containing the data
        return details;

    }
//get the day of the week of a date
    public int getDayOfWeek(DateTime dateTime){
            //get the calendar object and return the number of the day of the week
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dateTime.getTime());

        return c.get(Calendar.DAY_OF_WEEK);
    }
//start maintenance
    public void performMaintenance() {
        //change the maintenance data of the vehicle 
        if (vehicleStatus != VehicleStatus.AVALIABLE) {
            ExceptionHandler.ShowExceptionAlert("Vehicle already is in maintenance");
        }

        vehicleStatus = VehicleStatus.MAINTENANCE;
           model.Database database = new model.Database();
            Connection conn = database.initialize("database.db");
            try {
                //save vehicle
                PreparedStatement stmt = conn.prepareStatement("UPDATE VEHICLES SET vehicle_status = ? WHERE vehicle_id = ?");
                stmt.setString(1, "MAINTENANCE");
                stmt.setString(2, this.getVehicleID());
                stmt.executeUpdate();
            } catch (SQLException ex) {
                Logger.getLogger(VehicleDetail.class.getName()).log(Level.SEVERE, null, ex);
                //System.out.println(ex);
            }
            Dialog dialog = new Dialog<>();
            dialog.setTitle("Vehicle Maintenance");
            dialog.setHeaderText(("Vehicle ") + this.getVehicleID() + " is now being maintained");
            dialog.setResizable(true);
            ButtonType buttonTypeOk = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
            Optional result = dialog.showAndWait();

    }
//finish maintenance
    public void completeMaintenance(DateTime completionDate) {
        //change the maintenance data of the vehicle 
        
        if (vehicleStatus != VehicleStatus.MAINTENANCE) {
            ExceptionHandler.ShowExceptionAlert("Vehicle is not in maintenance");
        }

        lastMaintenanceDate = completionDate;
        vehicleStatus = VehicleStatus.AVALIABLE;
        model.Database database = new model.Database();
           Connection conn = database.initialize("database.db");
            try {
                //save vehicle
                PreparedStatement stmt = conn.prepareStatement("UPDATE VEHICLES set vehicle_status = ? WHERE vehicle_id = ?");
                stmt.setString(1, "AVALIABLE");
                stmt.setString(2, this.getVehicleID());
                stmt.executeUpdate();
            } catch (SQLException ex) {
                Logger.getLogger(VehicleDetail.class.getName()).log(Level.SEVERE, null, ex);
                //System.out.println(ex);
            }
            Dialog dialog = new Dialog<>();
            dialog.setTitle("Vehicle Maintenance");
            dialog.setHeaderText(("Vehicle ") + this.getVehicleID() + " has completed maintainance");
            dialog.setResizable(true);
            ButtonType buttonTypeOk = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
            Optional result = dialog.showAndWait();

    }
    //set data on a rental record
      public void setRentalRecordData(RentalRecord rentalRecord, Connection conn,DateTime returnDate,double estimatedRentalFee,double lateFee){
              try {
            //update the rental record
            PreparedStatement stmt = conn.prepareStatement("UPDATE RENTAL_RECORDS SET record_actualReturnDate = ?, record_rentalFee = ?, record_lateFee = ? WHERE record_id = ?");
            stmt.setString(1, returnDate.toString());
            stmt.setDouble(2,  estimatedRentalFee);
            stmt.setDouble(3,lateFee);
            stmt.setString(4, rentalRecord.recordID);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(Car.class.getName()).log(Level.SEVERE, null, ex);
            //System.out.println(ex);
        }
    }
    //set the vehicle as available
    public void setVehicleAvaliable(RentalRecord rentalRecord, Connection conn){
            try {
            //change the rental property status
            PreparedStatement stmt = conn.prepareStatement("UPDATE VEHICLES SET vehicle_status = ? WHERE vehicle_id = ?");
            stmt.setString(1, "AVALIABLE");
            stmt.setString(2, rentalRecord.vehicleID);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(Car.class.getName()).log(Level.SEVERE, null, ex);
            //System.out.println(ex);
        }    
    }


}
