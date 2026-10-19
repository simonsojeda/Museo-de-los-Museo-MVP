package cl.duoc.museodelosmuseos.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.museodelosmuseos.R
import cl.duoc.museodelosmuseos.data.*
import cl.duoc.museodelosmuseos.domain.*
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.compose.ui.viewinterop.AndroidView

private val tabs = listOf("dashboard", "map", "catalog", "notifications", "profile")
private val destinations = listOf("Inicio", "Mapa", "Catálogo", "Avisos", "Perfil")
private val tabIcons = listOf(Icons.Default.Home, Icons.Default.Place, Icons.Default.Store, Icons.Default.Notifications, Icons.Default.Person)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuseoApp(vm: MuseoViewModel, onGetLocation: () -> String, onNotify: () -> String) {
    val current by vm.person.collectAsStateWithLifecycle()
    val museums by vm.museums.collectAsStateWithLifecycle()
    val products by vm.products.collectAsStateWithLifecycle()
    val favorites by vm.favorites.collectAsStateWithLifecycle()
    val cart by vm.cart.collectAsStateWithLifecycle()
    val requests by vm.requests.collectAsStateWithLifecycle()
    val notices by vm.notices.collectAsStateWithLifecycle()
    val prefs by vm.preferencesList.collectAsStateWithLifecycle()
    val scans by vm.scans.collectAsStateWithLifecycle()
    var route by rememberSaveable { mutableStateOf("welcome") }
    var museumId by rememberSaveable { mutableIntStateOf(1) }
    var productId by rememberSaveable { mutableIntStateOf(1) }
    var resultCode by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val user = current
    val signedIn = user != null
    LaunchedEffect(user?.id) { if (user != null && route in setOf("welcome", "login", "register")) route = "dashboard" }
    LaunchedEffect(vm) { vm.message.collect { snackbar.showSnackbar(it) } }
    fun navigate(next: String) { route = next }
    BackHandler(enabled = route !in setOf("welcome", "dashboard", "login")) { route = if (signedIn) "dashboard" else "welcome" }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { code -> resultCode = code; vm.registerScan(code); route = "qrResult" }
    }
    val locationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { allowed ->
        scope.launch { snackbar.showSnackbar(if (allowed) onGetLocation() else "Permiso de ubicación denegado") }
    }
    val notificationsPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { allowed ->
        scope.launch { snackbar.showSnackbar(if (allowed) onNotify() else "Permiso de notificaciones denegado") }
    }
    val requestLocation: () -> Unit = {
        locationPermission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }
    val requestNotice: () -> Unit = {
        if (Build.VERSION.SDK_INT >= 33) notificationsPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        else { scope.launch { snackbar.showSnackbar(onNotify()) } }
    }
    val goMuseum: (Int) -> Unit = { museumId = it; route = "museumDetail" }
    val goProduct: (Int) -> Unit = { productId = it; route = "productDetail" }

    Scaffold(
        containerColor = Snow,
        topBar = {
            if (signedIn && route != "welcome") {
                CenterAlignedTopAppBar(
                    title = { Text(if (route in tabs) "Museo de los Museos" else titleFor(route), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                    navigationIcon = { if (route !in tabs) IconButton(onClick = { navigate("dashboard") }) { Icon(Icons.Default.ArrowBack, "Volver") } },
                    actions = { Text("DEMO", color = Gold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(end = 14.dp)) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                )
            }
        },
        bottomBar = {
            if (signedIn && route !in setOf("welcome", "login", "register")) {
                NavigationBar(containerColor = Color.White, tonalElevation = 2.dp) {
                    tabs.forEachIndexed { index, item ->
                        NavigationBarItem(selected = route == item,
                            onClick = { navigate(item) },
                            icon = { Icon(tabIcons[index], destinations[index]) },
                            label = { Text(destinations[index]) })
                    }
                }
            }
        }, snackbarHost = { SnackbarHost(snackbar) }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (route) {
                "welcome" -> WelcomeScreen(onStart = { navigate("login") }, onRegister = { navigate("register") })
                "login" -> LoginScreen(onLogin = { email,password,role -> vm.login(email,password,role) { if(it) navigate("dashboard") } }, onRegister = { navigate("register") })
                "register" -> RegisterScreen(onRegister = { n,e,p,a -> vm.register(n,e,p,a) { if(it) navigate("dashboard") } }, onLogin = { navigate("login") })
                "dashboard" -> user?.let { u -> Dashboard(u, museums, products, requests, go = { navigate(it) }, goMuseum = goMuseum, goProduct = goProduct) }
                "map" -> MuseumMap(museums, user?.role == "ADMIN" || user?.role == "INVESTIGADOR", onSelect = goMuseum, onLocation = requestLocation, onSave = { vm.saveMuseum(it) })
                "museumDetail" -> museums.firstOrNull { it.id == museumId }?.let { m ->
                    MuseumDetail(m, user?.role ?: "CLIENTE", isFavorite = favorites.any { it.userId == user?.id && it.museumId == m.id },
                        onFavorite = { vm.toggleFavorite(m.id) }, onSave = { vm.saveMuseum(it) })
                } ?: EmptyState("Museo no encontrado")
                "scanner" -> ScannerScreen(onScan = {
                    val opts = ScanOptions().setDesiredBarcodeFormats(ScanOptions.QR_CODE).setPrompt("Enfoca un QR del museo")
                        .setBeepEnabled(false).setOrientationLocked(false)
                    scanLauncher.launch(opts)
                }, onManual = { text -> resultCode = text.trim().uppercase(); vm.registerScan(resultCode); navigate("qrResult") })
                "qrResult" -> QRResultScreen(resultCode, museums, products, onMuseum = goMuseum, onProduct = goProduct, onAgain = { navigate("scanner") })
                "catalog" -> CatalogScreen(products, isAdmin = user?.role == "ADMIN", onSelect = goProduct, onSave = { vm.saveProduct(it) }, onSync = { vm.syncWithApi() })
                "productDetail" -> products.firstOrNull { it.id == productId }?.let { p ->
                    ProductDetail(p, user?.role ?: "CLIENTE", onAdd = { count -> vm.addCart(p,count) }, onSave = { vm.saveProduct(it) })
                } ?: EmptyState("Producto no encontrado")
                "favorites" -> user?.let { u -> FavoritesScreen(museums.filter { m -> favorites.any { it.userId == u.id && it.museumId == m.id } }, onMuseum = goMuseum, onRemove = { vm.toggleFavorite(it) }) }
                "cart" -> user?.let { u -> CartScreen(u, products, cart.filter { it.userId == u.id }, onChange = { id, req, avail -> vm.changeQuantity(id, req, avail) }, onCheckout = { amount, title -> vm.checkout(amount, title) }, onRequests = { navigate("requests") }) }
                "requests" -> user?.let { u -> RequestsScreen(requests.filter { u.role == "ADMIN" || it.userId == u.id }, u.role == "ADMIN", onStatus = { id, status -> vm.setRequestStatus(id, status) }) }
                "notifications" -> user?.let { u -> NotificationsScreen(notices.filter { it.targetUserId == 0 || it.targetUserId == u.id }, u.role == "ADMIN", onPublish = { title, text -> vm.publishNotice(title, text) }, onNotify = requestNotice, go = { navigate(it) }) }
                "profile" -> user?.let { u -> ProfileScreen(u, scans.filter { it.userId == u.id },
                    prefs.firstOrNull { it.userId == u.id } ?: PreferenceEntity(u.id),
                    onPref = { vm.updatePreferences(it) }, onRename = { vm.renameProfile(it) },
                    onLogout = { vm.signOut(); navigate("welcome") }, go = { navigate(it) }) }
            }
        }
    }
}

private fun titleFor(route: String) = when (route) {
    "museumDetail" -> "Ficha del museo"; "scanner" -> "Escáner QR"; "qrResult" -> "Resultado de QR"
    "productDetail" -> "Detalle del catálogo"; "favorites" -> "Mis favoritos"; "cart" -> "Mi carrito"
    "requests" -> "Historial y solicitudes"; else -> "Museo de los Museos"
}

@Composable fun InfoBanner(text: String) {
    Surface(color = PaleGold, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, null, tint = Navy, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp)); Text(text, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable fun PrimaryButton(label: String, onClick: () -> Unit) {
    PrimaryButton(label = label, enabled = true, onClick = onClick)
}

@Composable fun PrimaryButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = DeepNavy)) {
        Text(label, fontWeight = FontWeight.Bold)
    }
}
@Composable fun EmptyState(text: String) = Box(Modifier.fillMaxSize().padding(30.dp), contentAlignment = Alignment.Center) {
    Text(text, color = Ink, textAlign = TextAlign.Center)
}
@Composable fun SectionHeading(text: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Navy)
        if (action != null) TextButton(onClick = onAction) { Text(action) }
    }
}
@Composable fun MuseumArt(kind: String, modifier: Modifier = Modifier) {
    val resource = when(kind) {
        "book" -> R.drawable.cover_book
        "kit" -> R.drawable.cover_kit
        "course" -> R.drawable.cover_course
        "merch" -> R.drawable.cover_merch
        else -> R.drawable.cover_museum
    }
    Image(painterResource(resource), contentDescription = "Ilustración de demostración: $kind", contentScale = ContentScale.Crop,
        modifier = modifier.clip(RoundedCornerShape(12.dp)))
}
@Composable fun SmallLabel(text: String) = Surface(color = CardGray, shape = RoundedCornerShape(20.dp)) {
    Text(text, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
}

@Composable fun WelcomeScreen(onStart: () -> Unit, onRegister: () -> Unit) {
    var enter by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { enter = true }
    Box(Modifier.fillMaxSize().background(DeepNavy)) {
        MuseumArt("museum", Modifier.fillMaxWidth().fillMaxHeight(0.73f))
        Box(Modifier.fillMaxSize().background(DeepNavy.copy(alpha = 0.60f)))
        Column(Modifier.fillMaxSize().padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
            Spacer(Modifier.height(90.dp))
            AnimatedVisibility(visible=enter,enter=fadeIn(animationSpec=tween(650))+scaleIn(initialScale=.86f)) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.AccountBalance, null, tint = Gold, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(20.dp))
                Text("Museo de los\nMuseos", color = Color.White, style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(20.dp))
                Text("Conecta, aprende y explora el patrimonio de Iberoamérica", color = Color.White,
                    textAlign = TextAlign.Center)
              }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PrimaryButton("Comenzar", onStart)
                OutlinedButton(onClick = onRegister, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) {
                    Text("Crear cuenta de demostración")
                }
                Text("Prototipo académico · datos sintéticos", color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable fun LoginScreen(onLogin: (String,String,String) -> Unit, onRegister: () -> Unit) {
    var role by rememberSaveable { mutableStateOf("CLIENTE") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Spacer(Modifier.height(16.dp))
        Icon(Icons.Default.AccountBalance, null, tint = Gold, modifier = Modifier.align(Alignment.CenterHorizontally).size(56.dp))
        Text("Iniciar sesión", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Navy)
        Text("Selecciona un perfil académico", color = Ink)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("CLIENTE", "INVESTIGADOR", "ADMIN").forEach { option ->
                FilterChip(selected = role == option, onClick = { role = option },
                    label = { Text(when(option){"ADMIN"->"Admin"; "INVESTIGADOR"->"Investigador";else->"Cliente"}, style = MaterialTheme.typography.labelSmall) })
            }
        }
        OutlinedTextField(value = email, onValueChange = { email=it }, label = { Text("Correo electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, null) }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = submitted && Validators.email(email)!=null,
            supportingText = { if (submitted) Validators.email(email)?.let { Text(it) } }, modifier=Modifier.fillMaxWidth())
        OutlinedTextField(value = password, onValueChange = { password=it }, label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, null) }, visualTransformation = PasswordVisualTransformation(),
            isError = submitted && Validators.password(password)!=null,
            supportingText = { if(submitted) Validators.password(password)?.let { Text(it) } }, modifier=Modifier.fillMaxWidth())
        PrimaryButton("Iniciar sesión") {
            submitted=true
            if (Validators.email(email)==null && Validators.password(password)==null) onLogin(email,password,role)
        }
        TextButton(onClick = onRegister, modifier=Modifier.align(Alignment.CenterHorizontally)) { Text("¿No tienes cuenta? Regístrate") }
        InfoBanner("Cuentas ficticias: cliente@demo.cl, investigador@demo.cl y admin@demo.cl. Contraseña para las tres: museo123")
    }
}

@Composable fun RegisterScreen(onRegister: (String,String,String,Int)->Unit, onLogin:()->Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var avatar by rememberSaveable { mutableIntStateOf(0) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(Icons.Default.AccountBalance, null, modifier=Modifier.size(40.dp).align(Alignment.CenterHorizontally), tint=Navy)
        Text("Crear tu cuenta", style=MaterialTheme.typography.headlineMedium, color=Navy, fontWeight=FontWeight.Bold)
        Text("Registro de cliente con información ficticia")
        OutlinedTextField(name,{name=it}, label={Text("Nombre completo")}, leadingIcon={Icon(Icons.Default.Person,null)},
            isError=submitted&&Validators.name(name)!=null, supportingText={if(submitted) Validators.name(name)?.let { Text(it) }},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(email,{email=it}, label={Text("Correo electrónico")}, leadingIcon={Icon(Icons.Default.Email,null)},
            isError=submitted&&Validators.email(email)!=null, supportingText={if(submitted) Validators.email(email)?.let { Text(it) }},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(password,{password=it}, label={Text("Contraseña")}, visualTransformation=PasswordVisualTransformation(),
            leadingIcon={Icon(Icons.Default.Lock,null)}, isError=submitted&&Validators.password(password)!=null,
            supportingText={if(submitted) Validators.password(password)?.let { Text(it) }},modifier=Modifier.fillMaxWidth())
        Text("Avatar de demostración", fontWeight=FontWeight.Bold)
        Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            (0..3).forEach { i ->
                Surface(modifier=Modifier.size(52.dp).clickable { avatar=i }, shape=CircleShape,
                    color=if(avatar==i) PaleGold else CardGray, border=if(avatar==i) BorderStroke(2.dp,Gold) else null) {
                    Box(contentAlignment=Alignment.Center) { Icon(if(i%2==0) Icons.Default.Face else Icons.Default.AccountCircle, null, tint=Navy) }
                }
            }
        }
        PrimaryButton("Registrarme") {
            submitted=true
            if (Validators.name(name)==null && Validators.email(email)==null && Validators.password(password)==null)
                onRegister(name,email,password,avatar)
        }
        TextButton(onClick=onLogin) { Text("Ya tengo cuenta · Iniciar sesión") }
        InfoBanner("No ingreses tu contraseña ni correo real. Esta es una app de laboratorio.")
    }
}

@Composable fun Dashboard(user: UserEntity, museums: List<MuseumEntity>, products: List<ProductEntity>,
                         requests: List<RequestEntity>, go: (String)->Unit, goMuseum:(Int)->Unit, goProduct:(Int)->Unit) {
    LazyColumn(contentPadding=PaddingValues(16.dp), verticalArrangement=Arrangement.spacedBy(14.dp)) {
        item {
            Surface(color=Navy,shape=RoundedCornerShape(20.dp),modifier=Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text("¡Hola, ${user.displayName.substringBefore(' ')}!", color=Color.White, style=MaterialTheme.typography.headlineSmall, fontWeight=FontWeight.Bold)
                    Text(when(user.role) { "ADMIN"->"Gestiona el catálogo y solicitudes";"INVESTIGADOR"->"Explora y actualiza fichas museológicas";else->"Sigamos explorando el conocimiento" },color=Color.White.copy(alpha=.85f))
                    Spacer(Modifier.height(14.dp))
                    MuseumArt("museum", Modifier.fillMaxWidth().height(130.dp))
                }
            }
        }
        item {
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                QuickAction("Mapa", Icons.Default.Map, Modifier.weight(1f)) { go("map") }
                QuickAction("Catálogo", Icons.Default.MenuBook, Modifier.weight(1f)) { go("catalog") }
                if (user.role != "ADMIN") QuickAction("Escáner", Icons.Default.QrCodeScanner, Modifier.weight(1f)) { go("scanner") }
                else QuickAction("Pedidos", Icons.Default.Receipt, Modifier.weight(1f)) { go("requests") }
            }
        }
        item {
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                if(user.role=="CLIENTE") {
                    QuickAction("Favoritos", Icons.Default.Favorite, Modifier.weight(1f)) { go("favorites") }
                    QuickAction("Mi carrito", Icons.Default.ShoppingCart, Modifier.weight(1f)) { go("cart") }
                }
                QuickAction("Solicitudes", Icons.Default.Assignment, Modifier.weight(1f)) { go("requests") }
                QuickAction("Avisos", Icons.Default.Notifications, Modifier.weight(1f)) { go("notifications") }
            }
        }
        if(user.role=="ADMIN") item { InfoBanner("Solicitudes registradas: ${requests.size}. Revisa y cambia su estado desde Historial.") }
        if(user.role=="INVESTIGADOR") item { InfoBanner("Puedes crear fichas desde el Mapa y editarlas desde su detalle. Usa datos ficticios.") }
        item { SectionHeading("Museos destacados", "Ver mapa") { go("map") } }
        items(museums.take(3), key={"museum_${it.id}"}) { museum ->
            Surface(onClick={goMuseum(museum.id)},color=Color.White,shape=RoundedCornerShape(14.dp),modifier=Modifier.fillMaxWidth()) {
                Row(Modifier.padding(10.dp), verticalAlignment=Alignment.CenterVertically) {
                    MuseumArt("museum",Modifier.size(74.dp)); Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) { Text(museum.name,fontWeight=FontWeight.Bold);Text("${museum.city}, ${museum.country}",style=MaterialTheme.typography.bodySmall); SmallLabel(museum.project) }
                    Icon(Icons.Default.ChevronRight,null)
                }
            }
        }
        item { SectionHeading("Explora el catálogo", "Ver todos") { go("catalog") } }
        items(products.take(2), key={"product_${it.id}"}) { p ->
            Surface(onClick={goProduct(p.id)},shape=RoundedCornerShape(14.dp),color=Color.White) {
                Row(Modifier.padding(10.dp),verticalAlignment=Alignment.CenterVertically) {
                    MuseumArt(p.imageKind,Modifier.size(75.dp)); Spacer(Modifier.width(14.dp))
                    Column { Text(p.title,fontWeight=FontWeight.Bold);Text(money(p.price),color=Teal);Text("Precio ficticio",style=MaterialTheme.typography.labelSmall) }
                }
            }
        }
        item { InfoBanner("Proyecto académico. Precios, museos, coordenadas y solicitudes de demostración.") }
    }
}

