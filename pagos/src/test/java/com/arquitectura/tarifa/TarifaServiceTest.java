package com.arquitectura.tarifa;

import com.arquitectura.PagosApplication;
import com.arquitectura.tarifa.entity.TarifaRepository;
import com.arquitectura.tarifa.service.TarifaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = PagosApplication.class)
public class TarifaServiceTest {

    @MockBean
    private TarifaRepository tarifaRepository;

    @Autowired
    private TarifaService tarifaService;

    @Test
    @DisplayName("tieneTicketsAsociados - Debe retornar true si hay tickets asociados a la tarifa")
    void testTieneTicketsAsociados_True() {
        Long tarifaId = 1L;
        when(tarifaRepository.existsTicketsByTarifaId(tarifaId)).thenReturn(true);

        boolean resultado = tarifaService.tieneTicketsAsociados(tarifaId);

        assertTrue(resultado);
        verify(tarifaRepository, times(1)).existsTicketsByTarifaId(tarifaId);
    }

    @Test
    @DisplayName("tieneTicketsAsociados - Debe retornar false si no hay tickets asociados a la tarifa")
    void testTieneTicketsAsociados_False() {
        Long tarifaId = 2L;
        when(tarifaRepository.existsTicketsByTarifaId(tarifaId)).thenReturn(false);

        boolean resultado = tarifaService.tieneTicketsAsociados(tarifaId);

        assertFalse(resultado);
        verify(tarifaRepository, times(1)).existsTicketsByTarifaId(tarifaId);
    }

}
