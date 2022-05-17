package org.mozilla.fenix.home.animalsbackground

import mozilla.components.support.ktx.android.org.json.mapNotNull
import mozilla.components.support.locale.LocaleManager
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class RandomAnimalBackgroundParser {
    fun jsonToAnimals(json: String): List<AnimalBackground>? = try {
        val animalsArray = JSONArray(json)
        val animals = animalsArray.mapNotNull(JSONArray::getJSONObject) { jsonToAnimalBackground(it) }

        // We return null, rather than the empty list, because devs might forget to check an empty list.
        if (animals.isNotEmpty()) animals else null
    } catch (e: JSONException) {
        null
    }

    private fun jsonToAnimalBackground(json: JSONObject): AnimalBackground {
        val defaultText = try {
            json.getJSONObject("infoText").getString(LocaleManager.getSystemDefault().language)
        } catch(e: JSONException) {
            json.getJSONObject("infoText").getString("en")
        }

        return AnimalBackground(
            imageName = json.getString("imageName"),
            author = json.getString("author"),
            infoText = defaultText,
            url = json.getString("url")
        )
    }


}