@Composable fun QuickAction(name: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick=onClick,modifier=modifier.height(82.dp),color=Color.White,shape=RoundedCornerShape(14.dp)) {
        Column(horizontalAlignment=Alignment.CenterHorizontally, verticalArrangement=Arrangement.Center) {
            Icon(icon,null,tint=Navy); Spacer(Modifier.height(5.dp)); Text(name,style=MaterialTheme.typography.labelSmall, textAlign=TextAlign.Center)
        }
    }
}

@Composable fun MuseumMap(museums: List<MuseumEntity>, canEdit: Boolean, onSelect:(Int)->Unit, onLocation:()->Unit,
                         onSave:(MuseumEntity)->Unit) {
    var filter by rememberSaveable { mutableStateOf("Todos") }
    var showForm by remember { mutableStateOf(false) }
    val countries = listOf("Todos") + museums.map { it.country }.distinct().sorted()
    val filtered = museums.filter { filter=="Todos" || filter==it.country || filter==it.project }
    Column(Modifier.fillMaxSize().padding(horizontal=12.dp,vertical=10.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Text("Mapa de Museos Virtuales",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold,color=Navy)
        Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
            (countries+listOf("Patrimonio digital","Educación museológica","Rutas iberoamericanas")).distinct().forEach { country ->
                FilterChip(selected=filter==country,onClick={filter=country},label={Text(country)})
            }
        }
        val context = LocalContext.current
        val mapView = remember(context) { MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK); setMultiTouchControls(true)
            controller.setZoom(3.5); controller.setCenter(GeoPoint(-11.0,-59.0))
        } }
        DisposableEffect(mapView) { mapView.onResume(); onDispose { mapView.onPause(); mapView.onDetach() } }
        AndroidView(factory={mapView},modifier=Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(16.dp)),update={map ->
            map.overlays.clear()
            filtered.forEach { m ->
                map.overlays.add(Marker(map).apply {
                    position=GeoPoint(m.lat,m.lon); title=m.name; snippet="${m.city}, ${m.country}"
                    setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_BOTTOM)
                    setOnMarkerClickListener { _, _ -> onSelect(m.id); true }
                })
            }
            map.invalidate()
        })
        Text("© OpenStreetMap contributors · marcadores y coordenadas sintéticos",style=MaterialTheme.typography.labelSmall)
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick=onLocation,modifier=Modifier.weight(1f)) { Icon(Icons.Default.MyLocation,null); Spacer(Modifier.width(4.dp));Text("Mi ubicación") }
            if(canEdit) Button(onClick={showForm=true},modifier=Modifier.weight(1f)) { Icon(Icons.Default.Add,null);Text("Agregar ficha") }
        }
    }
    if(showForm) MuseumEditDialog(existing=null, nextId=(museums.maxOfOrNull{it.id}?:0)+1,
        onDismiss={showForm=false},onSave={onSave(it);showForm=false})
}

