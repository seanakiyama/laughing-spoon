package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RentalRecord {

    protected String recordID;
    protected String vehicleID;
    protected String customerID;
    protected DateTime rentDate;
    protected DateTime estimatedReturnDate;
    protected DateTime actualReturnDate;
    protected double rentalFee;
    protected double lateFee;

    public RentalRecord() {
    }

    //create constructor
    public RentalRecord(String vehicleID, String _customerID, DateTime _rentDate, DateTime _estimatedReturnDate, double _rentalFee) {

        recordID = vehicleID + "_" + _customerID + "_" + _estimatedReturnDate.getEightDigitDate();
        this.vehicleID = vehicleID;
        customerID = _customerID;
        rentDate = _rentDate;
        estimatedReturnDate = _estimatedReturnDate;
        rentalFee = _rentalFee;
    }

    //method to set the late fee
    public void setLateFee(double lateFee) {
        this.lateFee = lateFee;
    }

    public void save() {
        //create a database connection and insert the current object into the database

        model.Database database = new model.Database();
        Connection conn = database.initialize("database.db");

        try {
            //save vehicle
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO "
                    + "RENTAL_RECORDS(record_id, record_vehicleid, record_customerid, "
                    + "record_rentDate, record_estimatedReturnDate, record_rentalFee) "
                    + "VALUES (?,?,?,?,?,?)");
            
            stmt.setString(1, this.recordID);
            stmt.setString(2, this.vehicleID);
            stmt.setString(3, this.customerID);
            stmt.setString(4, this.rentDate.toString());
            stmt.setString(5, this.estimatedReturnDate.toString());
            stmt.setDouble(6, this.rentalFee);

            stmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(BaseVehicle.class.getName()).log(Level.SEVERE, null, ex);
            //System.out.println(ex);
        }

    }

    //getter and setter methods
    public void setActualReturnDate(DateTime actualReturnDate) {
        this.actualReturnDate = actualReturnDate;
    }

    public DateTime getActualReturnDate() {
        return this.actualReturnDate;
    }

    public void setRentalFee(double rentalFee) {
        this.rentalFee = rentalFee;
    }

    public String getRecordID() {
        return recordID;
    }

    public void setRecordID(String recordID) {
        this.recordID = recordID;
    }

    public String getVehicleID() {
        return vehicleID;
    }

    public void setVehicleID(String vehicleID) {
        this.vehicleID = vehicleID;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public DateTime getRentDate() {
        return rentDate;
    }

    public void setRentDate(DateTime rentDate) {
        this.rentDate = rentDate;
    }

    public DateTime getEstimatedReturnDate() {
        return estimatedReturnDate;
    }

    public void setEstimatedReturnDate(DateTime estimatedReturnDate) {
        this.estimatedReturnDate = estimatedReturnDate;
    }
//convert the object to a formatted string

    @Override
    public String toString() {
        String details = recordID + ":" + customerID + ":" + rentDate.toString() + ":" + estimatedReturnDate.toString() + ":";
        if (actualReturnDate == null) {
            details = details + "none:none:none";
        } else {
            details = details.concat(String.format("%s:%.2f:%.2f", actualReturnDate.toString(), rentalFee, lateFee));
        }

        return details;
    }
//return a formatted string containing the data from the rental record

    public String getDetails() {
        String details = String.format("%-30s%s\n", "Record ID:", recordID);
        details = details.concat(String.format("%-30s%s\n", "Rent Date:", rentDate.toString()));
        details = details.concat(String.format("%-30s%s\n", "Estimated Return Date:", estimatedReturnDate.toString()));
        if (actualReturnDate != null) {
            details = details.concat(String.format("%-30s%s\n", "Actual Return Date:", actualReturnDate.toString()));
            details = details.concat(String.format("%-30s%.2f\n", "Rental Fee:", rentalFee));
            details = details.concat(String.format("%-30s%.2f\n", "Late Fee:", lateFee));
        }

        return details;
    }

}
