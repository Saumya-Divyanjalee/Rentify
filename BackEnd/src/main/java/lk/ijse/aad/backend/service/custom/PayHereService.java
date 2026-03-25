package lk.ijse.aad.backend.service.custom;

import lk.ijse.aad.backend.dto.PayHereInitDTO;
import lk.ijse.aad.backend.dto.PayHereNotifyDTO;

public interface PayHereService {
    /**
     * Initialises a PayHere payment for an existing booking.
     * Creates a PENDING payment record and returns everything the frontend
     * needs to redirect the user to PayHere checkout.
     *
     * @param bookingId booking to pay for
     * @param username  JWT username of the logged-in user
     */
    PayHereInitDTO initiatePayment(Long bookingId, String username);

    /**
     * Processes the asynchronous notify POST from PayHere.
     * Validates the MD5 hash, then updates Payment + Booking status.
     * Returns "OK" on success, throws on failure.
     *
     * @param notify payload from PayHere
     */
    void handleNotify(PayHereNotifyDTO notify);
}
