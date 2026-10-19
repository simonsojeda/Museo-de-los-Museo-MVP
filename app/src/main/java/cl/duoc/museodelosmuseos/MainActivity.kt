package cl.duoc.museodelosmuseos

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import cl.duoc.museodelosmuseos.data.MuseoViewModel
import cl.duoc.museodelosmuseos.ui.MuseoApp
import cl.duoc.museodelosmuseos.ui.MuseoTheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    private val vm: MuseoViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = "MuseoDeLosMuseos-Demo/1.0 (academic)"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel("museo_demo", "Avisos del museo", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        setContent {
            MuseoTheme {
                MuseoApp(vm, onGetLocation = { getDemoLocation() }, onNotify = { showDemoNotification() })
            }
        }
    }
    private fun getDemoLocation(): String {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return "Se requiere permiso de ubicación"
        }
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val pos = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .firstNotNullOfOrNull { provider ->
                try { if (manager.isProviderEnabled(provider)) manager.getLastKnownLocation(provider) else null }
                catch (_: Exception) { null }
            }
        return if (pos == null) "Ubicación aún no disponible; activa GPS y vuelve a intentar"
        else "Mi ubicación aprox.: %.3f, %.3f".format(pos.latitude, pos.longitude)
    }
    private fun showDemoNotification(): String {
        if (Build.VERSION.SDK_INT >= 33 && ActivityCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return "Autoriza notificaciones para recibir el aviso"
        }
        val notification = NotificationCompat.Builder(this,"museo_demo")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Museo de los Museos · Demo")
            .setContentText("Aviso local de prueba generado correctamente")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT).setAutoCancel(true).build()
        NotificationManagerCompat.from(this).notify(26, notification)
        return "Notificación local enviada"
    }
}
