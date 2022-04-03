package org.mozilla.fenix.home.animalsbackground

import android.content.Context
import org.mozilla.fenix.R
import java.io.*


class RandomAnimalBackgroundService(val context: Context,
                                    private val jsonParser: RandomAnimalBackgroundParser
) {

    fun getRandomAnimals() : AnimalBackground? {
        val stream: InputStream = context.resources
                .openRawResource(R.raw.animals)
        val writer: Writer = StringWriter()
        val buffer = CharArray(1024)
        try {
            val reader: Reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
            var n: Int
            while (reader.read(buffer).also { n = it } != -1) {
                writer.write(buffer, 0, n)
            }
        } finally {
            stream.close()
        }

        val jsonString: String = writer.toString()
        val animals = jsonParser.jsonToAnimals(jsonString)
        return animals?.random()
    }

}