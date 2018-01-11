import javafx.application.Application
import javafx.concurrent.Task
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.io.File
import java.io.PrintWriter
import java.net.URLEncoder
import java.io.StringWriter



class Main : Application() {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Application.launch(Main::class.java)
        }
    }

    override fun start(primaryStage: Stage) {
        val rows = VBox()
        val webAddressTextField = TextField("https://www.amazon.com")
        rows.children.add(webAddressTextField)
        val textArea = TextArea()
        rows.children.add(textArea)
        VBox.setVgrow(textArea, Priority.ALWAYS)
        val button = Button("run")
        rows.children.add(button)


        primaryStage.title = "Amazon scraper"
        primaryStage.scene = Scene(rows, 400.0, 400.0)
        primaryStage.show()


        button.onAction = EventHandler {
            val task = object : Task<Any>() {
                override fun call() {
                    button.isDisable = true
                }
                override fun run() {
                    try {
                        button.isDisable = true
                        val inputFile = File("svitrkodi.txt")
                        val searchCodes = inputFile.readLines()
                        File("cenas.csv").printWriter().use { outFile ->
                            searchCodes.forEach {
                                searchString(it, webAddressTextField.text, textArea, outFile)
                            }
                        }
                        textArea.appendText("Done\n")
                    } catch (e: Exception) {
                        val sw = StringWriter()
                        e.printStackTrace(PrintWriter(sw))
                        val exceptionAsString = sw.toString()
                        textArea.appendText("$exceptionAsString\n")
                    }
                    button.isDisable = false
                }
            }
            Thread(task).start()
        }
    }

    fun searchString(str: String, webAddress: String, textArea: TextArea, outFile: PrintWriter) {
        val encodedStr = URLEncoder.encode(str, "UTF-8")
        val queryStr = "$webAddress/s/ref=nb_sb_noss?url=search-alias%3Daps&field-keywords=$encodedStr"
        textArea.appendText("$queryStr\n")

        val doc = Jsoup
                .connect(queryStr)
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.38 Safari/537.36")
                .get()
        val resultPane = doc.getElementById("s-results-list-atf")
        val itemsFound: Elements? = resultPane?.getElementsByTag("li")
        if (itemsFound == null || itemsFound.isEmpty()) {
            outFile.println("$str,0,,")
        } else {
            val price = itemsFound.first().select("span.s-price,span.a-offscreen")?.last()?.text()
            val title = itemsFound.first().select("h2.s-access-title")?.last()?.text()
            outFile.println("$str,1,$price,$title")
        }
    }
}