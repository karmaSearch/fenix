package org.mozilla.fenix.home.learnandact

import karma.service.learnandact.LearnAndAct

interface LearnAndActInteractor {
    fun onBlockShown(storiesShown: List<LearnAndAct>)
    fun onBlockClicked(bloc: LearnAndAct, position: Pair<Int, Int>)

}
