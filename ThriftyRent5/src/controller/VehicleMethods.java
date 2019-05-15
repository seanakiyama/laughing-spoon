package controller;

public interface VehicleMethods {

    void rent(String customerID, DateTime rentDate, int numOfRentDay)  throws RentException;

    void returnVehicle(DateTime returnDate,RentalRecord rentalRecord);

    void performMaintenance();

    void completeMaintenance(DateTime completionDate);

    String toString();

}
