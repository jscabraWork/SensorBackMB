package com.arquitectura.ptp;

import com.arquitectura.transaccion.entity.Transaccion;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class
PtpAdapterImpl implements PtpAdapter {

    @Override
    public Transaccion crearTransaccion(RequestResponse request) {
        Transaccion transaccion = new Transaccion();
        Request requestDetail=request.getRequest();

        if (requestDetail.getPayment() != null && requestDetail.getPayment().getAmount() != null) {
            transaccion.setAmount(requestDetail.getPayment().getAmount().getTotal());
        } else {
            transaccion.setAmount(0.0);
        }
        if(requestDetail.getPayer()!=null) {
            Payer payer =requestDetail.getPayer();
            transaccion.setEmail(payer.getEmail());
            transaccion.setIdPersona(payer.getDocumentType()+ payer.getDocument());
            transaccion.setPhone(payer.getMobile());
        }
        List<PaymentResponse> payments = request.getPayment();
        if(payments !=null) {

            PaymentResponse primero = payments.get(0);

            transaccion.setMetodoNombre(primero.getPaymentMethod().toUpperCase() +" "+primero.getIssuerName() );

            String status = request.getStatus().getStatus();

            if(status.equals("APPROVED")) {
                transaccion.setStatus(34);
            }
            else if(status.equals("PENDING")) {
                transaccion.setStatus(35);
            }
            else if(status.equals("REJECTED")) {
                transaccion.setStatus(36);
            }
            else if(status.equals("APPROVED_PARTIAL")) {
                transaccion.setStatus(50);
            }
            else {
                transaccion.setStatus(36);
            }

            String metodoNombre =primero.getPaymentMethod();

            if(metodoNombre.equals("visa") ||
                    metodoNombre.equals("master")||
                    metodoNombre.equals("diners")||
                    metodoNombre.equals("amex") ||
                    metodoNombre.equals("visa_electron")) {

                transaccion.setMetodo(1);
            }
            else if(metodoNombre.equals("pse") ||
                    metodoNombre.equals("bancolombia")) {

                transaccion.setMetodo(2);
            }
            else if(metodoNombre.equals("ath") ) {

                transaccion.setMetodo(4);
            }

        }
        else {
            transaccion.setMetodo(-1);
            transaccion.setMetodoNombre("Sin definir");

            String status = request.getStatus().getStatus();
            if(status.equals("APPROVED")) {
                transaccion.setStatus(34);
            }
            else if(status.equals("PENDING")) {
                transaccion.setStatus(35);
            }
            else if(status.equals("REJECTED")) {
                transaccion.setStatus(36);
            }
            else if(status.equals("APPROVED_PARTIAL")) {
                transaccion.setStatus(50);
            }
        }


        String idBase = request.getRequestId().toString();
        transaccion.setIdBasePasarela(idBase);
        transaccion.setIp(requestDetail.getIpAddress());
        transaccion.setIdPasarela(requestDetail.getUserAgent());


        return transaccion;
    }

}
