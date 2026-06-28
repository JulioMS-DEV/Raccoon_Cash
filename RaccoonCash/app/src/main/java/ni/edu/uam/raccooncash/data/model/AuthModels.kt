package ni.edu.uam.raccooncash.data.model

data class RegistroSolicitud(
    val nombre: String,
    val correo: String,
    val password: String
)

data class LoginSolicitud(
    val correo: String,
    val password: String
)

data class AuthRespuesta(
    val id: Long,
    val nombre: String,
    val correo: String
)
