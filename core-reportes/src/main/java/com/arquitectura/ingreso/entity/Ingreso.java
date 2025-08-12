    package com.arquitectura.ingreso.entity;


    import com.arquitectura.dia.entity.Dia;
    import com.arquitectura.entity.Auditable;
    import com.arquitectura.ticket.entity.Ticket;
    import com.fasterxml.jackson.annotation.JsonBackReference;
    import com.fasterxml.jackson.annotation.JsonIgnore;
    import jakarta.persistence.*;
    import lombok.*;

    import java.time.LocalDateTime;

    @Entity
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Table(name = "ingresos")
    public class Ingreso {

        @Id
        private Long id;

        private LocalDateTime fechaIngreso;

        private boolean utilizado;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "dia_id")
        private Dia dia;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "ticket_id")
        @JsonBackReference(value = "ticket-ingreso")
        private Ticket ticket;
    }
