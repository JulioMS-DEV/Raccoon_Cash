package ni.edu.uam.raccooncash.data.remote

import android.content.Context
import ni.edu.uam.raccooncash.data.session.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://192.168.1.32:8080/api/"
    private var sessionManager: SessionManager? = null

    fun initialize(context: Context) {
        if (sessionManager == null) {
            sessionManager = SessionManager(context.applicationContext)
        }
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val userHeaderInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath
        val isPublicEndpoint = path.startsWith("/api/auth") || path.startsWith("/api/categories")
        val usuarioId = sessionManager?.getUsuarioId()

        val request = if (!isPublicEndpoint && usuarioId != null) {
            originalRequest.newBuilder()
                .header("X-Usuario-Id", usuarioId.toString())
                .build()
        } else {
            originalRequest
        }

        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(userHeaderInterceptor)
        .addInterceptor(logging)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }
}
