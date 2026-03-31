package lk.ijse.aad.backend.service.custom;

import lk.ijse.aad.backend.dto.PayHereInitDTO;
import lk.ijse.aad.backend.dto.PayHereNotifyDTO;

public interface PayHereService {

    PayHereInitDTO initiatePayment(Long bookingId, String username);


    void handleNotify(PayHereNotifyDTO notify);
}
