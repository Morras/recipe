package dk.tildeslash.cookbook.common.exception;

public class DataStoreException extends Exception{

    private final String message;

    public DataStoreException(String message){
        this.message = message;
    }

    @Override
    public String getMessage(){
        return message;
    }
}
