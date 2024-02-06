package table

import java.text.SimpleDateFormat
import java.util.*

/**Utilizado para passar os dados para a tabela;
 * recebe os dados enumerados por tipo;
 * Se o tipo for Data devera ser passado no formato DD/MM/YYYY HH24:MI:SS em formato String;*/
data class Dado private constructor(
    internal var _uid: String = UUID.randomUUID().toString(),
    var valor: Any,
    val tipo: TipoDado,
    val decimais: Int = 2,
    val onChange: (List<Dado>) -> Unit = {}
) {
    val UID: String
        get() = _uid

    init {
        validarValor()
    }

    private fun validarValor() {
        when (tipo) {
            TipoDado.DATA -> {
                if (valor !is String || !isValidDateFormat(valor as String, "dd/MM/yyyy HH:mm:ss")) {
                    throw IllegalArgumentException("Valor inválido para o tipo de dado DATA. Esperado formato dd/MM/yyyy HH:mm:ss")
                }
            }
            // Adicione validações para outros tipos de dado conforme necessário
            TipoDado.TEXTO -> {}
            TipoDado.IMAGEM -> {}
            TipoDado.NUMEROINTEIRO -> {}
            TipoDado.NUMEROFLUTUANTE -> {}
            TipoDado.DATACOMPLETA -> {}
            TipoDado.CHECKBOX -> {}
        }
    }

    companion object {
        fun criarDado(valor: Any, tipo: TipoDado, decimais: Int = 2, onChange: (List<Dado>) -> Unit = {}): Dado {
            val uid = UUID.randomUUID().toString()
            return Dado(uid, valor, tipo, decimais, onChange)
        }

        private fun isValidDateFormat(date: String, format: String): Boolean {
            return try {
                val sdf = SimpleDateFormat(format, Locale.getDefault())
                sdf.isLenient = false
                sdf.parse(date)
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}