@Composable fun MuseumDetail(m: MuseumEntity, role:String, isFavorite:Boolean,onFavorite:()->Unit, onSave:(MuseumEntity)->Unit) {
    var edit by remember { mutableStateOf(false) }
    val canEdit = role in listOf("ADMIN","INVESTIGADOR")
    LazyColumn(contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        item { MuseumArt("museum",Modifier.fillMaxWidth().height(245.dp)) }
        item {
            Text(m.name,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold,color=Navy)
            Text("${m.city}, ${m.country}",color=Teal)
            Spacer(Modifier.height(6.dp));SmallLabel(m.project)
        }
        item { Text(m.description,style=MaterialTheme.typography.bodyLarge) }
        item { InfoBanner("La ubicación corresponde a coordenadas aproximadas de la ciudad. El enlace de visita virtual es ilustrativo, no un museo real.") }
        item {
            OutlinedButton(onClick={},enabled=false,modifier=Modifier.fillMaxWidth()) {
                Icon(Icons.Default.OpenInNew,null); Spacer(Modifier.width(5.dp));Text("Visita virtual (enlace de prueba)")
            }
        }
        if(role=="CLIENTE") item {
            PrimaryButton(if(isFavorite) "Quitar de favoritos" else "Guardar en favoritos", onFavorite)
        }
        if(canEdit) item { PrimaryButton("Editar ficha de investigación") { edit=true } }
        item { Text("Código QR de prueba: MUSEO:${m.id}",style=MaterialTheme.typography.bodySmall) }
    }
    if(edit) MuseumEditDialog(existing=m,nextId=m.id,onDismiss={edit=false},onSave={onSave(it);edit=false})
}

