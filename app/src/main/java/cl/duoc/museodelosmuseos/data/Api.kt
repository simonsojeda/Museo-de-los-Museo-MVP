package cl.duoc.museodelosmuseos.data

import cl.duoc.museodelosmuseos.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

/** API REST de prueba opcional; nunca transmite datos personales. */
interface MuseoApi {
    @GET("api/museums") suspend fun museums(): List<MuseumEntity>
    @GET("api/catalog") suspend fun catalog(): List<ProductEntity>
}
object ApiProvider {
    val api: MuseoApi by lazy {
        Retrofit.Builder().baseUrl(BuildConfig.BACKEND_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(MuseoApi::class.java)
    }
}
