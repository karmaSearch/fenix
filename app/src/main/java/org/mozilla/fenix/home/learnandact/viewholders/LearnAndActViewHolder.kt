package org.mozilla.fenix.home.learnandact.viewholders

import android.view.View
import androidx.annotation.Dimension
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.recyclerview.widget.RecyclerView
import karma.service.learnandact.LearnAndAct
import mozilla.components.lib.state.ext.observeAsComposableState
import mozilla.components.support.images.compose.loader.Fallback
import mozilla.components.support.images.compose.loader.ImageLoader
import mozilla.components.support.images.compose.loader.Placeholder
import mozilla.components.support.images.compose.loader.WithImage
import mozilla.components.ui.colors.PhotonColors
import org.mozilla.fenix.R
import org.mozilla.fenix.components.components
import org.mozilla.fenix.home.HomeFragmentStore
import org.mozilla.fenix.home.learnandact.LearnAndActInteractor
import org.mozilla.fenix.theme.FirefoxTheme

class LearnAndActViewHolder(
    val composeView: ComposeView,
    val store: HomeFragmentStore,
    val interactor: LearnAndActInteractor
) : RecyclerView.ViewHolder(composeView) {
    companion object {
        val LAYOUT_ID = View.generateViewId()
    }

    init {
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        composeView.setContent {
            FirefoxTheme {
                LearnAndAct(
                    store,
                    interactor::onBlockShown,
                    interactor::onBlockClicked,
                    with(composeView.resources) {
                        getDimensionPixelSize(R.dimen.home_item_horizontal_margin) / displayMetrics.density
                    }
                )
            }
        }
    }
}

@Composable
@Suppress("LongParameterList")
fun LearnAndAct(
    store: HomeFragmentStore,
    onBlockShown: (List<LearnAndAct>) -> Unit,
    onBlockClicked: (LearnAndAct, Pair<Int, Int>) -> Unit,
    @Dimension horizontalPadding: Float = 0f
) {
    val learnAndActBlocs = store
        .observeAsComposableState { state -> state.learnAndAct }.value
    LaunchedEffect(learnAndActBlocs) {
        // We should report back when a certain story is actually being displayed.
        // Cannot do it reliably so for now we'll just mass report everything as being displayed.
        learnAndActBlocs?.let {
            onBlockShown(it)
        }
    }
    print(horizontalPadding)
    print(onBlockClicked)
    Column {
        Spacer(Modifier.height(5.dp))
        Text("Learn & Act",
            fontSize = 38.sp,
            fontWeight = FontWeight.Normal,
            color = PhotonColors.Green50,
            fontFamily = FontFamily(Font(R.font.amithen)),
            modifier = Modifier
            .padding(start = 10.dp))

        Spacer(Modifier.height(5.dp))

        LearnAndActBlocs(learnAndActBlocs ?: emptyList(), onBlockClicked)

    }
}

@OptIn(ExperimentalUnitApi::class)
@Composable
fun LearnAndActBlocs(
    learnAndActBlocs: List<LearnAndAct>,
    onStoryClicked: (LearnAndAct, Pair<Int, Int>) -> Unit
) {

    learnAndActBlocs.forEach { item ->
        Card(shape = RoundedCornerShape(8.dp),
            elevation = 12.dp,
            backgroundColor = FirefoxTheme.colors.surface,
            modifier = Modifier
                .clickable { onStoryClicked(item, Pair(0, 0)) }
                .padding(start = 10.dp, end = 10.dp)) {

            val defaultImageName = if(item.type.lowercase() == "learn" || item.type.lowercase() == "comprendre") R.drawable.ic_learn_placeholder else R.drawable.ic_act_placeholder

            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                ImageLoader(
                    url = item.imageUrl,
                    client = components.core.client
                ) {
                    WithImage { painter ->
                        androidx.compose.foundation.Image(
                            painter = painter,
                            modifier = Modifier
                                .height(193.dp)
                                .fillMaxWidth(),
                            contentDescription = null,
                            contentScale = ContentScale.FillHeight
                        )
                    }

                    Placeholder {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = defaultImageName),
                            contentDescription = null,
                            modifier = Modifier
                                .height(193.dp)
                                .fillMaxWidth(),
                            contentScale = ContentScale.FillWidth
                        )
                    }

                    Fallback {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = defaultImageName),
                            contentDescription = null,
                            modifier = Modifier
                                .height(193.dp)
                                .fillMaxWidth(),
                            contentScale = ContentScale.FillWidth
                        )
                    }
                }
                Text(
                    text = item.type.uppercase(),
                    lineHeight = 22.sp, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .offset(y = -12.dp)
                        .clip(RoundedCornerShape(0.dp, 12.dp, 12.dp, 0.dp))
                        .background(FirefoxTheme.colors.brandColor)
                        .padding(start = 17.dp, end = 17.dp, bottom = 0.dp)
                        .height(24.dp) ,
                    color = Color.Black,
                    fontFamily = FontFamily(Font(R.font.proximanova_semibold)),
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    modifier = Modifier
                        .offset(y = -10.dp)
                        .padding(start = 10.dp, end = 10.dp, top = 0.dp, bottom = 5.dp)
                ) {
                    Text(
                        text = item.title,
                        lineHeight = 19.sp,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FirefoxTheme.colors.textPrimary,
                        fontFamily = FontFamily(Font(R.font.proximanova_semibold)),
                    )
                    if (item.duration.isNotEmpty()) {
                        Text(
                            text = item.duration,
                            lineHeight = 17.sp,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.LightGray,
                            fontFamily = FontFamily(Font(R.font.proximanova_medium)),
                        )
                    }
                    Text(
                        text = item.description,
                        lineHeight = 20.sp,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = FirefoxTheme.colors.textPrimary,
                        maxLines = 4,
                        fontFamily = FontFamily(Font(R.font.proximanova_medium)),
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.action,
                        lineHeight = 20.sp,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = FirefoxTheme.colors.brandColor,
                        fontFamily = FontFamily(Font(R.font.proximanova_bold)),
                    )
                }
            }

        }

        Spacer(modifier = Modifier.height(25.dp))
    }


}


@Preview
@Composable
private fun LearnAndActBlocPreview() {
    FirefoxTheme {
        LearnAndActBlocs(learnAndActBlocs = mutableListOf<LearnAndAct>().apply {

            add(
                LearnAndAct(
                    "LEARN",
                    "The State of Biodiversity and how we’ll get better",
                    "The IPBES (Intergovernmental Science-Policy Platform on Biodiversity and Ecosystem Services) just released...",
                    "Learn more",
                    "",
                    "",
                    "https://mykarma.org/images/home/LearnAct2_FR.webp"
                )
            )
            add(
                LearnAndAct(
                    "ACT",
                    "The State of Biodiversity and how we’ll get better",
                    "The IPBES (Intergovernmental Science-Policy Platform on Biodiversity and Ecosystem Services) just released...",
                    "Learn more",
                    "",
                    "7 minutes",
                    ""
                )
            )

        }, onStoryClicked = { _, _ ->

        })
    }
}
