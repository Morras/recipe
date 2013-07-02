package dk.tildeslash.cookbook.common.exception;

public class NotConfiguredException extends Exception {

	/**
	 * serialVersionUID as recommended by the java documentation.
	 */
	private static final long serialVersionUID = 0;

    private String message = "Error while configuring the editor from the configuration file.";

    public NotConfiguredException(){

    }

    public NotConfiguredException(String message){
        this.message = message;
    }

    @Override
    public String getMessage(){
        return message;
    }

}
