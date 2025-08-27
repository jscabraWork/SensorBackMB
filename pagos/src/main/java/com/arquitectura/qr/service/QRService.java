package com.arquitectura.qr.service;

import com.arquitectura.ticket.entity.Ticket;
import com.google.zxing.WriterException;

import java.io.IOException;

public interface QRService {

    /**
     * Envia un codigo QR con la informacion del ticket.
     *
     * @param pTicket  el ticket comprado
     */
    public void mandarQR(Ticket pTicket) throws WriterException, IOException, Exception;


}
