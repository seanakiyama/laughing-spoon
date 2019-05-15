package controller;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Car extends BaseVehicle implements VehicleMethods { // TODO implement PropertyMethods
    //create object contructor

    public Car(String _vehicleID, int _year, String _make, String _model, int _numPassengerSeats, String _image) throws InvalidIdException {
        super(_vehicleID, _year, _make, _model, _numPassengerSeats, VehicleType.CAR, null, _image);
        //check the information
        if (_vehicleID.toUpperCase().charAt(0) != 'C') {

            throw new InvalidIdException("Invalid id");

        }

    }
//rent the car

    public void rent(String customerID, DateTime rentDate, int numOfRentDay) throws RentException {
        //check if the vehicle can be rented
        if (super.vehicleStatus == VehicleStatus.MAINTENANCE || super.vehicleStatus == VehicleStatus.RENTED) {
            return;
        }

        if (numOfRentDay > 14) {

            throw new RentException("Book for less than 15 days!");

        }

        if (getDayOfWeek(rentDate) == FRIDAY || getDayOfWeek(rentDate) == SATURDAY) {
            if (numOfRentDay < 3) {

                throw new RentException("Rent date is Friday or Saturday. Book for more than 2 days!");

            }
        } else {
            if (numOfRentDay < 2) {

                throw new RentException("Book for more than 2 days!");

            }
        }
        //if can be rented add the rental record
        super.addRental(customerID, rentDate, numOfRentDay, calculateEstimatedRentalFee(numOfRentDay));

    }

//calculate the fee of the rental
    public double calculateEstimatedRentalFee(double numOfRentDay) {
        //calculate the rental fee
        switch (super.numPassengerSeats) {
            case (4):
                return 78 * numOfRentDay;
            case (7):
                return 113 * numOfRentDay;
            default:
                return -1;
        }
    }
//calculate the late fee

    private double calculateLateFee(int lateDays) {
        //calculate the rental fee if the late days are higher than 0
        if (lateDays <= 0) {
            return 0.0;
        }

        return calculateEstimatedRentalFee(1.25 * lateDays);
    }
//return the vehicle

    public void returnVehicle(DateTime returnDate, RentalRecord rentalRecord) {
        //get the rent date from the rentalrecord
        DateTime rentDate = rentalRecord.rentDate;
        //get how many days are in the rental
        int daysAtRental = DateTime.diffDays(returnDate, rentDate);
        //if return before rent dont
        if (returnDate.getTime() < rentDate.getTime()) {
            try {
                throw new ReturnException("Return date is lower than the rent date!");
            } catch (ReturnException ex) {
                Logger.getLogger(Car.class.getName()).log(Level.SEVERE, null, ex);
            }
            return;
        }
        //if days at rental lower than 2 don´t
        if (daysAtRental < 2) {
            try {
                throw new ReturnException("Days at rental need to be higher than 2!");
            } catch (ReturnException ex) {
                Logger.getLogger(Car.class.getName()).log(Level.SEVERE, null, ex);
            }
            return;
        }
        //check if the vehicle can be return at the current day
        if ((getDayOfWeek(rentDate) == FRIDAY || getDayOfWeek(rentDate) == SATURDAY) && daysAtRental < 3) {
            try {
                throw new ReturnException("You can´t return the vehicle today!");
            } catch (ReturnException ex) {
                Logger.getLogger(Car.class.getName()).log(Level.SEVERE, null, ex);
            }
            return;
        }
        //change the vehicle status
        vehicleStatus = VehicleStatus.AVALIABLE;
        //get the late days number
        int lateDays = DateTime.diffDays(returnDate, rentalRecord.estimatedReturnDate);
        //if late days lower than 0 it will be set as 0
        if (lateDays <= 0) {
            lateDays = 0;
        }
        //set data in the rental record
        rentalRecord.setActualReturnDate(returnDate);
        rentalRecord.setRentalFee(calculateEstimatedRentalFee(daysAtRental - lateDays));
        rentalRecord.setLateFee(calculateLateFee(lateDays));
        //create the database connections
        model.Database database = new model.Database();
        Connection conn = database.initialize("database.db");
        setRentalRecordData(rentalRecord, conn, returnDate, calculateEstimatedRentalFee(daysAtRental - lateDays), calculateLateFee(lateDays));
        setVehicleAvaliable(rentalRecord, conn);
    }
}
