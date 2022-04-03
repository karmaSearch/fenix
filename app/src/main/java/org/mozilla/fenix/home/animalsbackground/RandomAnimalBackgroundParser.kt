package org.mozilla.fenix.home.animalsbackground

import mozilla.components.support.ktx.android.org.json.mapNotNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class RandomAnimalBackgroundParser {
    fun jsonToAnimals(json: String): List<AnimalBackground>? = try {
        val rawJSON = JSONObject(json)
        val animalsArray = rawJSON.getJSONArray("images")
        val animals = animalsArray.mapNotNull(JSONArray::getJSONObject) { jsonToAnimalBackground(it) }

        // We return null, rather than the empty list, because devs might forget to check an empty list.
        if (animals.isNotEmpty()) animals else null
    } catch (e: JSONException) {
        null
    }

    private fun jsonToAnimalBackground(json: JSONObject): AnimalBackground =
        AnimalBackground(
            imageName = json.getString("imageName"),
            author = json.getString("author"),
            infoText = json.getString("infoText"),
            url = json.getString("url")
        )

}