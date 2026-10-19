package cl.duoc.museodelosmuseos.domain

import org.junit.Assert.*
import org.junit.Test

class RulesTest {
    @Test fun `valida correo y contrasena`() {
        assertNull(Validators.email("alumno@demo.cl"))
        assertNotNull(Validators.email("sin-arroba"))
        assertNull(Validators.password("museo123"))
        assertNotNull(Validators.password("123"))
    }
    @Test fun `calculo de tarifa ficticia`() {
        val quote = ShippingCalculator.calculate(20000, 1.0, "Chile - Metropolitana", false)
        assertEquals(4700, quote.shipping)
        assertEquals(24700, quote.total)
    }
    @Test fun `express tiene mayor costo`() {
        val normal = ShippingCalculator.calculate(10000, 1.0, "Internacional - América", false)
        val express = ShippingCalculator.calculate(10000, 1.0, "Internacional - América", true)
        assertTrue(express.shipping > normal.shipping)
    }
}
