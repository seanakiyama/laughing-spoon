package controller;

import static controller.BaseVehicle.FRIDAY;
import static controller.BaseVehicle.SATURDAY;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Van extends BaseVehicle implements VehicleMethods {

//constructor
    public Van(String _vehicleID, int _year, String _make, String _model, DateTime _lastMaintenanceDate,String _image) throws InvalidIdException {
        super(_vehicleID, _year, _make, _model, 15, VehicleType.VAN, _lastMaintenanceDate,_image);
        if (_vehicleID.toUpperCase().charAt(0) != 'V') {

                throw new InvalidIdException("Invalid vehicleID prefix");
     
        }
    }

    public void rent(String customerID, DateTime rentDate, int numOfRentDay) throws RentException {
            //check if the van can be rented
        if (lastMaintenanceDate == null) {
 
                throw new RentException("Book for less than 15 days!");


        }

        if (super.vehicleStatus == VehicleStatus.MAINTENANCE || super.vehicleStatus == VehicleStatus.RENTED) {

                throw new RentException("Vehicle in maintenance or rented!");

      
        }

        if (numOfRentDay < 1) {

                throw new RentException("Please input a positive number of days!");

  
        }
     
        DateTime maxNextMaintenanceDate = new DateTime(lastMaintenanceDate, 12);
        if (DateTime.diffDays(maxNextMaintenanceDate, new DateTime(rentDate, numOfRentDay)) < 0) {

                throw new RentException("Estimated rent date will exceed maintenance schedule!");
  
       
        }
       //add the rental record

        super.addRental(customerID, rentDate, numOfRentDay, calculateEstimatedRentalFee(numOfRentDay));

    }
    //calculate the estimated rental fee
    public int calculateEstimatedRentalFee(int numOfRentDay) {
        return numOfRentDay * 235;
    }
//calculate the rental fee
    private double calculateLateFee(int lateDays) {
        if (lateDays <= 0) {return 0.0;}

        return 299 * lateDays;
    }
    //return the vehicle method
    public void returnVehicle(DateTime returnDate, RentalRecord rentalRecord) {
        //get the rent date of the rental record
               DateTime rentDate = rentalRecord.rentDate;
       //get the number of days on rental
        int daysAtRental = DateTime.diffDays(returnDate, rentDate);
      //check if vehicle can be returned
        if (returnDate.getTime() < rentDate.getTime()) { try {
            //  unnecessary if diffDays can return negative
            throw new ReturnException("Return date is lower than rent date!");
                   } catch (ReturnException ex) {
                       Logger.getLogger(Van.class.getName()).log(Level.SEVERE, null, ex);
                   }
            return;
        }
        if (daysAtRental < 2) {
                   try {
                       throw new ReturnException("The number of days at rental need to be higher than 2!");
                   } catch (ReturnException ex) {
                       Logger.getLogger(Van.class.getName()).log(Level.SEVERE, null, ex);
                   }
            return;
        }

        if ( (getDayOfWeek(rentDate) == FRIDAY || getDayOfWeek(rentDate) == SATURDAY) && daysAtRental < 3) {
                   try {
                       throw new ReturnException("You can return the vehicle today!");
                   } catch (ReturnException ex) {
                       Logger.getLogger(Van.class.getName()).log(Level.SEVERE, null, ex);
                   }
             return;
        }
         //change the vehicle status
        vehicleStatus = VehicleStatus.AVALIABLE;
        //get the number of late days
        int lateDays = DateTime.diffDays(returnDate, rentalRecord.estimatedReturnDate);
        //if is lower than 0 set it to 0
        if (lateDays <= 0) {lateDays = 0;}
        //set the actual return date
        rentalRecord.setActualReturnDate(returnDate);
        //set the rental fee
        rentalRecord.setRentalFee(calculateEstimatedRentalFee(daysAtRental - lateDays));
        //Set the late fee
        rentalRecord.setLateFee(calculateLateFee(lateDays) );
        //create the database connection
        model.Database database = new model.Database();
        Connection conn = database.initialize("database.db");
        
        setRentalRecordData(rentalRecord, conn, returnDate, calculateEstimatedRentalFee(daysAtRental - lateDays), calculateLateFee(lateDays));
        setVehicleAvaliable(rentalRecord, conn);
    }
    
    
  

}