@Composable fun MuseumEditDialog(existing:MuseumEntity?,nextId:Int,onDismiss:()->Unit,onSave:(MuseumEntity)->Unit) {
    var name by remember(existing) { mutableStateOf(existing?.name ?: "") }
    var country by remember(existing) { mutableStateOf(existing?.country ?: "Chile") }
    var city by remember(existing) { mutableStateOf(existing?.city ?: "Santiago") }
    var description by remember(existing) { mutableStateOf(existing?.description ?: "") }
    var lat by remember(existing) { mutableStateOf((existing?.lat ?: -33.4489).toString()) }
    var lon by remember(existing) { mutableStateOf((existing?.lon ?: -70.6693).toString()) }
    var tried by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest=onDismiss,title={Text("Ficha museológica · DEMO")},
        text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(name,{name=it},label={Text("Nombre")},isError=tried&&name.trim().length<3)
            OutlinedTextField(country,{country=it},label={Text("País")})
            OutlinedTextField(city,{city=it},label={Text("Ciudad")})
            OutlinedTextField(description,{description=it},label={Text("Descripción")},minLines=2)
            OutlinedTextField(lat,{lat=it},label={Text("Latitud sintética")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal))
            OutlinedTextField(lon,{lon=it},label={Text("Longitud sintética")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal))
            Text("No utilices datos personales ni institucionales reales.",style=MaterialTheme.typography.labelSmall)
        }},confirmButton={TextButton(onClick={
            tried=true
            val a=lat.toDoubleOrNull(); val b=lon.toDoubleOrNull()
            if(name.trim().length>=3 && country.isNotBlank() && city.isNotBlank() && description.isNotBlank() && a!=null && b!=null && a in -90.0..90.0 && b in -180.0..180.0) {
                onSave(MuseumEntity(nextId,name.trim(),country.trim(),city.trim(),description.trim(),existing?.project?:"Investigación demo",a,b,existing?.virtualUrl?:"https://example.org/demo"))
            }
        }){Text("Guardar")}},dismissButton={TextButton(onClick=onDismiss){Text("Cancelar")}})
}

