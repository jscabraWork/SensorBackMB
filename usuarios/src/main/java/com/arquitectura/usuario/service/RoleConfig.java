package com.arquitectura.usuario.service;
/**
 * Record de configuración para asociar roles con topics y clases de Kafka
 */
public record RoleConfig(String topic, String clase) {
    // El record automáticamente genera:
    // - Constructor: RoleConfig(String topic, String clase)
    // - Getters: topic() y clase()
    // - equals(), hashCode(), toString()
    // - Los campos son automáticamente final
}
