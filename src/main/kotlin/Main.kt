import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.onClick
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import table.*
import java.awt.SystemColor.text
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import kotlin.random.Random


@Composable
@Preview
fun App() {

    val columnNames =
        arrayOf(
            "Dt Inicio",
            "H. Inicio",
            "Dt Fim",
            "H. Fim",
            "Tempo",
            "Nome",
            "Item",
            "Ordem",
            "Parada",
            "mascara"
        )

    val columnWeights = listOf(0f, 10f, 15f, 10f, 10f, 25f, 25f, 15f, 25f, 25f)

    val columnData = mutableListOf<List<Dado>>()

// Exemplo de valores iniciais para cada campo

    fun gerarConjuntoCaracteres(tamanhoMin: Int, tamanhoMax: Int): String {
        require(tamanhoMin >= 0 && tamanhoMax >= tamanhoMin) { "Tamanhos mínimos e máximos inválidos" }

        val tamanho = Random.nextInt(tamanhoMin, tamanhoMax + 1)
        val caracteresPermitidos =
            ('a'..'z') + ('A'..'Z') + ('0'..'9') // Caracteres permitidos: letras minúsculas, letras maiúsculas e dígitos

        return (1..tamanho)
            .map { caracteresPermitidos.random() }
            .joinToString("")
    }


// Criar a lista de dados para cada campo
    for (i in 1..3000) {
        val initialValues = listOf(
            Dado.criarDado(valor = "12:00", tipo = TipoDado.TEXTO),
            Dado.criarDado(valor = Random.nextBoolean(), tipo = TipoDado.CHECKBOX) { println("print do intem " + it) },
            Dado.criarDado(
                valor = "22/04/2023 ${String.format("%02d", (0..23).random())}:${
                    String.format(
                        "%02d",
                        (0..59).random()
                    )
                }:${String.format("%02d", (0..59).random())}", tipo = TipoDado.DATACOMPLETA
            ),
            Dado.criarDado(valor = "14:30", tipo = TipoDado.TEXTO),
            Dado.criarDado(valor = i * 10, tipo = TipoDado.NUMEROFLUTUANTE),  // Valor numérico para o campo Tempo
            Dado.criarDado(valor = gerarConjuntoCaracteres(4, 10), tipo = TipoDado.TEXTO),
            Dado.criarDado(valor = "Item Exemplo", tipo = TipoDado.TEXTO),
            Dado.criarDado(
                valor = (1..2000).random(),
                tipo = TipoDado.NUMEROINTEIRO
            ),  // Valor numérico para o campo Ordem
            Dado.criarDado(valor = "Parada Exemplo", tipo = TipoDado.TEXTO),
            Dado.criarDado(valor = Icons.Default.Backup, tipo = TipoDado.IMAGEM)
        )
        columnData.add(initialValues)
    }


    val selecionado = remember { mutableStateListOf<Dado>() }


    TabelaHacker(
        modifier = Modifier.fillMaxSize(),
        columnCount = 10,
        columnNames = columnNames,
        columnWeights = columnWeights,
        onSelectionChange = {
            selecionado.clear()
            selecionado.addAll(it)
        },
        columnData = columnData,
        opcIcons = {
            Icon(imageVector = Icons.Default.Backup, contentDescription = "Enviar")
            Icon(imageVector = Icons.Default.Backup, contentDescription = "Enviar")
        },
        mostraCabecalho = true
    )
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
fun main() = application {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard

    Window(onCloseRequest = ::exitApplication, onKeyEvent = {
        if (
            it.isCtrlPressed &&
            it.key == Key.C &&
            it.type == KeyEventType.KeyDown
        ) {
            clipboard.setContents(StringSelection(copiador), null)
            true
        } else {
            false
        }
    }) {

//        App()

        testeJtable()

//        Column(
//            Modifier.background(Color.White),
//            verticalArrangement = Arrangement.spacedBy(10.dp)
//        ) {
//            PhotosGrid()
//        }
    }
}
