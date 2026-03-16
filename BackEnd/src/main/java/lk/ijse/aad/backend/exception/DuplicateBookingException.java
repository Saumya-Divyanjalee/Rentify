package lk.ijse.aad.backend.exception;

public class DuplicateBookingException extends RuntimeException{
    public DuplicateBookingException(String message){
        super(message);
    }
}
