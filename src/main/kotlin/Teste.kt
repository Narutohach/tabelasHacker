import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import table.copiador
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.plaf.basic.BasicScrollBarUI
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableModel
import javax.swing.table.TableRowSorter
import kotlin.random.Random

@Composable
fun testeJtable() {

    criTabela()
}

@Composable
fun criTabela() {

    val search = remember { mutableStateOf("") }
    var check by remember { mutableStateOf(false) }

    val corx = MaterialTheme.colorScheme.background.toArgb()
    val cory = MaterialTheme.colorScheme.surfaceVariant.toArgb()
    val tableModel by remember { mutableStateOf(criarJTable(Color(corx), Color(cory))) }

    var linhas = 0

    LaunchedEffect(Unit) {
        linhas = tableModel.rowCount
    }

    Surface {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Pesquisar")
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = search.value,
                    onValueChange = { search.value = it },
                    label = { Text("Pesquisar") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(2.dp))

                Checkbox(
                    checked = check,
                    onCheckedChange = { check = !check }
                )

                Spacer(modifier = Modifier.width(2.dp))

//                IconButton(onClick = { criarTabelaPDF(tableModel, false) }) {
//                    Icon(imageVector = Icons.Default.Send, contentDescription = "Pesquisar")
//                }

            }

            Spacer(modifier = Modifier.height(10.dp))

            SwingPanel(
                modifier = Modifier.fillMaxSize().weight(1f),
                factory = {
                    CustomJScrollPane(tableModel)
                },
                update = {
                    val searchText = search.value.trim()
                    if (!check) {

                        val rowSorter = tableModel.rowSorter as? TableRowSorter<*>
                        rowSorter?.let {

                            if (searchText.isEmpty()) {

                                it.rowFilter = null
                            } else {

                                it.setRowFilter(RowFilter.regexFilter("(?i)$searchText"))
                            }
                        }
                    } else {
                        findItemAndHighlight(searchText, tableModel)
                    }

                }
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                val text = if (search.value.isEmpty()) {
                    "Registros: ${tableModel.rowCount}"
                } else {
                    "Registros: $linhas / Filtrados: ${tableModel.rowCount}"
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }


        }
    }

}


fun gerarConjuntoCaracteres(tamanhoMin: Int, tamanhoMax: Int): String {
    require(tamanhoMin >= 0 && tamanhoMax >= tamanhoMin) { "Tamanhos mínimos e máximos inválidos" }

    val tamanho = Random.nextInt(tamanhoMin, tamanhoMax + 1)
    val caracteresPermitidos =
        ('a'..'z') + ('A'..'Z') + ('0'..'9') // Caracteres permitidos: letras minúsculas, letras maiúsculas e dígitos

    return (1..tamanho)
        .map { caracteresPermitidos.random() }
        .joinToString("")
}


fun criarDados(numLinhas: Int, numColunas: Int): Array<Array<String>> {
    val data = Array(numLinhas) { Array(numColunas) { "" } }

    for (i in 0 until numLinhas) {
        for (j in 0 until numColunas) {
            if (numColunas == 3) {
                data[i][j] = (1..numLinhas).random().toString()
            } else {
                data[i][j] = gerarConjuntoCaracteres((1..5).random(), (5..1500).random())
            }
        }
    }

    return data
}

