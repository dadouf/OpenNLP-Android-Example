package opennlp.duckyapp.com.opennlpandroidexample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.EditText
import opennlp.tools.namefind.NameFinderME
import opennlp.tools.namefind.TokenNameFinderModel
import opennlp.tools.tokenize.TokenizerME
import opennlp.tools.tokenize.TokenizerModel
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    private lateinit var inputEt: EditText

    private val tokenizer by lazy {
        loadModel("en-token.bin") { TokenizerME(TokenizerModel(it)) }
    }
    private val personNameFinder by lazy {
        loadModel("en-ner-person.bin") { NameFinderME(TokenNameFinderModel(it)) }
    }
    private val locationNameFinder by lazy {
        loadModel("en-ner-location.bin") { NameFinderME(TokenNameFinderModel(it)) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputEt = findViewById(R.id.inputText)

    }

    @Suppress("UNUSED_PARAMETER") // Listener in XML
    fun onClickButton(view: View) {
        val startTime = System.currentTimeMillis()

        val text = inputEt.text.toString()
        val tokens = tokenizer.tokenize(text)

        Log.i("OpenNLP-Tokens", tokens.toList().toString())
        Log.i("OpenNLP-PersonNames", personNameFinder.find(tokens).toList().toString())
        Log.i("OpenNLP-LocationNames", locationNameFinder.find(tokens).toList().toString())

        val consumeTime = System.currentTimeMillis() - startTime
        Log.d("OpenNLP", "Time to exec: ${consumeTime}ms")
    }

    private fun <T> loadModel(fileName: String, loader: (InputStream) -> T): T {
        val startTime = System.currentTimeMillis()

        try {
            return loader(assets.open(fileName))
        } catch (e: IOException) {
            throw RuntimeException("Error opening model: $fileName", e)

        } finally {
            val consumeTime = System.currentTimeMillis() - startTime
            Log.d("OpenNLP", "Time to load $fileName: ${consumeTime}ms")
        }
    }
}
