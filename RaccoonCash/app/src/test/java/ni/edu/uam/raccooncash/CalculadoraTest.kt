package ni.edu.uam.raccooncash

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculadoraTest {

    @Test
    fun verificarSuma() {
        val calculadora = Calculadora()
        val resultado = calculadora.sumar(5, 3)
        assertEquals(8, resultado)
    }
}
