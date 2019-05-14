package nk.mobileapps.otpreceiver.utils;

/**
 * Created by nagendra on 21/3/17.
 */

public interface ResponseListener {
    String GET = "GET";
    String POST = "POST";

    /**
     * called after got response from web service
     *
     * @param responseCode request code that you sent in constructor
     * @param response     response from web service
     */
    public void onSuccess(int responseCode, String response)
    ;

    /**
     * called on time out or network issues or any other exception
     *
     * @param responseCode request code that you sent in constructor
     * @param error        error message
     */
    public void onError(int responseCode, String error);
}
