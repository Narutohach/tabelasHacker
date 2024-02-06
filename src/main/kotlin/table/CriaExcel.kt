package table

import androidx.compose.runtime.Composable
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.awt.Desktop
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter



interface GeradorExcel {
    fun criarExcel(
        resultados: List<List<Dado>>,
        campos: Array<String>,
        tamanhosColunas: List<Float>,
        isPrint: Boolean,
        titulo: String = ""
    )
}

object GeradorExcelPadrao : GeradorExcel {

    override fun criarExcel(
        resultados: List<List<Dado>>,
        campos: Array<String>,
        tamanhosColunas: List<Float>,
        isPrint: Boolean,
        titulo: String
    ) {

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Dados")

        val boldFont: XSSFFont = workbook.createFont() as XSSFFont
        boldFont.bold = true
        val boldStyle: XSSFCellStyle = workbook.createCellStyle() as XSSFCellStyle
        boldStyle.setFont(boldFont)

        val headerRow = sheet.createRow(0)

        var colNum = 0

        campos.forEachIndexed { index, campo ->

            if (tamanhosColunas[index] > 0f) {
                val cell = headerRow.createCell(colNum++)
                cell.setCellValue(campo.uppercase())
                cell.cellStyle = boldStyle
            }
        }

        var rowNum = 1
        resultados.forEachIndexed { index, resultado ->

            val row = sheet.createRow(rowNum++)
            colNum = 0

            resultado.forEachIndexed { i, it ->
                if (tamanhosColunas[i] > 0f) {
                    if (it.tipo in setOf(TipoDado.NUMEROINTEIRO, TipoDado.NUMEROFLUTUANTE)) {
                        row.createCell(colNum).setCellValue(it.valor.toString().toDouble())
                    } else if (it.tipo == TipoDado.IMAGEM) {
//                val image = it.valor as ImageVector
                        row.createCell(colNum).setCellValue(it.valor.toString())
                    } else {
                        row.createCell(colNum).setCellValue(it.valor.toString())
                    }
                    colNum++
                }


            }

        }

        for (i in campos.indices) {
            sheet.autoSizeColumn(i)
        }


        val dataHoraAtual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
        val nomeArquivo = "Hach-$dataHoraAtual.xlsx"

        val tempDir = System.getProperty("java.io.tmpdir")

        val directory = File(tempDir)
        val files = directory.listFiles { file -> file.name.startsWith("Hach-") }

        for (file in files!!) {
            file.delete()
        }

        val caminhoArquivo = "$tempDir/$nomeArquivo"

        // Salvar a planilha em um arquivo
        val file = File(caminhoArquivo)
        FileOutputStream(file).use { outputStream ->
            workbook.write(outputStream)
        }

        // Abrir o arquivo com o programa padrão do sistema operacional
        Desktop.getDesktop().open(file)

    }
}