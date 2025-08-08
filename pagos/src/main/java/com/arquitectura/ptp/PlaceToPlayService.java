package com.arquitectura.ptp;

import com.arquitectura.alcancia.service.AlcanciaService;
import com.arquitectura.cliente.entity.Cliente;
import com.arquitectura.configSeguro.entity.ConfigSeguro;
import com.arquitectura.configSeguro.service.ConfigSeguroService;
import com.arquitectura.orden.entity.Orden;
import com.arquitectura.orden.service.OrdenService;
import com.arquitectura.orden_alcancia.service.OrdenAlcanciaService;
import com.arquitectura.ticket.service.TicketService;
import com.arquitectura.transaccion.entity.Transaccion;
import com.arquitectura.transaccion.service.TransaccionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PlaceToPlayService {

    @Value("${ptp.login}")
    private String login;

    @Value("${ptp.secret-key}")
    private String secretKey;

    @Value("${ptp.url}")
    private String urlPTP;

    @Autowired
    private OrdenService ordenService;

    @Autowired
    private ConfigSeguroService configSeguroService;

    @Autowired
    private AlcanciaService alcanciaService;

    @Autowired
    private PtpAdapter adapter;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TransaccionService transaccionService;

    @Autowired
    private OrdenAlcanciaService ordenAlcanciaService;

    public AuthEntity generarAuth() throws NoSuchAlgorithmException{
        LocalDateTime date = LocalDateTime.now();
        ZonedDateTime zonedDateTime = date.atZone(ZoneOffset.ofHours(-5));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
        String formattedDate = zonedDateTime.format(formatter);

        String seed = String.format(formattedDate);
        String rawNonce = UUID.randomUUID().toString();
        String nonce =Base64.getEncoder().encodeToString(rawNonce.getBytes()) ;

        String tranKeyCompose = rawNonce + seed +secretKey;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashTranKey = digest.digest(tranKeyCompose.getBytes(StandardCharsets.UTF_8));
        String tranKey= Base64.getEncoder().encodeToString(hashTranKey);

        AuthEntity auth = new AuthEntity(login,tranKey,nonce,seed);
        return auth;
    }

    public Object makePostRequest(String url, Map<String, Object> requestBody,  Object object) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(requestBody);
        HttpEntity<String> request = new HttpEntity<>(json, headers);
        ResponseEntity<?> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                object.getClass()
        );
        return response.getBody();
    }



    public boolean validarSignature(String texto, String signature) throws NoSuchAlgorithmException {
        texto += secretKey;

        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] inputBytes = texto.getBytes(StandardCharsets.UTF_8);
        byte[] hashBytes = md.digest(inputBytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        String string= sb.toString();

        System.out.println(" el signature es: " + string);

        boolean	validado=string.equals(signature);
        return validado;
    }

    /**
     * Crea un enlace de pago para Place to Pay
     */
    public ProcessUrlResponse crearEnlacePago(Long ordenId, String returnUrl, boolean seguro, Double aporteAlcancia) throws Exception {

        //----------PROCESAR LA ORDEN Y EL SEGURO------------------
        //Validar orden
        Orden orden = ordenService.findById(ordenId);
        if (orden == null || orden.getEstado() != 3) {
            throw new IllegalArgumentException("Orden no encontrada o no está en estado pendiente");
        }
        
        // Procesar configuración de pago según el tipo de orden
        //Si no tiene seguro sigue con el valorOrden previamente asignado
        if (orden.getTipo() == 1) {
            orden.setValorSeguro(0.0);
            if(seguro){
                // Orden de tickets con seguro
                ConfigSeguro configSeguro = configSeguroService.getConfigSeguroActivo();
                Double valorSeguro = configSeguro.calcularValorSeguro(orden.getValorOrden());
                orden.setValorSeguro(valorSeguro);
            }
            // Calcular el valor total de la orden incluyendo el seguro
            orden.setValorOrden(orden.calcularValorOrden());

        //Si es una orden de alcancia
        } else if (orden.getTipo() == 4 || orden.getTipo() == 3) {
            // Orden de alcancía
            var localidadOrden = orden.getTarifa().getLocalidad();
            if (aporteAlcancia < localidadOrden.getAporteMinimo()) {
                throw new IllegalArgumentException("El valor no puede ser menor al valor mínimo para abrir una alcancía");
            }
            orden.setValorSeguro(0.0); //Para ordenes alcancia siempre valor cero
            orden.setValorOrden(aporteAlcancia); //El valor orden es el monto del aporte
        }

        //----------CONSTRUIR REQUEST PARA PTP------------------
        
        Map<String, Object> requestData = construirRequestPTP(orden, returnUrl);
        
        ProcessUrlResponse response = (ProcessUrlResponse) makePostRequest(
            urlPTP + "/api/session", 
            requestData, 
            new ProcessUrlResponse()
        );
        
        orden.setIdTRXPasarela(response.getRequestId());

        //Guardar la orden en la base de datos
        ordenService.saveKafka(orden);
        
        return response;
    }

    private Map<String, Object> construirRequestPTP(Orden orden, String returnUrl) throws NoSuchAlgorithmException {

        Map<String, Object> requestData = new HashMap<>();

        Cliente cliente = orden.getCliente();

        // Configurar autenticación
        requestData.put("auth", generarAuth());
        
        // Configurar impuestos y monto
        List<Tax> taxes = Arrays.asList(
            new Tax("valueAddedTax", orden.calcularIva(), orden.getValorOrden() - orden.calcularIva())
        );
        Amount amount = new Amount("COP", orden.getValorOrden(), taxes);
        Payment payment = new Payment(orden.getId().toString(), orden.getDescripcion(), amount);
        
        // Configurar expiración (30 minutos)
        LocalDateTime newDate = LocalDateTime.now().plusMinutes(30);
        ZonedDateTime zonedDateTime = newDate.atZone(ZoneOffset.ofHours(-5));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
        String expiration = zonedDateTime.format(formatter);

        // Configurar comprador con los datos del cliente
        Person buyer = new Person(
            cliente.getNumeroDocumento(),
            cliente.getTipoDocumentoPtp(),
            cliente.getNombre(),
            cliente.getCorreo(),
            cliente.getCelular()
        );
        
        // Construir request completo
        requestData.put("expiration", expiration);
        requestData.put("returnUrl", returnUrl);
        requestData.put("ipAddress", "127.0.0.1");
        requestData.put("userAgent", "PlacetoPay Sandbox");
        requestData.put("payment", payment);
        requestData.put("locale", "es_CO");
        requestData.put("buyer", buyer);
        requestData.put("skipResult", true);
        
        return requestData;
    }

    /**
     * Procesa la notificación recibida de Place to Pay
     */
    public ResponseEntity<?> procesarNotificacionPTP(SessionNotification data) throws Exception {
        
        Status status = data.getStatus();
        String shaCompose = data.getRequestId() + status.getStatus() + status.getDate();
        
        if (!validarSignature(shaCompose, data.getSignature())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        RequestResponse response = consultarEstadoTransaccion(data.getRequestId());

        Long reference = Long.parseLong(data.getReference());

        Orden orden = ordenService.findById(reference);
        
        Transaccion transaccion = adapter.crearTransaccion(response);
        transaccion.setOrden(orden);

        if (transaccionService.getTransaccionRepetida(transaccion.getStatus(), orden.getId()) != null) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        Transaccion transaccionBD = transaccionService.saveKafka(transaccion);
        
        if (transaccionBD.getStatus() == 34) {
            procesarTransaccionExitosa(orden, transaccion);
        }
        
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private RequestResponse consultarEstadoTransaccion(Long requestId) throws Exception {

        Map<String, Object> info = new HashMap<>();

        info.put("auth", generarAuth());
        
        String urlPeticion = urlPTP + "/api/session/" + requestId;
        return (RequestResponse) makePostRequest(urlPeticion, info, new RequestResponse());
    }

    private void procesarTransaccionExitosa(Orden orden, Transaccion transaccion) throws Exception {

        switch (orden.getTipo()) {

            case 1:
                ordenService.confirmar(orden); //Compra estandar de tickets
                break;

            case 2: break;    //Compra de Adicionales

            case 3:
                ordenAlcanciaService.confirmarCreacion(orden, transaccion.getAmount()); //Apertura de alcancía
                break;
            case 4:
                ordenAlcanciaService.confirmarAporte(orden, transaccion.getAmount()); //Aporte a alcancía
                break;
            // TIPO 5 (TRASPASO), 6 (ASIGNACIÓN) No se manejan con PTP
        }
    }
}
