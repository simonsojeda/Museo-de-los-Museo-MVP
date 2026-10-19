package cl.duoc.museodelosmuseos.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val displayName: String, val email: String, val passwordHash: String,
    val role: String = "CLIENTE", val avatar: Int = 0
)
@Entity(tableName = "preferences")
data class PreferenceEntity(
    @PrimaryKey val userId: Int,
    val notifications: Boolean = true,
    val reduceMotion: Boolean = false,
    val language: String = "Español"
)
@Entity(tableName = "museums")
data class MuseumEntity(
    @PrimaryKey val id: Int, val name: String, val country: String, val city: String,
    val description: String, val project: String, val lat: Double, val lon: Double,
    val virtualUrl: String = "https://example.org/visita-demo", val featured: Boolean = false
)
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Int, val title: String, val category: String,
    val description: String, val price: Int, val stock: Int,
    val weightKg: Double, val imageKind: String, val active: Boolean = true
)
@Entity(tableName = "favorites", primaryKeys = ["userId", "museumId"])
data class FavoriteEntity(val userId: Int, val museumId: Int)
@Entity(tableName = "cart", primaryKeys = ["userId", "productId"])
data class CartEntity(val userId: Int, val productId: Int, val quantity: Int)
@Entity(tableName = "requests")
data class RequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int, val title: String, val category: String,
    val amount: Int, val status: String = "Pendiente", val createdAt: Long = System.currentTimeMillis()
)
@Entity(tableName = "scans", indices = [Index(value = ["userId", "code"], unique = true)])
data class ScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int, val code: String, val result: String, val createdAt: Long = System.currentTimeMillis()
)
@Entity(tableName = "notices")
data class NoticeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val targetUserId: Int = 0, val title: String, val message: String,
    val route: String = "notifications", val createdAt: Long = System.currentTimeMillis()
)

@Dao interface MuseoDao {
    @Query("SELECT * FROM users ORDER BY id") fun users(): Flow<List<UserEntity>>
    @Query("SELECT COUNT(*) FROM users") suspend fun userCount(): Int
    @Query("UPDATE users SET displayName=:name WHERE id=:id") suspend fun renameUser(id: Int, name: String)
    @Query("SELECT * FROM preferences") fun preferences(): Flow<List<PreferenceEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun savePreferences(value: PreferenceEntity)
    @Query("SELECT * FROM users WHERE email=:email LIMIT 1") suspend fun userByEmail(email: String): UserEntity?
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun addUser(user: UserEntity): Long
    @Query("SELECT * FROM museums ORDER BY name") fun museums(): Flow<List<MuseumEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveMuseums(value: List<MuseumEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveMuseum(value: MuseumEntity)
    @Query("SELECT * FROM products ORDER BY title") fun products(): Flow<List<ProductEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveProducts(value: List<ProductEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveProduct(value: ProductEntity)
    @Query("SELECT * FROM favorites") fun favorites(): Flow<List<FavoriteEntity>>
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun addFavorite(value: FavoriteEntity)
    @Query("DELETE FROM favorites WHERE userId=:userId AND museumId=:museumId") suspend fun removeFavorite(userId: Int, museumId: Int)
    @Query("SELECT * FROM cart") fun cart(): Flow<List<CartEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveCart(value: CartEntity)
    @Query("DELETE FROM cart WHERE userId=:userId AND productId=:productId") suspend fun removeCart(userId: Int, productId: Int)
    @Query("DELETE FROM cart WHERE userId=:userId") suspend fun clearCart(userId: Int)
    @Query("SELECT * FROM requests ORDER BY createdAt DESC") fun requests(): Flow<List<RequestEntity>>
    @Insert suspend fun addRequest(value: RequestEntity)
    @Query("UPDATE requests SET status=:status WHERE id=:id") suspend fun updateRequest(id: Int, status: String)
    @Query("SELECT * FROM scans ORDER BY createdAt DESC") fun scans(): Flow<List<ScanEntity>>
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun addScan(value: ScanEntity)
    @Query("SELECT * FROM notices ORDER BY createdAt DESC") fun notices(): Flow<List<NoticeEntity>>
    @Insert suspend fun addNotice(value: NoticeEntity)
}

@Database(entities = [UserEntity::class, MuseumEntity::class, ProductEntity::class,
    FavoriteEntity::class, CartEntity::class, RequestEntity::class,
    ScanEntity::class, NoticeEntity::class, PreferenceEntity::class], version = 1, exportSchema = false)
abstract class MuseoDatabase : RoomDatabase() {
    abstract fun dao(): MuseoDao
    companion object {
        @Volatile private var instance: MuseoDatabase? = null
        fun get(context: android.content.Context): MuseoDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, MuseoDatabase::class.java, "museo_demo.db")
                .build().also { instance = it }
        }
    }
}

object Fixtures {
    // Coordenadas de demostración, aproximadas a ciudades y NO a museos reales.
    val museums = listOf(
        MuseumEntity(1,"Archivo de Arte Colonial (Demo)","Colombia","Bogotá","Colección académica de arte colonial y mediación digital.","Patrimonio digital",4.711,-74.072,trueUrl(),true),
        MuseumEntity(2,"Galería Virtual Andina (Demo)","Chile","Santiago","Experiencia introductoria a patrimonio y culturas del territorio andino.","Rutas iberoamericanas",-33.4489,-70.6693,trueUrl(),true),
        MuseumEntity(3,"Centro de Memoria Digital (Demo)","México","Ciudad de México","Recorrido de exposiciones museológicas de investigación.","Patrimonio digital",19.4326,-99.1332,trueUrl()),
        MuseumEntity(4,"Museo de Artes Iberoamericanas (Demo)","España","Madrid","Muestra interactiva de materiales culturales ficticios.","Rutas iberoamericanas",40.4168,-3.7038,trueUrl()),
        MuseumEntity(5,"Museo Virtual de Ciencias (Demo)","Perú","Lima","Colección simulada de patrimonio científico y conservación.","Educación museológica",-12.0464,-77.0428,trueUrl()),
        MuseumEntity(6,"Museo de Nuevas Narrativas (Demo)","Argentina","Buenos Aires","Explora prácticas de creación, educación y participación cultural.","Educación museológica",-34.6037,-58.3816,trueUrl())
    )
    private fun trueUrl() = "https://example.org/visita-demo"
    val products = listOf(
        ProductEntity(1,"Historia del arte iberoamericano","Libros","Publicación ficticia, tapa dura, 180 páginas.",18500,12,0.8,"book"),
        ProductEntity(2,"Kit de conservación documental","Materiales","Kit didáctico para prácticas de conservación.",24500,8,1.2,"kit"),
        ProductEntity(3,"Curso de curaduría virtual","Cursos","Introducción a herramientas y criterios curatoriales.",39000,20,0.0,"course"),
        ProductEntity(4,"Taller de museología digital","Cursos","Formación simulada con materiales de consulta.",29000,12,0.0,"course"),
        ProductEntity(5,"Bolso patrimonio cultural","Merchandising","Bolso de tela con ilustración de arquitectura museal.",9800,19,0.35,"merch"),
        ProductEntity(6,"Cuaderno del investigador","Publicaciones","Bitácora para fichas de investigación museográfica.",6900,14,0.4,"book")
    )
}
