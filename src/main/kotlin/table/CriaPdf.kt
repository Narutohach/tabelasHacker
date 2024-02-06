package table

import androidx.compose.ui.graphics.vector.ImageVector
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.events.Event
import com.itextpdf.kernel.events.IEventHandler
import com.itextpdf.kernel.events.PdfDocumentEvent
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.printing.PDFPageable
import java.awt.Desktop
import java.awt.print.PrinterJob
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface GeradorPDF {
    fun criarPDF(
        resultados: List<List<Dado>>,
        campos: Array<String>,
        tamanhosColunas: List<Float>,
        isPrint: Boolean,
        titulo: String = ""
    )
}

object GeradorPDFPadrao : GeradorPDF {

    override fun criarPDF(
        resultados: List<List<Dado>>,
        campos: Array<String>,
        tamanhosColunas: List<Float>,
        isPrint: Boolean,
        titulo: String
    ) {
        class HeaderFooterHandler(val img: Image) : IEventHandler {
            override fun handleEvent(event: Event) {
                val docEvent = event as PdfDocumentEvent
                val pdfDoc = docEvent.document
                val page = docEvent.page
                val pageNumber = pdfDoc.getPageNumber(page)
                val pageSize = page.pageSize
                val pdfCanvas = PdfCanvas(page.newContentStreamBefore(), page.resources, pdfDoc)
                val canvas = com.itextpdf.layout.Canvas(pdfCanvas, pageSize, true)

                // Adicionar imagem ao lado esquerdo do rodapé
                val imageX = pageSize.left + 30
                val imageY = pageSize.bottom
                canvas.add(img.scaleToFit(50f, 50f).setFixedPosition(imageX, imageY))

                // Adicionar título centralizado na página no cabeçalho

                val titleX = (pageSize.left + pageSize.right) / 2
                val titleY = pageSize.top - 30
                canvas.setFontSize(14f).showTextAligned(titulo, titleX, titleY, TextAlignment.CENTER)
                    .setBold()

                // Adicionar data e hora atual ao centro do rodapé
                val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val currentDateTime = LocalDateTime.now().format(dateTimeFormatter)
                val dateTimeX = (pageSize.left + pageSize.right) / 2
                val dateTimeY = pageSize.bottom + 15
                canvas.setFontSize(10f).showTextAligned(currentDateTime, dateTimeX, dateTimeY, TextAlignment.CENTER)

                // Adicionar numeração de página à direita do rodapé
                val pageNumberX = pageSize.right - 30
                val pageNumberY = pageSize.bottom + 15
                canvas.setFontSize(10f)
                    .showTextAligned(pageNumber.toString(), pageNumberX, pageNumberY, TextAlignment.RIGHT)
            }
        }

        // Gerar o nome do arquivo PDF usando a data e hora atual
        val dataHoraAtual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
        val nomeArquivo = "Hach-$dataHoraAtual.pdf"

        val classLoader = Thread.currentThread().contextClassLoader
        val imageUrl = classLoader.getResource("images/hacker.png")
        val imgData = ImageDataFactory.create(imageUrl)
        val img = Image(imgData)

        // Obter o caminho da pasta temp do sistema operacional
        val tempDir = System.getProperty("java.io.tmpdir")

        val directory = File(tempDir)
        val files = directory.listFiles { file -> file.name.startsWith("Hach-") }

        for (file in files!!) {
            file.delete()
        }

        val caminhoArquivo = "$tempDir/$nomeArquivo"

        // Criar um documento PDF
        val writer = PdfWriter(caminhoArquivo)
        val pdf = PdfDocument(writer)
        pdf.addEventHandler(PdfDocumentEvent.START_PAGE, HeaderFooterHandler(img))
        val document = Document(pdf, PageSize.A4.rotate())
        document.setMargins(36f, 36f, 46f, 36f)

        // Criar a tabela
        val table = Table(UnitValue.createPercentArray(tamanhosColunas.filter { it > 0f }.toFloatArray()))

        // Adicionar o cabeçalho da tabela
        campos.forEachIndexed { index, campo ->

            if (tamanhosColunas[index] > 0f) {
                val headerCell =
                    Cell().setBackgroundColor(DeviceRgb(122, 157, 84))
                        .add(Paragraph(campo.uppercase()).setFontSize(10f))
                table.addHeaderCell(headerCell)
            }
        }


        // Adicionar os dados da tabela
        resultados.forEachIndexed {index, resultado ->
            val cor = when {
                index % 2 == 0 -> DeviceRgb(212, 226, 212)
                else -> DeviceRgb(255, 255, 255)
            }

            resultado.forEachIndexed { indexx, linha ->
                if (tamanhosColunas[indexx] > 0f) {
                    if (linha.tipo == TipoDado.NUMEROINTEIRO) {
                        table.addCell(
                            Cell().setBackgroundColor(cor).add(
                                Paragraph(
                                    linha.valor.toString()
                                ).setFontSize(7f)
                            )
                        )
                    } else if (linha.tipo == TipoDado.IMAGEM) {
//                val image = it.valor as ImageVector


                        table.addCell(
                            Cell().setBackgroundColor(cor).add(
                                Paragraph(
                                    linha.valor.toString()
                                ).setFontSize(7f)
                            )
                        )
                    } else {
                        table.addCell(
                            Cell().setBackgroundColor(cor).add(
                                Paragraph(
                                    linha.valor.toString()
                                ).setFontSize(7f)
                            )
                        )
                    }
                }

            }


        }

// Adicionar a tabela ao documento PDF e fechar o documento
        document.add(table.setHorizontalAlignment(HorizontalAlignment.CENTER))
        document.close()

// Abrir o arquivo PDF com o programa padrão do sistema ou imprimir o arquivo
        if (isPrint) {
// Chamar a função printFile para imprimir o arquivo (não definida no código fornecido)
            printFile(caminhoArquivo)
        } else {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(File(caminhoArquivo))
            }
        }
    }
}


internal fun printFile(fileName: String) {
    // Carregar o arquivo PDF
    val document = PDDocument.load(File(fileName))

    // Criar um objeto PrinterJob
    val printJob = PrinterJob.getPrinterJob()

    // Definir o documento a ser impresso
    printJob.setPageable(PDFPageable(document))

    // Exibir a caixa de diálogo de impressão
    if (printJob.printDialog()) {
        // Imprimir o documento no serviço de impressão selecionado
        printJob.print()
    }

    // Fechar o documento
    document.close()
}