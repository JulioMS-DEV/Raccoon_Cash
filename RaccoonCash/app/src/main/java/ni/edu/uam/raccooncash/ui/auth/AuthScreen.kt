package ni.edu.uam.raccooncash.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ni.edu.uam.raccooncash.data.model.AuthRespuesta
import ni.edu.uam.raccooncash.data.model.LoginSolicitud
import ni.edu.uam.raccooncash.data.model.RegistroSolicitud
import ni.edu.uam.raccooncash.data.repository.RaccoonRepository
import ni.edu.uam.raccooncash.data.session.SessionManager
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

@Composable
fun AuthScreen(
    sessionManager: SessionManager,
    onAuthenticated: (AuthRespuesta) -> Unit
) {
    val repository = remember { RaccoonRepository() }
    val scope = rememberCoroutineScope()

    var isRegisterMode by remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun submit() {
        if (isLoading) return

        errorMessage = null
        val trimmedNombre = nombre.trim()
        val trimmedCorreo = correo.trim()
        val trimmedPassword = password.trim()

        if (trimmedCorreo.isBlank() || trimmedPassword.isBlank() || (isRegisterMode && trimmedNombre.isBlank())) {
            errorMessage = "Completa todos los campos obligatorios"
            return
        }

        scope.launch {
            isLoading = true
            try {
                val response = if (isRegisterMode) {
                    repository.registrarUsuario(
                        RegistroSolicitud(
                            nombre = trimmedNombre,
                            correo = trimmedCorreo,
                            password = trimmedPassword
                        )
                    )
                } else {
                    repository.loginUsuario(
                        LoginSolicitud(
                            correo = trimmedCorreo,
                            password = trimmedPassword
                        )
                    )
                }

                sessionManager.saveSession(response.id, response.nombre, response.correo)
                onAuthenticated(response)
            } catch (e: HttpException) {
                errorMessage = authHttpErrorMessage(e, isRegisterMode)
            } catch (e: IOException) {
                errorMessage = "No se pudo conectar con el servidor"
            } catch (e: Exception) {
                errorMessage = "No se pudo conectar con el servidor"
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF080B14),
                        Color(0xFF111827),
                        Color(0xFF1E1B4B)
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF171C2A).copy(alpha = 0.96f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Raccoon Cash",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp
                )
                Text(
                    text = if (isRegisterMode) "Crea tu usuario para empezar limpio" else "Inicia sesion para ver tus finanzas",
                    color = Color(0xFFB5A9D4),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (isRegisterMode) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = Color(0xFFFFA5A5),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = { submit() },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA78BFA))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (isRegisterMode) "Registrarse" else "Entrar",
                            color = Color(0xFF111827),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                TextButton(
                    onClick = {
                        isRegisterMode = !isRegisterMode
                        errorMessage = null
                    },
                    enabled = !isLoading
                ) {
                    Text(
                        text = if (isRegisterMode) "¿Ya tienes cuenta? Iniciar sesión" else "¿No tienes cuenta? Registrarse",
                        color = Color(0xFFE1D5F9)
                    )
                }
            }
        }
    }
}

private fun authHttpErrorMessage(exception: HttpException, isRegisterMode: Boolean): String {
    val backendMessage = exception.response()?.errorBody()?.string()?.let { body ->
        runCatching { JSONObject(body).optString("message") }.getOrNull()
            ?.takeIf { it.isNotBlank() }
    }

    if (!isRegisterMode) {
        return "Correo o contraseña incorrectos"
    }

    return backendMessage ?: "No se pudo registrar el usuario"
}
