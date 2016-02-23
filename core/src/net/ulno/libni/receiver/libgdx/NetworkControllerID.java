package net.ulno.libni.receiver.libgdx;

/**
 * Created by ulno on 22.02.16.
 */
public class NetworkControllerID {
    long sessionID; // random session ID
    long clientID; // self selected client id (by client)

    public NetworkControllerID(long sessionID, long clientID) {
        this.sessionID = sessionID;
        this.clientID = clientID;
    }

    public NetworkControllerID() {
        // empty
    }

    @Override
    public boolean equals(Object obj) {
        if(obj != null && obj instanceof NetworkControllerID) {
            return ((NetworkControllerID) obj).sessionID == sessionID && ((NetworkControllerID) obj).clientID == clientID;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(sessionID+clientID);
    }
}
