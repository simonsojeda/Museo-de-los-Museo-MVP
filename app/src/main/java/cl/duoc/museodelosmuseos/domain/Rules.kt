package cl.duoc.museodelosmuseos.domain

import java.util.Locale

/** Validaciones independientes de Compose, aptas para pruebas unitarias. */
object Validators {
    fun name(value: String): String? = when {
        value.trim().length < 3 -> "Ingresa al menos 3 caracteres"
        else -> null
    }
    fun email(value: String): String? = when {
        !Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(value.trim()) -> "Ingresa un correo válido"
        else -> null
    }
    fun password(value: String): String? = when {
        value.length < 8 -> "La contraseña requiere 8 caracteres o más"
        else -> null
    }
    fun positiveInt(value: String): String? = when {
        value.toIntOrNull() == null || value.toInt() < 0 -> "Ingresa un número mayor o igual a 0"
        else -> null
    }
}

data class ShippingQuote(val shipping: Int, val total: Int, val description: String)

/** Reglas 100% FICTICIAS para fines académicos; pesos en kg, montos en CLP. */
object ShippingCalculator {
    fun calculate(subtotal: Int, totalKg: Double, zone: String, express: Boolean): ShippingQuote {
        require(subtotal >= 0 && totalKg >= 0.0)
        val base = when (zone) {
            "Chile - Metropolitana" -> 3500
            "Chile - Regiones" -> 6500
            "Internacional - América" -> 18000
            "Internacional - Europa" -> 28000
            else -> throw IllegalArgumentException("Destino no admitido")
        }
        val rate = if (zone.startsWith("Internacional")) 4400 else 1200
        val normal = base + kotlin.math.ceil(totalKg * rate).toInt()
        val shipping = if (express) kotlin.math.ceil(normal * 1.6).toInt() else normal
        return ShippingQuote(shipping, subtotal + shipping, "Tarifa simulada; no constituye cotización real")
    }
}
fun money(value: Int): String = "$" + String.format(Locale("es", "CL"), "%,d", value)
