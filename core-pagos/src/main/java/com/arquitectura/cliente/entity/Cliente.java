package com.arquitectura.cliente.entity;

import com.arquitectura.ptp.Person;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "clientes")
@JsonIgnoreProperties(value={"handler","hibernateLazyInitializer"})
public class Cliente {

    @Id
    private String numeroDocumento;
    private String nombre;
    private String correo;
    private String celular;
    private String tipoDocumento;

    public String getTipoDocumentoPtp() {

        if(tipoDocumento==null){
            return "CC";
        }

        switch (tipoDocumento) {
            case "Cedula":
            case "Cédula":
                return "CC";

            case "NIT": return "NIT";

            case "Pasaporte": return "PPN";

            case "Cedula de Extranjeria":
            case "Cedula de Extranjería":
                return "CE";

            case "Tarjeta de identidad": return "TI";

            default: return "CC";
        }
    }


}
