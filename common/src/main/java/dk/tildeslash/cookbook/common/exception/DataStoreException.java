package dk.tildeslash.cookbook.common.exception;

public class DataStoreException extends Exception{

    private final String message;

    public DataStoreException(String message){
        this.message = message;
    }

    public DataStoreException(String message, Throwable cause){
        this.message = message;
        initCause(cause);
    }

    @Override
    public String getMessage(){
        return message;
    }
}
