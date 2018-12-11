package ypp170130;

/**
 *     Team No: 39
 *     @author Pranita Hatte: prh170230
 *     @author Prit Thakkar: pvt170000
 *     @author Shivani Thakkar: sdt170030
 *     @author Yash Pradhan: ypp170130
 *     Long Project 1: Num - To Perform Integer arithmetic with arbitrarily large numbers
 *
 * 	Custom exception class BaseMismatchException,
 * 	for methods like add, subtract, product, divide etc.
 * 	for handling cases where the base of the parameters
 * 	do not match.
 *
 */

public class BaseMismatchException extends RuntimeException {
    public BaseMismatchException(String message){
        super(message);
    }
}
