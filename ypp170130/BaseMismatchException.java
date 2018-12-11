package ypp170130;

/**
 *     @author Pranita Hatte
 *     @author Prit Thakkar
 *     @author Shivani Thakkar
 *     @author Yash Pradhan
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