@Composable fun ScannerScreen(onScan:()->Unit,onManual:(String)->Unit) {
    var value by rememberSaveable { mutableStateOf("MUSEO:1") }
    Column(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(20.dp),horizontalAlignment=Alignment.CenterHorizontally) {
        Spacer(Modifier.height(28.dp))
        Surface(color=DeepNavy,shape=RoundedCornerShape(28.dp),modifier=Modifier.fillMaxWidth().height(240.dp)) {
            Box(contentAlignment=Alignment.Center) {
                Icon(Icons.Default.QrCodeScanner,null,tint=Color.White,modifier=Modifier.size(134.dp))
            }
        }
        Text("Escanea códigos de museos, publicaciones o productos",textAlign=TextAlign.Center)
        PrimaryButton("Abrir cámara y leer QR",onScan)
        InfoBanner("Puedes probar sin cámara escribiendo MUSEO:1, PRODUCTO:1 o CURSO:3.")
        OutlinedTextField(value,{value=it},label={Text("Código QR manual de prueba")},modifier=Modifier.fillMaxWidth())
        OutlinedButton(onClick={onManual(value)},modifier=Modifier.fillMaxWidth(),enabled=value.isNotBlank()) { Text("Validar código") }
    }
}

@Composable fun QRResultScreen(code:String,museums:List<MuseumEntity>,products:List<ProductEntity>,
                              onMuseum:(Int)->Unit,onProduct:(Int)->Unit,onAgain:()->Unit) {
    val museum=if(code.startsWith("MUSEO:")) museums.firstOrNull{it.id.toString()==code.substringAfter(':')} else null
    val product=if(code.startsWith("PRODUCTO:")||code.startsWith("CURSO:")) products.firstOrNull{
        it.id.toString()==code.substringAfter(':') && (code.startsWith("PRODUCTO:") || it.category=="Cursos")
    } else null
    val found=museum!=null||product!=null
    var shown by remember(code) { mutableStateOf(false) }
    LaunchedEffect(code) { shown=true }
    Column(Modifier.fillMaxSize().padding(24.dp),verticalArrangement=Arrangement.spacedBy(18.dp),horizontalAlignment=Alignment.CenterHorizontally) {
        Spacer(Modifier.height(26.dp))
        AnimatedVisibility(visible=shown,enter=fadeIn(tween(350))+scaleIn(initialScale=.6f)) {
            Icon(if(found) Icons.Default.CheckCircle else Icons.Default.Error,null,
                tint=if(found) Teal else MaterialTheme.colorScheme.error,modifier=Modifier.size(84.dp))
        }
        Text(if(found) "¡Código identificado!" else "Código no reconocido",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
        if(found) {
            MuseumArt(if(museum!=null) "museum" else product?.imageKind?:"book",Modifier.fillMaxWidth().height(180.dp))
            Text(museum?.name ?: product?.title ?: "",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
            Text(if(museum!=null) "Ficha de museo" else "Artículo del catálogo")
            PrimaryButton("Ver detalle") { museum?.let{onMuseum(it.id)} ?: product?.let{onProduct(it.id)} }
        } else InfoBanner("Usa un código de demostración válido. La lectura no almacena información personal.")
        OutlinedButton(onClick=onAgain,modifier=Modifier.fillMaxWidth()) { Text("Volver a escanear") }
    }
}

@Composable fun CatalogScreen(products:List<ProductEntity>,isAdmin:Boolean,onSelect:(Int)->Unit,
                              onSave:(ProductEntity)->Unit,onSync:()->Unit) {
    var search by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("Todos") }
    var create by remember { mutableStateOf(false) }
    val visible=products.filter { (isAdmin||it.active) && (category=="Todos"||it.category==category) &&
        it.title.contains(search,ignoreCase=true) }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal=16.dp,vertical=10.dp)) {
            OutlinedTextField(search,{search=it},modifier=Modifier.fillMaxWidth(),singleLine=true,
                leadingIcon={Icon(Icons.Default.Search,null)},placeholder={Text("Buscar publicaciones, cursos, artículos...")})
            Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                listOf("Todos","Libros","Cursos","Materiales","Merchandising","Publicaciones").forEach { item ->
                    FilterChip(selected=item==category,onClick={category=item},label={Text(item)})
                }
            }
            if(isAdmin) Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick=onSync,modifier=Modifier.weight(1f)) { Icon(Icons.Default.Sync,null);Text("REST") }
                Button(onClick={create=true},modifier=Modifier.weight(1f)) { Icon(Icons.Default.Add,null);Text("Producto") }
            }
        }
        LazyColumn(contentPadding=PaddingValues(12.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            item { Text("Catálogo de productos y cursos · Precios simulados",style=MaterialTheme.typography.labelMedium) }
            items(visible.chunked(2)) { row ->
                Row(horizontalArrangement=Arrangement.spacedBy(10.dp),modifier=Modifier.fillMaxWidth()) {
                    row.forEach { product ->
                        Surface(onClick={onSelect(product.id)},modifier=Modifier.weight(1f),shape=RoundedCornerShape(14.dp),color=Color.White) {
                            Column(Modifier.padding(10.dp)) {
                                MuseumArt(product.imageKind,Modifier.fillMaxWidth().height(120.dp))
                                Spacer(Modifier.height(6.dp))
                                Text(product.title,fontWeight=FontWeight.Bold,maxLines=2,style=MaterialTheme.typography.bodyMedium)
                                Text(money(product.price),color=Teal,fontWeight=FontWeight.SemiBold)
                                Text(if(product.active) "Stock: ${product.stock}" else "Desactivado",style=MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    if(row.size==1) Spacer(Modifier.weight(1f))
                }
            }
            if(visible.isEmpty()) item { Text("No hay resultados para este filtro") }
        }
    }
    if(create) ProductEditDialog(null, (products.maxOfOrNull{it.id}?:0)+1, onDismiss={create=false},onSave={onSave(it);create=false})
}

@Composable fun ProductDetail(p:ProductEntity,role:String,onAdd:(Int)->Unit,onSave:(ProductEntity)->Unit) {
    var amount by rememberSaveable(p.id) { mutableIntStateOf(1) }
    var edit by remember { mutableStateOf(false) }
    LazyColumn(contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        item { MuseumArt(p.imageKind,Modifier.fillMaxWidth().height(230.dp)) }
        item {
            Text(p.title,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
            Text(money(p.price),style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold,color=Teal)
            Text("Precio ficticio en CLP",style=MaterialTheme.typography.bodySmall)
        }
        item { SmallLabel(p.category); Spacer(Modifier.height(10.dp));Text(p.description) }
        item { Text("Disponibilidad: ${p.stock} ${if(p.active) "unidades/cupos" else "(inactivo)"}") }
        if(role=="CLIENTE" && p.active) {
            if(p.category!="Cursos") item {
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(18.dp)) {
                    Text("Cantidad:"); IconButton(onClick={if(amount>1) amount--}){Icon(Icons.Default.Remove,null)}
                    Text(amount.toString()); IconButton(onClick={if(amount<p.stock) amount++}){Icon(Icons.Default.Add,null)}
                }
            }
            item { PrimaryButton(if(p.category=="Cursos") "Solicitar inscripción" else "Agregar al carrito",onClick={onAdd(amount)},enabled=p.stock>0) }
        }
        if(role=="ADMIN") item { PrimaryButton("Editar / desactivar artículo") {edit=true} }
        item { InfoBanner("Datos académicos. No se realizan transacciones reales ni se envían datos a couriers.") }
    }
    if(edit) ProductEditDialog(p,p.id,onDismiss={edit=false},onSave={onSave(it);edit=false})
}

@Composable fun ProductEditDialog(existing:ProductEntity?,id:Int,onDismiss:()->Unit,onSave:(ProductEntity)->Unit) {
    var name by remember(existing) { mutableStateOf(existing?.title?:"") }
    var category by remember(existing) { mutableStateOf(existing?.category?:"Libros") }
    var description by remember(existing) { mutableStateOf(existing?.description?:"") }
    var price by remember(existing) { mutableStateOf(existing?.price?.toString()?:"") }
    var stock by remember(existing) { mutableStateOf(existing?.stock?.toString()?:"") }
    var active by remember(existing) { mutableStateOf(existing?.active?:true) }
    var submit by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest=onDismiss,title={Text(if(existing==null) "Nuevo artículo demo" else "Editar artículo demo")},
        text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(name,{name=it},label={Text("Título")},isError=submit&&name.length<3)
            Text("Categoría")
            Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                listOf("Libros","Cursos","Materiales","Merchandising","Publicaciones").forEach {
                    FilterChip(selected=category==it,onClick={category=it},label={Text(it)})
                }
            }
            OutlinedTextField(description,{description=it},label={Text("Descripción")})
            OutlinedTextField(price,{price=it},label={Text("Precio CLP (ficticio)")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number))
            OutlinedTextField(stock,{stock=it},label={Text("Stock o cupos")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number))
            Row(verticalAlignment=Alignment.CenterVertically) { Switch(active,{active=it});Text(" Publicado") }
        }},confirmButton={TextButton(onClick={
            submit=true
            val p=price.toIntOrNull();val s=stock.toIntOrNull()
            if(name.trim().length>=3&&description.isNotBlank()&&p!=null&&p>=0&&s!=null&&s>=0) {
                val weight=if(category=="Cursos") 0.0 else existing?.weightKg?:0.5
                onSave(ProductEntity(id,name.trim(),category,description.trim(),p,s,weight,
                    existing?.imageKind?:if(category=="Cursos") "course" else "book",active))
            }
        }){Text("Guardar")}},dismissButton={TextButton(onClick=onDismiss){Text("Cancelar")}})
}

