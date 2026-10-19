package cl.duoc.museodelosmuseos.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Navy = Color(0xFF09344B)
val DeepNavy = Color(0xFF052434)
val Gold = Color(0xFFBD9859)
val PaleGold = Color(0xFFFFF0D2)
val Snow = Color(0xFFF5F7F8)
val Teal = Color(0xFF087D82)
val Ink = Color(0xFF163042)
val CardGray = Color(0xFFE7EEF1)

private val colors = lightColorScheme(
    primary = Navy, onPrimary = Color.White, secondary = Gold,
    onSecondary = DeepNavy, background = Snow, onBackground = Ink,
    surface = Color.White, onSurface = Ink, surfaceVariant = CardGray,
    onSurfaceVariant = Ink, error = Color(0xFFB32828)
)
@Composable fun MuseoTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = Typography(), content = content)
}
