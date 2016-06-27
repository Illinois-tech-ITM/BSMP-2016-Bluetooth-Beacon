package edu.iit.bluetoothbeacon;

import edu.iit.bluetoothbeacon.models.Masterpiece;

/**
 * Created by anderson on 6/25/16.
 */

public interface OnResponseReceivedListener {
    // error is false if the response was successful
    void OnResponseReceived(Masterpiece mp, boolean error);
}
