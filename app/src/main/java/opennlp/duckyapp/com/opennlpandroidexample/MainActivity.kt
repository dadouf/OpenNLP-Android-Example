package opennlp.duckyapp.com.opennlpandroidexample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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

        // if this work
//        System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");
//        System.setProperty("javax.xml.parsers.SAXParserFactory", "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");

//        System.setProperty()

//        Log.v("BLAH", "Stuff:" + System.getProperty("javax.xml.parsers.DocumentBuilderFactory"))
//
//        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
//        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
//        val b = documentBuilderFactory.newDocumentBuilder()

//        val f = SAXParserFactory.newInstance()
//        f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
//        val b = f.newSAXParser()


        btn_execute.setOnClickListener {
            val text = input_text.text.toString()
            analyzeText(text)
        }
    }

    private fun analyzeText(text: String) {
        Thread {
            Log.v("OpenNLP", "analyzeText(\"$text\")")

            val startTime = System.currentTimeMillis()

            val tokens = tokenizer.tokenize(text)

            Log.i("OpenNLP-Tokens", tokens.toList().toString())
            Log.i("OpenNLP-PersonNames", personNameFinder.find(tokens).toList().toString())
            Log.i("OpenNLP-LocationNames", locationNameFinder.find(tokens).toList().toString())

            val consumeTime = System.currentTimeMillis() - startTime
            Log.d("OpenNLP", "Time to exec: ${consumeTime}ms")
        }.start()
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
