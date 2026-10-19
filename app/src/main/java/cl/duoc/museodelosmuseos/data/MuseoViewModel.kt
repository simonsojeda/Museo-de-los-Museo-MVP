package cl.duoc.museodelosmuseos.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.museodelosmuseos.BuildConfig
import cl.duoc.museodelosmuseos.domain.Validators
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.security.MessageDigest

class MuseoViewModel(application: Application): AndroidViewModel(application) {
    private val db = MuseoDatabase.get(application)
    private val dao = db.dao()
    private val preferences = application.getSharedPreferences("museo_sesion", 0)
    val currentId = MutableStateFlow(preferences.getInt("user_id", -1))
    val users = dao.users().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val museums = dao.museums().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val products = dao.products().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val favorites = dao.favorites().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val cart = dao.cart().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val requests = dao.requests().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val scans = dao.scans().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val preferencesList = dao.preferences().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val notices = dao.notices().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val person = combine(currentId, users) { id, list -> list.firstOrNull { it.id == id } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val message = MutableSharedFlow<String>(extraBufferCapacity = 10)
    init {
        viewModelScope.launch {
            if (dao.userCount() == 0) {
                dao.addUser(UserEntity(displayName="Admin Demo", email="admin@demo.cl", passwordHash=hash("museo123"), role="ADMIN"))
                dao.addUser(UserEntity(displayName="Investigador Demo", email="investigador@demo.cl", passwordHash=hash("museo123"), role="INVESTIGADOR"))
                dao.addUser(UserEntity(displayName="Cliente Demo", email="cliente@demo.cl", passwordHash=hash("museo123"), role="CLIENTE"))
                dao.saveMuseums(Fixtures.museums)
                dao.saveProducts(Fixtures.products)
                dao.addNotice(NoticeEntity(title="Bienvenido", message="Explora el catálogo y el mapa de museos ficticios."))
            }
        }
    }
    // Contraseñas SOLO ficticias. En una aplicación real, autenticar en un servidor seguro.
    private fun hash(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(("laboratorio_museos_2026:" + value).toByteArray())
        .joinToString("") { "%02x".format(it) }
    private fun setUser(id: Int) { currentId.value = id; preferences.edit().putInt("user_id", id).apply() }
    fun signOut() { currentId.value = -1; preferences.edit().remove("user_id").apply() }
    fun login(email: String, password: String, role: String, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        val user = dao.userByEmail(email.trim().lowercase())
        val ok = user != null && user.passwordHash == hash(password) && user.role == role
        if (ok) setUser(user!!.id)
        else message.emit("Credenciales o rol incorrectos")
        onResult(ok)
    }
    fun register(name: String, email: String, password: String, avatar: Int, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        if (Validators.name(name) != null || Validators.email(email) != null || Validators.password(password) != null) {
            message.emit("Revisa los campos del formulario"); onResult(false); return@launch
        }
        val result = dao.addUser(UserEntity(displayName=name.trim(), email=email.trim().lowercase(), passwordHash=hash(password), avatar=avatar))
        if (result == -1L) { message.emit("El correo ya está registrado"); onResult(false) }
        else { setUser(result.toInt()); onResult(true) }
    }
    fun updatePreferences(value: PreferenceEntity) = viewModelScope.launch {
        if (value.userId == currentId.value) dao.savePreferences(value)
    }
    fun renameProfile(name: String) = viewModelScope.launch {
        if (Validators.name(name) == null && currentId.value > 0) {
            dao.renameUser(currentId.value, name.trim())
            message.emit("Perfil actualizado")
        } else message.emit("Nombre de al menos 3 caracteres")
    }
    fun toggleFavorite(id: Int) = viewModelScope.launch {
        val u = currentId.value
        if (u < 0) return@launch
        if (favorites.value.any { it.userId == u && it.museumId == id }) dao.removeFavorite(u,id)
        else dao.addFavorite(FavoriteEntity(u,id))
    }
    fun addCart(product: ProductEntity, quantity: Int = 1) = viewModelScope.launch {
        val u = currentId.value
        if (u < 0) return@launch
        val original = cart.value.firstOrNull { it.userId == u && it.productId == product.id }?.quantity ?: 0
        if (product.category == "Cursos") {
            dao.addRequest(RequestEntity(userId=u,title="Inscripción: ${product.title}",category="Curso",amount=product.price))
            dao.addNotice(NoticeEntity(targetUserId=u,title="Solicitud registrada",message="Tu solicitud de curso está Pendiente.",route="requests"))
            message.emit("Inscripción de demostración registrada")
        } else if (original+quantity <= product.stock && quantity > 0) {
            dao.saveCart(CartEntity(u,product.id,original+quantity)); message.emit("Agregado al carrito")
        } else message.emit("Cantidad no disponible")
    }
    fun changeQuantity(productId: Int, requested: Int, available: Int) = viewModelScope.launch {
        val u = currentId.value
        if (u < 0) return@launch
        if (requested <= 0) dao.removeCart(u,productId)
        else if (requested <= available) dao.saveCart(CartEntity(u,productId,requested))
        else message.emit("Stock insuficiente")
    }
    fun checkout(amount: Int, title: String) = viewModelScope.launch {
        val u = currentId.value
        if (u < 0) return@launch
        if (cart.value.none { it.userId == u }) { message.emit("Carrito vacío"); return@launch }
        // Verificación de stock al confirmar, en laboratorio solo lectura local.
        val invalid = cart.value.filter { it.userId == u }.any { line ->
            val item = products.value.firstOrNull { it.id == line.productId }
            item == null || !item.active || line.quantity > item.stock
        }
        if (invalid) { message.emit("Hay productos sin stock suficiente"); return@launch }
        dao.addRequest(RequestEntity(userId=u,title=title,category="Producto",amount=amount))
        dao.clearCart(u)
        dao.addNotice(NoticeEntity(targetUserId=u,title="Solicitud recibida",message="Pedido registrado como Pendiente.",route="requests"))
        message.emit("Solicitud simulada guardada")
    }
    fun setRequestStatus(id: Int, status: String) = viewModelScope.launch {
        if (person.value?.role != "ADMIN") return@launch
        dao.updateRequest(id,status)
        val request = requests.value.firstOrNull { it.id == id }
        if (request != null) dao.addNotice(NoticeEntity(targetUserId=request.userId,title="Estado actualizado",message="Solicitud #$id: $status",route="requests"))
        message.emit("Estado actualizado a $status")
    }
    fun saveMuseum(m: MuseumEntity) = viewModelScope.launch {
        if (person.value?.role !in setOf("ADMIN","INVESTIGADOR")) return@launch
        dao.saveMuseum(m); message.emit("Ficha de demostración guardada")
    }
    fun saveProduct(p: ProductEntity) = viewModelScope.launch {
        if (person.value?.role != "ADMIN") return@launch
        dao.saveProduct(p); message.emit("Producto actualizado")
    }
    fun publishNotice(title: String, content: String) = viewModelScope.launch {
        if (person.value?.role != "ADMIN") return@launch
        if (title.isBlank() || content.isBlank()) { message.emit("Completa el título y el mensaje"); return@launch }
        dao.addNotice(NoticeEntity(title=title.trim(),message=content.trim()))
        message.emit("Aviso de demostración publicado")
    }
    fun registerScan(text: String) = viewModelScope.launch {
        val u = currentId.value
        if (u < 0) return@launch
        val label = when {
            text.startsWith("MUSEO:") -> museums.value.firstOrNull { it.id.toString()==text.substringAfter(':') }?.name
            text.startsWith("PRODUCTO:") -> products.value.firstOrNull { it.id.toString()==text.substringAfter(':') }?.title
            text.startsWith("CURSO:") -> products.value.firstOrNull { it.id.toString()==text.substringAfter(':') && it.category=="Cursos" }?.title
            else -> null
        }
        if (label == null) { message.emit("Código no reconocido: usa MUSEO:1 o PRODUCTO:1"); return@launch }
        dao.addScan(ScanEntity(userId=u,code=text,result=label))
        message.emit("Código identificado: $label")
    }
    fun syncWithApi() = viewModelScope.launch {
        try {
            // El backend de muestra solo entrega datos públicos, nunca usuarios ni solicitudes.
            dao.saveMuseums(ApiProvider.api.museums())
            dao.saveProducts(ApiProvider.api.catalog())
            message.emit("Catálogo y mapa sincronizados desde API REST")
        } catch (e: Exception) { message.emit("API no disponible: se conserva el catálogo local") }
    }
}
