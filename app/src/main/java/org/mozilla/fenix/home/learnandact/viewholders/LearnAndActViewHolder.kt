package org.mozilla.fenix.home.learnandact.viewholders

import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import karma.service.learnandact.LearnAndAct
import mozilla.components.support.images.compose.loader.Fallback
import mozilla.components.support.images.compose.loader.ImageLoader
import mozilla.components.support.images.compose.loader.Placeholder
import mozilla.components.support.images.compose.loader.WithImage
import mozilla.components.ui.colors.PhotonColors
import org.mozilla.fenix.R
import org.mozilla.fenix.components.components
import org.mozilla.fenix.compose.ComposeViewHolder
import org.mozilla.fenix.home.learnandact.LearnAndActInteractor
import org.mozilla.fenix.theme.FirefoxTheme


class LearnAndActHeaderViewHolder(composeView: ComposeView,
                                viewLifecycleOwner: LifecycleOwner) : ComposeViewHolder(composeView, viewLifecycleOwner)   {

    companion object {
        val LAYOUT_ID = View.generateViewId()
    }

    @Composable
    override fun Content() {
        Column {
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.learn_and_act_title),
                fontSize = 38.sp,
                fontWeight = FontWeight.Normal,
                color = PhotonColors.Green50,
                fontFamily = FontFamily(Font(R.font.amithen)),
                modifier = Modifier
                    .padding(start = 10.dp)
                    .height(45.dp)
            )
            Text(
                stringResource(R.string.learn_and_act_subtitle),
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily(Font(R.font.proximanova)),
                color = Color.LightGray,
                modifier = Modifier
                    .padding(start = 10.dp)
            )

            Spacer(Modifier.height(10.dp))
        }
    }


}

class LearnAndActItemViewHolder(composeView: ComposeView,
                                viewLifecycleOwner: LifecycleOwner,
                                val interactor: LearnAndActInteractor) : ComposeViewHolder(composeView, viewLifecycleOwner)   {

    private var item = LearnAndAct("", "", "", "", "", "", "")

    companion object {
        val LAYOUT_ID = View.generateViewId()
    }

    @Composable
    override fun Content() {
        Column {
            LearnAndActItem(
                item,
                interactor::onBlockClicked
            )
            Spacer(modifier = Modifier.height(25.dp))
        }

    }

    fun bindSession(item: LearnAndAct) {
        this.item = item
    }

}

@Composable
fun LearnAndActTextsColumn(
    y: Dp,
    item: LearnAndAct
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(0.dp),
        modifier = Modifier
            .offset(y = y)
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
            color = FirefoxTheme.colors.textAccent,
            fontFamily = FontFamily(Font(R.font.proximanova_bold)),
        )
    }

}

@Composable
fun LearnAndActItem(item: LearnAndAct,
                    onStoryClicked: (LearnAndAct) -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = 12.dp,
        backgroundColor = FirefoxTheme.colors.layer2,
        modifier = Modifier
            .clickable { onStoryClicked(item) }
            .padding(start = 10.dp, end = 10.dp),
    ) {

        val defaultImageName =
            if (item.type.lowercase() == "learn" || item.type.lowercase() == "comprendre") R.drawable.ic_learn_placeholder else R.drawable.ic_act_placeholder

        if (booleanResource(id = R.bool.learn_and_act_large_view)) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {

                if (item.imageUrl.startsWith("http")) {
                    ImageLoader(
                        url = item.imageUrl,
                        client = components.core.client,
                    ) {
                        WithImage { painter ->
                            Image(
                                painter = painter,
                                modifier = Modifier.weight(8f, true),
                                contentDescription = null,
                                contentScale = ContentScale.FillWidth,
                            )
                        }

                        Placeholder {
                            Image(
                                painter = painterResource(id = defaultImageName),
                                modifier = Modifier.weight(8f, true),
                                contentDescription = null,
                                contentScale = ContentScale.FillWidth,
                            )
                        }

                        Fallback {
                            Image(
                                painter = painterResource(id = defaultImageName),
                                modifier = Modifier.weight(8f, true),
                                contentDescription = null,
                                contentScale = ContentScale.FillWidth,
                            )
                        }
                    }
                } else {
                    Image(
                        painter = painterResource(id = defaultImageName),
                        modifier = Modifier.weight(8f, true),
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                    )
                }

                Column(
                    modifier = Modifier.weight(10f, true),
                ) {
                    Text(
                        text = item.type.uppercase(),
                        lineHeight = 22.sp,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(0.dp, 12.dp, 12.dp, 0.dp))
                            .background(FirefoxTheme.colors.textAccent)
                            .padding(start = 17.dp, end = 17.dp, bottom = 0.dp)
                            .height(24.dp),
                        color = Color.Black,
                        fontFamily = FontFamily(Font(R.font.proximanova_semibold)),
                    )

                    LearnAndActTextsColumn(item = item, y = 0.dp)
                }


            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                if (item.imageUrl.startsWith("http")) {
                    ImageLoader(
                        url = item.imageUrl,
                        client = components.core.client,
                    ) {
                        WithImage { painter ->
                            Image(
                                painter = painter,
                                modifier = Modifier
                                    .height(193.dp)
                                    .fillMaxWidth(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                            )
                        }

                        Placeholder {
                            Image(
                                painter = painterResource(id = defaultImageName),
                                contentDescription = null,
                                modifier = Modifier
                                    .height(193.dp)
                                    .fillMaxWidth(),
                                contentScale = ContentScale.Crop,
                            )
                        }

                        Fallback {
                            Image(
                                painter = painterResource(id = defaultImageName),
                                contentDescription = null,
                                modifier = Modifier
                                    .height(193.dp)
                                    .fillMaxWidth(),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                } else {
                    Image(
                        painter = painterResource(id = defaultImageName),
                        contentDescription = null,
                        modifier = Modifier
                            .height(193.dp)
                            .fillMaxWidth(),
                        contentScale = ContentScale.Crop,
                    )
                }

                Text(
                    text = item.type.uppercase(),
                    lineHeight = 22.sp, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .offset(y = -12.dp)
                        .clip(RoundedCornerShape(0.dp, 12.dp, 12.dp, 0.dp))
                        .background(FirefoxTheme.colors.textAccent)
                        .padding(start = 17.dp, end = 17.dp, bottom = 0.dp)
                        .height(24.dp),
                    color = Color.Black,
                    fontFamily = FontFamily(Font(R.font.proximanova_semibold)),
                )

                LearnAndActTextsColumn(item = item, y = -10.dp)

            }
        }
    }
}

@Preview
@Composable
private fun LearnAndActBlocPreview() {
    FirefoxTheme {
        LearnAndActItem(
            LearnAndAct(
                "LEARN",
                "The State of Biodiversity and how we’ll get better",
                "The IPBES (Intergovernmental Science-Policy Platform on Biodiversity and Ecosystem Services) just released...",
                "Learn more",
                "",
                "",
                "https://karmasearch.org/images/home/LearnAct2_FR.webp"
            ),
            onStoryClicked = { _ ->

            }
        )

    }
}