@Composable fun FavoritesScreen(museums:List<MuseumEntity>,onMuseum:(Int)->Unit,onRemove:(Int)->Unit) {
    LazyColumn(contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item { SectionHeading("Mi colección de museos") }
        if(museums.isEmpty()) item { InfoBanner("Aún no guardaste museos. Explora el mapa y marca tus favoritos.") }
        items(museums) { m ->
            Surface(color=Color.White,shape=RoundedCornerShape(14.dp)) {
                Row(Modifier.padding(10.dp),verticalAlignment=Alignment.CenterVertically) {
                    MuseumArt("museum",Modifier.size(75.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f).clickable{onMuseum(m.id)}) {
                        Text(m.name,fontWeight=FontWeight.Bold);Text(m.country,style=MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick={onRemove(m.id)}) { Icon(Icons.Default.Favorite,"Quitar",tint=Color(0xFFC64753)) }
                }
            }
        }
    }
}

@Composable fun CartScreen(user:UserEntity,products:List<ProductEntity>,lines:List<CartEntity>,
                          onChange:(Int,Int,Int)->Unit,onCheckout:(Int,String)->Unit,onRequests:()->Unit) {
    var destination by rememberSaveable { mutableStateOf("Chile - Metropolitana") }
    var express by rememberSaveable { mutableStateOf(false) }
    var confirm by remember { mutableStateOf(false) }
    val entries=lines.mapNotNull { line -> products.firstOrNull{it.id==line.productId}?.let{it to line.quantity} }
    val subtotal=entries.sumOf { (p,q)->p.price*q }
    val weight=entries.sumOf { (p,q)->p.weightKg*q }
    val quote=ShippingCalculator.calculate(subtotal,weight,destination,express)
    LazyColumn(contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item { SectionHeading("Mi carrito") }
        if(entries.isEmpty()) item { InfoBanner("Tu carrito está vacío. Puedes consultar el catálogo desde la barra inferior.") }
        items(entries) { (p,q) ->
            Surface(color=Color.White,shape=RoundedCornerShape(14.dp)) {
                Row(Modifier.padding(8.dp),verticalAlignment=Alignment.CenterVertically) {
                    MuseumArt(p.imageKind,Modifier.size(65.dp))
                    Column(Modifier.weight(1f).padding(start=12.dp)) {
                        Text(p.title,fontWeight=FontWeight.Bold);Text(money(p.price*q),color=Teal)
                    }
                    IconButton(onClick={onChange(p.id,q-1,p.stock)}){Icon(Icons.Default.Remove,null)}
                    Text(q.toString()); IconButton(onClick={onChange(p.id,q+1,p.stock)}){Icon(Icons.Default.Add,null)}
                }
            }
        }
        if(entries.isNotEmpty()) {
            item { SectionHeading("Calcular envío") }
            item { Text("País o zona de destino (tarifas académicas)") }
            item {
                Column(verticalArrangement=Arrangement.spacedBy(5.dp)) {
                    listOf("Chile - Metropolitana","Chile - Regiones","Internacional - América","Internacional - Europa").forEach { zone ->
                        Row(verticalAlignment=Alignment.CenterVertically,modifier=Modifier.fillMaxWidth().clickable{destination=zone}) {
                            RadioButton(selected=zone==destination,onClick={destination=zone});Text(zone)
                        }
                    }
                }
            }
            item { Row(verticalAlignment=Alignment.CenterVertically) {
                Switch(checked=express,onCheckedChange={express=it});Text("  Envío exprés (+60% tarifa simulada)")
            } }
            item { Surface(color=Color.White,shape=RoundedCornerShape(14.dp),modifier=Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(7.dp)) {
                    PriceRow("Subtotal",subtotal);PriceRow("Envío simulado",quote.shipping)
                    HorizontalDivider();PriceRow("Total estimado",quote.total,bold=true)
                    Text("Peso calculado: ${"%.2f".format(weight)} kg",style=MaterialTheme.typography.bodySmall)
                    Text(quote.description,style=MaterialTheme.typography.labelSmall)
                }
            } }
            item { PrimaryButton("Registrar solicitud (sin pago)") {confirm=true} }
        }
        item { OutlinedButton(onClick=onRequests,modifier=Modifier.fillMaxWidth()){Text("Consultar historial de solicitudes")} }
    }
    if(confirm) AlertDialog(onDismissRequest={confirm=false},title={Text("Confirmar solicitud ficticia")},
        text={Text("Total estimado: ${money(quote.total)}. No se cobrará ni enviará ningún producto.")},
        confirmButton={TextButton(onClick={onCheckout(quote.total,"Pedido de ${entries.size} artículo(s) · $destination");confirm=false}){Text("Registrar")}},
        dismissButton={TextButton(onClick={confirm=false}){Text("Volver")}})
}
@Composable fun PriceRow(label:String, amount:Int,bold:Boolean=false) = Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
    Text(label,fontWeight=if(bold) FontWeight.Bold else FontWeight.Normal)
    Text(money(amount),fontWeight=if(bold) FontWeight.Bold else FontWeight.Medium,color=if(bold) Teal else Ink)
}

