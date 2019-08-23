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

    private val tokenizer by lazy {
        loadModelAndMeasure("en-token.bin") { TokenizerME(TokenizerModel(it)) }
    }
    private val personNameFinder by lazy {
        loadModelAndMeasure("en-ner-person.bin") { NameFinderME(TokenNameFinderModel(it)) }
    }
    private val locationNameFinder by lazy {
        loadModelAndMeasure("en-ner-location.bin") { NameFinderME(TokenNameFinderModel(it)) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        log_view.movementMethod = ScrollingMovementMethod()

        btn_load.setOnClickListener {
            Thread {
                log("Loading models...")

                measure("TOTAL LOAD", indent = "") {
                    // Call lazy properties to init them
                    tokenizer
                    personNameFinder
                    locationNameFinder
                }

                log("\n")
            }.start()
        }

        btn_execute.setOnClickListener {
            val text = edit_text.text.toString()
            analyzeText(text)
        }

        btn_clear.setOnClickListener {
            log_view.text = ""
        }
    }

    private fun analyzeText(text: String) {
        Thread {
            log("Analyzing text \"$text\"...")

            measure("TOTAL ANALYZE", indent = "") {
                val tokens = measure("tokenize") { tokenizer.tokenize(text) }
                val personNames = measure("find personNames") { personNameFinder.find(tokens) }
                val locationNames = measure("find locationNames") { locationNameFinder.find(tokens) }

                log("  Tokens: ${tokens.toList()}")
                log("  PersonNames: ${personNames.toList()}")
                log("  LocationNames: ${locationNames.toList()}")
            }

            log("\n")
        }.start()
    }

    private fun <T> loadModelAndMeasure(fileName: String, loader: (InputStream) -> T): T =
            measure("load $fileName") { loadModel(fileName, loader) }

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

    private fun <T> measure(operationName: String, indent: String = "  ", block: () -> T): T {
        val start = System.currentTimeMillis()

        return block().also {
            val execTime = System.currentTimeMillis() - start
            log("${indent}Time to $operationName: ${execTime}ms")
        }
    }

    private fun log(s: String) {
        runOnUiThread {
            log_view.append("$s\n")
        }

        Log.v("OpenNPL", s)
    }
}
