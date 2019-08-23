package opennlp.duckyapp.com.opennlpandroidexample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.method.ScrollingMovementMethod
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import opennlp.tools.namefind.NameFinderME
import opennlp.tools.namefind.TokenNameFinderModel
import opennlp.tools.tokenize.TokenizerME
import opennlp.tools.tokenize.TokenizerModel
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    private var tokenizer: TokenizerME? = null
    private fun loadTokenizer() {
        tokenizer = loadModel("en-token.bin") { TokenizerME(TokenizerModel(it)) }
    }

    private var personNameFinder: NameFinderME? = null
    private fun loadPersonsNER() {
        personNameFinder = loadModel("en-ner-person.bin") { NameFinderME(TokenNameFinderModel(it)) }
    }

    private var locationNameFinder: NameFinderME? = null
    private fun loadLocationsNER() {
        locationNameFinder = loadModel("en-ner-location.bin") { NameFinderME(TokenNameFinderModel(it)) }
    }

    private var latestTokens: Array<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        log_view.movementMethod = ScrollingMovementMethod()

        btn_tokenizer_load.setOnClickListener { measure("loadTokenizer") { loadTokenizer() } }
        btn_ner_persons_load.setOnClickListener { measure("loadPersonsNER") { loadPersonsNER() } }
        btn_ner_locations_load.setOnClickListener { measure("loadLocationsNER") { loadLocationsNER() } }

        btn_tokenizer_analyze.setOnClickListener {
            val text = edit_text.text.toString()

            measure("tokenize") {
                latestTokens = tokenizer?.tokenize(text)
                        ?: null.also { log("ERROR: tokenizer not loaded\n") }
            }

            log("Tokens: ${latestTokens?.toList()}")
        }

        btn_ner_persons_analyze.setOnClickListener {
            measure("find person names") {
                val names = personNameFinder?.find(latestTokens)
                        ?: null.also { log("ERROR: personNameFinder not loaded\n") }

                log("PersonNames: ${names?.toList()}")
            }
        }

        btn_ner_locations_analyze.setOnClickListener {
            measure("find location names") {
                val names = locationNameFinder?.find(latestTokens)
                        ?: null.also { log("ERROR: locationNameFinder not loaded\n") }

                log("LocationNames: ${names?.toList()}")
            }
        }

        btn_clear_log.setOnClickListener {
            log_view.text = ""
        }
    }

    private fun <T> loadModel(fileName: String, loader: (InputStream) -> T): T {
        var stream: InputStream? = null

        try {
            stream = assets.open(fileName)
            return loader(stream)

        } catch (e: IOException) {
            throw RuntimeException("Error opening model: $fileName", e)

        } finally {
            stream?.close()
        }
    }

    private fun measure(operationName: String, block: () -> Unit) {
        background {
            val start = System.currentTimeMillis()

            log("START $operationName...")

            block().also {
                val execTime = System.currentTimeMillis() - start
                log("END $operationName in ${execTime}ms\n")
            }
        }
    }

    private fun log(s: String) {
        runOnUiThread {
            log_view.append("$s\n")
        }

        Log.v("OpenNPL", s)
    }

    private fun background(block: () -> Unit) = Thread { block() }.start()
}