@Composable fun RequestsScreen(requests:List<RequestEntity>,isAdmin:Boolean,onStatus:(Int,String)->Unit) {
    var filter by rememberSaveable { mutableStateOf("Todos") }
    val allowed=listOf("Pendiente","Confirmado","Enviado","Entregado")
    val visible=requests.filter { filter=="Todos" || it.status==filter }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(horizontal=16.dp).horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
            (listOf("Todos")+allowed).forEach { status -> FilterChip(selected=filter==status,onClick={filter=status},label={Text(status)}) }
        }
        LazyColumn(contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            item { Text(if(isAdmin) "Gestión de solicitudes registradas" else "Mis solicitudes",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold) }
            if(visible.isEmpty()) item { InfoBanner("Aún no hay solicitudes con este estado") }
            items(visible) { request ->
                Surface(shape=RoundedCornerShape(14.dp),color=Color.White,modifier=Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                            Text("#2026-${request.id.toString().padStart(4,'0')}",fontWeight=FontWeight.Bold)
                            SmallLabel(request.status)
                        }
                        Text(request.title,style=MaterialTheme.typography.bodyMedium)
                        Text(java.text.SimpleDateFormat("dd/MM/yyyy",java.util.Locale("es","CL")).format(java.util.Date(request.createdAt)),style=MaterialTheme.typography.labelSmall)
                        PriceRow("Total simulado",request.amount)
                        if(isAdmin) {
                            Text("Cambiar estado:",style=MaterialTheme.typography.labelSmall)
                            Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(5.dp)) {
                                allowed.forEach { state -> FilterChip(selected=request.status==state,onClick={onStatus(request.id,state)},label={Text(state)}) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable fun NotificationsScreen(notices:List<NoticeEntity>,isAdmin:Boolean,onPublish:(String,String)->Unit,
                                  onNotify:()->Unit,go:(String)->Unit) {
    var form by remember { mutableStateOf(false) }
    LazyColumn(contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item { SectionHeading("Notificaciones") }
        item {
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick=onNotify,modifier=Modifier.weight(1f)) { Icon(Icons.Default.NotificationsActive,null);Text("Aviso push") }
                if(isAdmin) Button(onClick={form=true},modifier=Modifier.weight(1f)) { Icon(Icons.Default.Add,null);Text("Crear aviso") }
            }
        }
        item { InfoBanner("Las alertas se generan localmente. No se utiliza un servicio push remoto.") }
        if(notices.isEmpty()) item { Text("Sin notificaciones por ahora") }
        items(notices) { n ->
            Surface(onClick={go(n.route)},color=Color.White,shape=RoundedCornerShape(14.dp)) {
                Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically) {
                    Icon(Icons.Default.Campaign,null,tint=Gold,modifier=Modifier.size(30.dp))
                    Column(Modifier.padding(start=12.dp)) {
                        Text(n.title,fontWeight=FontWeight.Bold);Text(n.message,style=MaterialTheme.typography.bodyMedium)
                        Text("Aviso de demostración",style=MaterialTheme.typography.labelSmall,color=Teal)
                    }
                }
            }
        }
    }
    if(form) {
        var title by remember { mutableStateOf("") }
        var text by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest={form=false},title={Text("Publicar aviso simulado")},text={Column {
            OutlinedTextField(title,{title=it},label={Text("Título")})
            OutlinedTextField(text,{text=it},label={Text("Contenido")},minLines=2)
        }},confirmButton={TextButton(onClick={onPublish(title,text);form=false}){Text("Publicar")}},
            dismissButton={TextButton(onClick={form=false}){Text("Cancelar")}})
    }
}

