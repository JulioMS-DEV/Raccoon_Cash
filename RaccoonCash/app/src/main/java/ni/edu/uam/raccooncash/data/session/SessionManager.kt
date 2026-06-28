package ni.edu.uam.raccooncash.data.session

import android.content.Context

class SessionManager(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveSession(usuarioId: Long, nombre: String, correo: String) {
        preferences.edit()
            .putLong(KEY_USUARIO_ID, usuarioId)
            .putString(KEY_NOMBRE, nombre)
            .putString(KEY_CORREO, correo)
            .apply()
    }

    fun getUsuarioId(): Long? {
        val value = preferences.getLong(KEY_USUARIO_ID, NO_USER_ID)
        return if (value == NO_USER_ID) null else value
    }

    fun getNombre(): String? = preferences.getString(KEY_NOMBRE, null)

    fun getCorreo(): String? = preferences.getString(KEY_CORREO, null)

    fun isLoggedIn(): Boolean = getUsuarioId() != null

    fun clearSession() {
        preferences.edit().clear().apply()
    }

    private companion object {
        const val PREFS_NAME = "raccoon_cash_session"
        const val KEY_USUARIO_ID = "usuario_id"
        const val KEY_NOMBRE = "nombre"
        const val KEY_CORREO = "correo"
        const val NO_USER_ID = -1L
    }
}
