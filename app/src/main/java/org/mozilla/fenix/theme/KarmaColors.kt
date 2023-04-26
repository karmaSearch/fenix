package org.mozilla.fenix.theme

import androidx.compose.ui.graphics.Color
import karma.service.learnandact.LearnAndActContentType
import org.mozilla.fenix.R

object KarmaColors {
    val Green50 = Color(0xFF2BCD6D)
    val LightGrey30 = Color(0xFFE1E1E1)
    val DarkGrey30 = Color(0xFF58585B)
    val DarkGrey80 = Color(0xFF16161A)
    val DarkGrey70 = Color(0xFF2E2E30)


    fun contentTypeColor(type: LearnAndActContentType): Color = when (type) {
            LearnAndActContentType.NEWS -> Color(0xE5D9FD)
            LearnAndActContentType.VICTORY -> Color(0xFFFEF2D6)
            LearnAndActContentType.ACT -> Color(0xFFFFDDCC)
            LearnAndActContentType.LEARN -> Color(0xFFD4FCD4)
            LearnAndActContentType.UNDEFINED -> Color(0xFFFFFFFF)
        }


    fun contentTypeTitleColor(type: LearnAndActContentType): Color = when (type) {
            LearnAndActContentType.NEWS -> Color(0xFF6640DA)
            LearnAndActContentType.VICTORY -> Color(0xFFB5551A)
            LearnAndActContentType.ACT -> Color(0xFFDB0012)
            LearnAndActContentType.LEARN -> Color(0xFF0D7657)
            LearnAndActContentType.UNDEFINED -> Color(0xFF000000)
        }


    fun contentTypeDrawable(type: LearnAndActContentType): Int = when (type) {
            LearnAndActContentType.NEWS -> R.drawable.learn_and_act_news
            LearnAndActContentType.VICTORY -> R.drawable.learn_and_act_victory
            LearnAndActContentType.ACT -> R.drawable.learn_and_act_act
            LearnAndActContentType.LEARN -> R.drawable.learn_and_act_learn
            LearnAndActContentType.UNDEFINED -> R.drawable.learn_and_act_learn

    }
}