fun criarJTable(cor: Color, color: Color): JTable {
    val columnNames =
        arrayOf("Coluna 1", "Coluna 2", "Coluna 3", "Coluna 4", "Coluna 5", "Coluna 6", "Coluna 7", "Coluna 8")
    val data = criarDados(50000, 8) // Inicialmente carrega 500 linhas
    val tableModel = DefaultTableModel(data, columnNames)
    val table = CustomJTable(tableModel)



    table.setDefaultEditor(Any::class.java, null)

    val rowSorter = TableRowSorter<TableModel>(tableModel)
    table.rowSorter = rowSorter

    table.isFocusable = false
//    table.autoResizeMode = JTable.AUTO_RESIZE_OFF

    val columnModel = table.columnModel
    val firstColumn = columnModel.getColumn(0)
    firstColumn.preferredWidth = 5




    // Cria um renderizador de células personalizado para alternar as cores de fundo das linhas
    val alternatingRowRenderer = object : DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(
            table: JTable?,
            value: Any?,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int
        ): Component {
            val component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
            val label = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) as JLabel



            table?.setRowHeight(row, 80)

            border = noFocusBorder

            // Defina a cor de fundo alternada
            if (isSelected) {
                component.background = Color(0, 188, 89, 50)
            } else {
                if (row % 2 == 0) {
                    component.background = color
                } else {
                    component.background = cor
                }
            }

            // Defina o alinhamento do texto para a primeira coluna
            if (column == 0) {
                label.text = value.toString()
                label.horizontalAlignment = SwingConstants.CENTER
                label.toolTipText = "Tooltip para a terceira coluna"
                return label
            } else {
                horizontalAlignment = SwingConstants.LEFT
                border = BorderFactory.createEmptyBorder(0, 5, 0, 0)
            }

            // Ajuste a altura da linha com base no conteúdo
            if (table != null) {
                val rowHeight = table.getRowHeight(row)
                val preferredHeight = label.preferredSize.height
                if (preferredHeight > rowHeight) {
                    table.setRowHeight(row, preferredHeight)
                }
            }

            return component
        }
    }


    // Define o renderizador de células personalizado para a tabela
    table.setDefaultRenderer(Any::class.java, alternatingRowRenderer)

    table.actionMap.put("copy", object : AbstractAction(
    ) {
        override fun actionPerformed(e: ActionEvent) {
            val cellValue = table.model.getValueAt(table.selectedRow, table.selectedColumn).toString()
            val stringSelection = StringSelection(cellValue)
            Toolkit.getDefaultToolkit().systemClipboard.setContents(stringSelection, stringSelection)
        }
    })



    return table
}

fun findItemAndHighlight(searchText: String, table: JTable) {
    val rowCount = table.rowCount
    val columnCount = table.columnCount
    var found = false

    for (row in 0 until rowCount) {
        for (column in 0 until columnCount) {
            val cellValue = table.getValueAt(row, column)?.toString() ?: ""
            if (cellValue.contains(searchText, ignoreCase = true)) {
                table.changeSelection(row, column, false, false)
                found = true
                break
            }
        }
        if (found) {
            break
        }
    }
}


class CustomJTable(model: TableModel) : JTable(model) {
    init {
        // Adicione um Action personalizado para a cópia de células
        val copyCellAction = object : AbstractAction("Copy Cell") {
            override fun actionPerformed(e: ActionEvent?) {
                val row = selectedRow
                val column = selectedColumn
                if (row >= 0 && column >= 0) {
                    val value = getValueAt(row, column)
                    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                    clipboard.setContents(StringSelection(value?.toString()), null)
                }
            }
        }


        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                val row: Int = rowAtPoint(e.getPoint())
                val column: Int = columnAtPoint(e.getPoint())
                if (row >= 0 && column >= 0) {
                    val value: Any = getValueAt(row, column)
                    copiador = value.toString()
                }
            }
        })




        setShowGrid(false)
        showVerticalLines = true
        showHorizontalLines = false

        rowHeight = 30
        tableHeader.preferredSize = Dimension(width, 40)
        tableHeader.isOpaque = false
        tableHeader.background = Color(0, 188, 89, 50)

        // Associe o Action ao atalho Ctrl+C
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("control C"), "copyCell")
        actionMap.put("copyCell", copyCellAction)

    }
}

class CustomJScrollPane(view: JTable) : JScrollPane(view) {
    init {

        verticalScrollBar.preferredSize = Dimension(20, 10)

        getVerticalScrollBar().setUI(object : BasicScrollBarUI() {
            override fun configureScrollBarColors() {
                this.thumbColor = Color(0, 188, 89, 50)
            }

            override fun createDecreaseButton(orientation: Int): JButton {
                return createZeroButton()
            }

            override fun createIncreaseButton(orientation: Int): JButton {
                return createZeroButton()
            }

            private fun createZeroButton(): JButton {
                val jbutton = JButton()
                jbutton.preferredSize = Dimension(0, 0)
                jbutton.minimumSize = Dimension(0, 0)
                jbutton.maximumSize = Dimension(0, 0)
                return jbutton
            }
        })


    }
}