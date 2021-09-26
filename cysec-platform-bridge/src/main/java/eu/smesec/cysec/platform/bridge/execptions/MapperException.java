package eu.smesec.cysec.platform.bridge.execptions;

public class MapperException extends Exception {

    public MapperException(final String message) {
        super(message);
    }

    public MapperException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
