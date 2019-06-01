package ly.rqmana.huia.java.fingerprints.activity;

public class FingerprintException extends RuntimeException {

    private final String message;

    public FingerprintException(){
        this("");
    }

    public FingerprintException(String message){
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