@Composable fun ProfileScreen(user:UserEntity,scans:List<ScanEntity>,prefs:PreferenceEntity,
                              onPref:(PreferenceEntity)->Unit,onRename:(String)->Unit,
                              onLogout:()->Unit,go:(String)->Unit) {
    var nameEdit by remember { mutableStateOf(false) }
    LazyColumn(contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item {
            Surface(color=Color.White,shape=RoundedCornerShape(16.dp)) {
                Row(Modifier.fillMaxWidth().padding(18.dp),verticalAlignment=Alignment.CenterVertically) {
                    Icon(if(user.avatar%2==0) Icons.Default.AccountCircle else Icons.Default.Face,null,tint=Navy,modifier=Modifier.size(66.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {Text(user.displayName,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleLarge)
                        SmallLabel(user.role);Text(user.email,style=MaterialTheme.typography.bodySmall)}
                    IconButton(onClick={nameEdit=true}) { Icon(Icons.Default.Edit,"Editar nombre") }
                }
            }
        }
        item { SectionHeading("Mi perfil y configuración") }
        if(user.role=="CLIENTE") {
            item { ProfileLink("Mi colección de favoritos",Icons.Default.Favorite) {go("favorites")} }
            item { ProfileLink("Carrito y costo de envío",Icons.Default.ShoppingCart) {go("cart")} }
        }
        item { ProfileLink("Mis solicitudes",Icons.Default.Receipt) {go("requests")} }
        if(user.role!="ADMIN") item { ProfileLink("Escanear QR",Icons.Default.QrCodeScanner) {go("scanner")} }
        item {
            Surface(color=Color.White,shape=RoundedCornerShape(14.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Text("Animación reducida / accesibilidad",modifier=Modifier.weight(1f))
                        Switch(prefs.reduceMotion,{onPref(prefs.copy(reduceMotion=it))})
                    }
                    HorizontalDivider()
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Text("Preferencia de avisos",modifier=Modifier.weight(1f));Switch(prefs.notifications,{onPref(prefs.copy(notifications=it))})
                    }
                    HorizontalDivider()
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Icon(Icons.Default.Language,null);Spacer(Modifier.width(8.dp));Text("Idioma: ${prefs.language} (MVP)",modifier=Modifier.weight(1f))
                    }
                }
            }
        }
        item { SectionHeading("Historial QR (${scans.size})") }
        items(scans.take(4)) { scan -> Text("${scan.code} · ${scan.result}",style=MaterialTheme.typography.bodySmall) }
        item { InfoBanner("Las preferencias se guardan por usuario en Room; la adaptación completa de idioma, accesibilidad y envío push remoto queda para la EFT.") }
        item { OutlinedButton(onClick=onLogout,modifier=Modifier.fillMaxWidth()) { Icon(Icons.Default.Logout,null);Text("Cerrar sesión") } }
    }
    if(nameEdit) {
        var changed by remember { mutableStateOf(user.displayName) }
        AlertDialog(onDismissRequest={nameEdit=false},title={Text("Editar nombre de perfil")},
            text={OutlinedTextField(changed,{changed=it},label={Text("Nombre")},isError=Validators.name(changed)!=null,
                supportingText={Validators.name(changed)?.let{Text(it)}})},
            confirmButton={TextButton(onClick={onRename(changed);nameEdit=false},enabled=Validators.name(changed)==null){Text("Guardar")}},
            dismissButton={TextButton(onClick={nameEdit=false}){Text("Cancelar")}})
    }
}
@Composable fun ProfileLink(label:String,icon:androidx.compose.ui.graphics.vector.ImageVector,onClick:()->Unit) {
    Surface(onClick=onClick,color=Color.White,shape=RoundedCornerShape(13.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically) {
            Icon(icon,null,tint=Navy);Spacer(Modifier.width(10.dp));Text(label,modifier=Modifier.weight(1f));Icon(Icons.Default.ChevronRight,null)
        }
    }
}
