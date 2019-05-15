/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

public class RentException extends Exception  {
     public RentException (String message) 
    { 
         super(message); 
        ExceptionHandler.ShowExceptionAlert(message);
    }    
}
