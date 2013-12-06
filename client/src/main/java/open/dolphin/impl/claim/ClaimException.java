package open.dolphin.impl.claim;

import open.dolphin.client.ClaimMessageEvent;

/**
 * ClaimException
 * @author masuda, Masuda Naika
 */
public class ClaimException extends Exception {

    public static enum ERROR_CODE {

        NO_ERROR, CONNECTION_REJECT, IO_ERROR, NAK_SIGNAL
    };
    
    private final ERROR_CODE code;
    private final ClaimMessageEvent evt;

    public ClaimException(ERROR_CODE code, ClaimMessageEvent evt) {
        this.code = code;
        this.evt = evt;
    }

    public ERROR_CODE getErrorCode() {
        return code;
    }

    public ClaimMessageEvent getClaimEvent() {
        return evt;
    }
}
