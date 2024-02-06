package table

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import java.awt.Cursor
import java.text.SimpleDateFormat
import java.util.*

var copiador = ""

@Composable
fun TabelaHacker(
    modifier: Modifier = Modifier,
    columnCount: Int,
    columnNames: Array<String>,
    columnWeights: List<Float>,
    columnData: List<List<Dado>>,
    onSelectionChange: (List<Dado>) -> Unit,
    opcIcons: @Composable (() -> Unit)? = null,
    geradorPDF: GeradorPDF = GeradorPDFPadrao,
    geradorExcel: GeradorExcel = GeradorExcelPadrao,
    mostraCabecalho: Boolean = true
) {

    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { focusManager.clearFocus() })
            }
    ) {

        val searchQuery = remember { mutableStateOf("") }
        val sortedColumn = remember { mutableStateOf<Int?>(null) }
        val sortAscending = remember { mutableStateOf(true) }

        val apenasLocalizar = remember { mutableStateOf(false) }

        val indexOfItem = remember { mutableStateOf(0) }
        val atualizador = remember { mutableStateOf(0) }

        val filteredData = if (!apenasLocalizar.value) {
            columnData.filter { rowData ->
                val searchString = buildString {
                    for (dado in rowData) {
                        append(dado.valor.toString())
                    }
                }
                searchString.contains(searchQuery.value, ignoreCase = true)
            }
        } else {
            columnData // Retorna a lista original sem filtro quando apenasLocalizar for true
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")

        val sortedData = if (sortAscending.value) {
            filteredData.sortedWith(compareBy {
                when (it[sortedColumn.value ?: 0].tipo) {
                    TipoDado.NUMEROINTEIRO, TipoDado.NUMEROFLUTUANTE -> it.getOrNull(
                        sortedColumn.value ?: 0
                    )?.valor as? Comparable<*>

                    TipoDado.DATA, TipoDado.DATACOMPLETA -> {
                        val dateStr = it.getOrNull(sortedColumn.value ?: 0)?.valor?.toString() ?: ""
                        dateFormat.parse(dateStr)
                    }

                    else -> it.getOrNull(sortedColumn.value ?: 0)?.valor?.toString() ?: ""
                }
            })
        } else {
            filteredData.sortedWith(compareByDescending {
                when (it[sortedColumn.value ?: 0].tipo) {
                    TipoDado.NUMEROINTEIRO, TipoDado.NUMEROFLUTUANTE -> it.getOrNull(
                        sortedColumn.value ?: 0
                    )?.valor as? Comparable<*>

                    TipoDado.DATA, TipoDado.DATACOMPLETA -> {
                        val dateStr = it.getOrNull(sortedColumn.value ?: 0)?.valor?.toString() ?: ""
                        dateFormat.parse(dateStr)
                    }

                    else -> it.getOrNull(sortedColumn.value ?: 0)?.valor?.toString() ?: ""
                }
            })
        }


        val selectedIndex = rememberSaveable { mutableStateOf<Int?>(null) }

        val columnWidths = remember { mutableStateListOf(*columnWeights.toTypedArray()) }

        if (mostraCabecalho) {
            criaCabecalho(
                searchQuery = searchQuery,
                apenasLocalizar = apenasLocalizar,
                sortedData = sortedData,
                indexOfItem = indexOfItem,
                selectedIndex = selectedIndex,
                columnNames = columnNames,
                columnWeights = columnWeights,
                opcIcons = opcIcons,
                geradorPDF = geradorPDF,
                geradorExcel = geradorExcel
            )
        }

        criaTitleColumn(
            columnCount = columnCount,
            columnWidths = columnWidths,
            sortedColumn = sortedColumn,
            sortAscending = sortAscending,
            columnNames = columnNames
        )

        criaTabela(
            apenasLocalizar = apenasLocalizar,
            sortedData = sortedData,
            selectedIndex = selectedIndex,
            focusManager = focusManager,
            columnCount = columnCount,
            columnWidths = columnWidths,
            modifier = Modifier.weight(1f),
            indexOfItem = indexOfItem,
            onSelectionChange
        )

        criaRodape(searchQuery = searchQuery, data = columnData, filteredData = filteredData)

    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun criaCabecalho(
    searchQuery: MutableState<String>,
    apenasLocalizar: MutableState<Boolean>,
    sortedData: List<List<Dado>>,
    indexOfItem: MutableState<Int>,
    selectedIndex: MutableState<Int?>,
    columnNames: Array<String>,
    columnWeights: List<Float>,
    opcIcons: @Composable() (() -> Unit)?,
    geradorPDF: GeradorPDF = GeradorPDFPadrao,
    geradorExcel: GeradorExcel
) {


    val arrowUpIcon = Icons.Filled.ArrowDropUp
    val arrowDownIcon = Icons.Default.ArrowDropDown

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.Search, contentDescription = "Pesquisar")
        Spacer(modifier = Modifier.width(8.dp))



        Row(modifier = Modifier.weight(1f)) {

            val listIndexItem = remember { mutableStateListOf<Int>() }

            if (apenasLocalizar.value) {
                LaunchedEffect(searchQuery.value) {
                    listIndexItem.clear()
                    listIndexItem.addAll(sortedData.indices.filter { index ->
                        val searchData = sortedData[index]
                        searchData.any { dado ->
                            dado.valor.toString().contains(searchQuery.value, ignoreCase = true)
                        }
                    })
                    if (listIndexItem.size > 0)
                        indexOfItem.value = listIndexItem[0]
                    else
                        selectedIndex.value = null
                }
            }

            OutlinedTextField(
                value = searchQuery.value,
                onValueChange = {
                    searchQuery.value = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Pesquisar") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                trailingIcon = {
                    if (apenasLocalizar.value && listIndexItem.size > 1) {
                        Row {

                            IconButton(onClick = {
                                val currentIndex = indexOfItem.value
                                val prevIndex = listIndexItem.indexOfLast { it < currentIndex }

                                if (prevIndex != -1) {
                                    // Se houver um índice anterior, atualize para ele.
                                    indexOfItem.value = listIndexItem[prevIndex]
                                } else {
                                    // Se não houver índice anterior, pule para o último índice.
                                    indexOfItem.value = listIndexItem.last()
                                }
                            }) {
                                Icon(imageVector = arrowUpIcon, contentDescription = "Enviar")
                            }

                            IconButton(onClick = {

                                val currentIndex = indexOfItem.value
                                val nextIndex = listIndexItem.indexOfFirst { it > currentIndex }

                                if (nextIndex != -1) {
                                    // Se houver um próximo índice, atualize para ele.
                                    indexOfItem.value = listIndexItem[nextIndex]
                                } else {
                                    // Se não houver próximo índice, volte ao início da lista.
                                    indexOfItem.value = listIndexItem.first()
                                }

                            }) {
                                Icon(imageVector = arrowDownIcon, contentDescription = "Enviar")
                            }


                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.width(2.dp))

        TooltipArea(
            tooltip = {
                // composable tooltip content
                Surface(
                    modifier = Modifier.shadow(4.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Apenas Localizar",
                        modifier = Modifier.padding(10.dp)
                    )
                }
            },
            delayMillis = 600
        ) {
            Checkbox(
                checked = apenasLocalizar.value,
                onCheckedChange = { apenasLocalizar.value = !apenasLocalizar.value })
        }


        Spacer(modifier = Modifier.width(2.dp))
        // opções dos botões

        Box {
            var showMenu by remember { mutableStateOf(false) }

            IconButton(onClick = { showMenu = true }) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Enviar")

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        onClick = {
                            geradorPDF.criarPDF(
                                resultados = sortedData,
                                campos = columnNames,
                                tamanhosColunas = columnWeights,
                                isPrint = true,
                            )
                            showMenu = false
                        },
                        modifier = Modifier.background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Imprimir"
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Imprimir", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface)
                    }


                    DropdownMenuItem(
                        onClick = {
                            geradorPDF.criarPDF(
                                resultados = sortedData,
                                campos = columnNames,
                                tamanhosColunas = columnWeights,
                                isPrint = false
                            )
                            showMenu = false

                        },
                        modifier = Modifier.background(androidx.compose.material3.MaterialTheme.colorScheme.surface),

                        ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Arquivo PDF"
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Arquivo PDF", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface)
                    }

                    DropdownMenuItem(
                        onClick = {
                            geradorExcel.criarExcel(
                                resultados = sortedData,
                                campos = columnNames,
                                tamanhosColunas = columnWeights,
                                isPrint = false
                            )
                            showMenu = false
                        },
                        modifier = Modifier.background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = "Planilha"
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Planilha", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(2.dp))

        if (opcIcons != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                opcIcons()
            }
        }


//        val navigator = LocalNavigator.currentOrThrow
//
//        atualizar {
//            atualizador.value++
//            searchQuery.value = ""
//            sortedColumn.value = null
//            sortAscending.value = true
//        }
//
//        voltar { navigator.pop() }

    }

    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
internal fun criaTitleColumn(
    columnCount: Int,
    columnWidths: SnapshotStateList<Float>,
    sortedColumn: MutableState<Int?>,
    sortAscending: MutableState<Boolean>,
    columnNames: Array<String>
) {
    Row(modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)) {
        for (i in 0 until columnCount) {
            val weight = columnWidths[i]
            if (weight > 0f) {
                Box(
                    modifier = Modifier
                        .weight(columnWidths[i])
                        .height(48.dp)
                        .clickable {
                            if (sortedColumn.value == i) sortAscending.value = !sortAscending.value
                            else {
                                sortedColumn.value = i
                                sortAscending.value = true
                            }
                        }
                ) {
                    Row(
                        Modifier.fillMaxSize().padding(start = 4.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        androidx.compose.material3.Text(columnNames[i], modifier = Modifier.weight(1f))

                        if (sortedColumn.value == i) {
                            androidx.compose.material3.Icon(
                                imageVector =
                                if (sortAscending.value) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = null
                            )
                        }
                    }
                }


                if (i < columnCount - 1) {
                    Box(contentAlignment = Alignment.Center,
                        modifier = Modifier.width(5.dp)
                            .pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
                            .draggable(
                                orientation = Orientation.Horizontal,
                                state = rememberDraggableState { delta ->
                                    val newWidth = columnWidths[i] + delta / 5 // Ajuste a velocidade aqui
                                    if (newWidth >= 1f) { // Limit the minimum width
                                        columnWidths[i] = newWidth
                                    }

                                }
                            )) {
                        androidx.compose.material3.Divider(
                            modifier = Modifier.height(48.dp).width(1.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun criaTabela(
    apenasLocalizar: MutableState<Boolean>,
    sortedData: List<List<Dado>>,
    selectedIndex: MutableState<Int?>,
    focusManager: FocusManager,
    columnCount: Int,
    columnWidths: SnapshotStateList<Float>,
    modifier: Modifier = Modifier,
    indexOfItem: MutableState<Int>,
    onSelectionChange: (List<Dado>) -> Unit
) {

    val state = rememberLazyListState()

    if (apenasLocalizar.value) {

        if (indexOfItem.value >= 0) {
            LaunchedEffect(indexOfItem.value) {
                state.animateScrollToItem(indexOfItem.value)
            }
            selectedIndex.value = indexOfItem.value
        }
    }


    Box(modifier = modifier) {


//        val lis = remember { mutableStateListOf<List<Dado>>() }
//
//        sortedData.filter { item -> item !in lis }.forEach {item->
//            println(item)
//        }

        val ouvinte = LocalWindowInfo.current.keyboardModifiers

        ouvinte.isCtrlPressed


        LazyColumn(state = state) {
            itemsIndexed(sortedData) { index, item ->


                val backgroundColor = when {
                    index == selectedIndex.value -> MaterialTheme.colorScheme.inversePrimary
                    index % 2 == 0 -> Color.Transparent
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                val boxMaxHeight = remember { mutableStateOf(0.dp) }
                Row(
                    modifier = Modifier.background(backgroundColor)
                        .onGloballyPositioned { coordinates ->
                            boxMaxHeight.value = coordinates.size.height.dp
                        }
                        .combinedClickable(
                            onClick = {
                                onSelectionChange(item)
                                selectedIndex.value = if (index == selectedIndex.value) null else index
                                focusManager.clearFocus()
                            }),
                    verticalAlignment = Alignment.CenterVertically
                ) {



                    for (i in 0 until columnCount) {

                        val weight = columnWidths[i]
                        if (weight > 0f) {

                            Box(
                                modifier = Modifier.weight(columnWidths[i]).heightIn(min = 27.dp)
                                    .onClick {
                                        copiador = item[i].valor.toString()
                                        onSelectionChange(item)
                                        selectedIndex.value = if (index == selectedIndex.value) null else index
                                        focusManager.clearFocus()
                                    },
                                contentAlignment =
                                if (item[i].tipo in setOf(
                                        TipoDado.NUMEROINTEIRO,
                                        TipoDado.NUMEROFLUTUANTE,
                                        TipoDado.DATA,
                                        TipoDado.IMAGEM,
                                        TipoDado.CHECKBOX
                                    )
                                ) Alignment.Center
                                else Alignment.CenterStart
                            ) {


                                val style = MaterialTheme.typography.bodySmall

                                when {
                                    item[i].tipo == TipoDado.NUMEROFLUTUANTE -> {
                                        val numero = item[i].valor.toString().toDouble()
                                        val numeroFormatado = String.format("%.${item[i].decimais}f", numero)


                                        Text(
                                            text = numeroFormatado,
                                            style = style,
                                            modifier = Modifier.padding(start = 4.dp),
                                        )
                                    }

                                    item[i].tipo == TipoDado.IMAGEM -> {
//                                        Image(
//                                            imageVector = item[i].valor as ImageVector, contentDescription = "Enviar"
//                                        )
                                    }

                                    item[i].tipo == TipoDado.DATA -> {
                                        val formato = SimpleDateFormat("dd/MM/yyyy")

                                        val data = Date()

                                        val dataFormatada = formato.format(data)

                                        Text(
                                            text = dataFormatada,
                                            style = style,
                                            modifier = Modifier.padding(start = 4.dp),
                                        )
                                    }

                                    item[i].tipo == TipoDado.CHECKBOX -> {
                                        val valor = remember(item[i].UID) { mutableStateOf(item[i].valor as Boolean) }
                                        Checkbox(checked = valor.value, onCheckedChange = {
                                            item[i].onChange(item)
                                            item[i].valor = !valor.value
                                            valor.value = !valor.value
                                        })
                                    }

                                    else -> {
                                        Text(
                                            text = item[i].valor.toString(),
                                            style = style,
                                            modifier = Modifier.padding(start = 4.dp),
                                        )
                                    }
                                }


                            }

                            if (i < columnCount - 1) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.width(5.dp)
                                ) {
                                    androidx.compose.material3.Divider(
                                        modifier = Modifier.height(boxMaxHeight.value).width(1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                androidx.compose.material3.Divider()

            }
        }

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(
                scrollState = state
            ),
            style = ScrollbarStyle(
                minimalHeight = 16.dp,
                thickness = 10.dp,
                shape = RoundedCornerShape(4.dp),
                hoverDurationMillis = 300,
                unhoverColor = MaterialTheme.colorScheme.primaryContainer,
                hoverColor = MaterialTheme.colorScheme.primaryContainer

            )
        )

    }
}

@Composable
internal fun criaRodape(
    searchQuery: MutableState<String>,
    data: List<List<Dado>>,
    filteredData: List<List<Dado>>,
) {
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        val text = if (searchQuery.value.isEmpty()) {
            "Registros: ${data.size}"
        } else {
            "Registros: ${data.size} / Filtrados: ${filteredData.size}"
        }
        androidx.compose.material3.Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
