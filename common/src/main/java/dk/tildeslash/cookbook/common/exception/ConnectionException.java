package dk.tildeslash.cookbook.common.exception;

/**
 * Created with IntelliJ IDEA.
 * User: morten
 * Date: 29/07/13
 * Time: 09:45
 * To change this template use File | Settings | File Templates.
 */
public class ConnectionException extends Exception{

    private final String message;

    public ConnectionException(String message){
        this.message = message;
    }

    public ConnectionException(String message, Throwable cause){
        initCause(cause);
        this.message = message;
    }

    @Override
    public String getMessage(){
        return message;
    }
}
