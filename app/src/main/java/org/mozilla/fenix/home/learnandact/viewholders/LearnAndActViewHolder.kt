package org.mozilla.fenix.home.learnandact.viewholders

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
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
import com.squareup.picasso.Picasso
import karma.service.learnandact.LearnAndAct
import mozilla.components.ui.colors.PhotonColors
import org.mozilla.fenix.R
import org.mozilla.fenix.compose.ComposeViewHolder
import org.mozilla.fenix.home.learnandact.LearnAndActInteractor
import org.mozilla.fenix.theme.FirefoxTheme
import org.mozilla.fenix.theme.KarmaColors


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
                fontSize = 45.sp,
                fontWeight = FontWeight.Normal,
                color = PhotonColors.Green50,
                fontFamily = FontFamily(Font(R.font.amithen)),
                modifier = Modifier
                    .padding(start = 10.dp)
                    .height(45.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.learn_and_act_subtitle),
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily(Font(R.font.proximanova)),
                color = FirefoxTheme.colors.textSecondary,
                modifier = Modifier
                    .padding(start = 10.dp)
            )

            Spacer(Modifier.height(8.dp))
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

                    NetworkImage(
                        url = item.imageUrl, defaultImageName,
                        modifier = Modifier.weight(8f, true),
                        contentScale = ContentScale.FillWidth
                    )
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

                    LearnAndActType(item = item)
                    LearnAndActTextsColumn(item = item)
                }


            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier.fillMaxHeight(1f)) {

                if (item.imageUrl.startsWith("http")) {

                    NetworkImage(
                        url = item.imageUrl, defaultImageName,
                        modifier = Modifier
                            .height(193.dp)
                            .fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = defaultImageName),
                        modifier = Modifier
                            .height(193.dp)
                            .fillMaxWidth(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                    )
                }
                LearnAndActType(item = item, offset = -12.dp)
                LearnAndActTextsColumn(item = item, y = -10.dp)

            }
        }
    }
}


@Composable
fun LearnAndActType(
    item: LearnAndAct,
    offset: Dp = 0.dp
) {

    val backgroundColor =
        if (item.type.lowercase() == "learn") KarmaColors.learnHeader else KarmaColors.actHeader
    val icon = if (item.type.lowercase() == "learn") R.drawable.ic_learn else R.drawable.ic_act
    Row(horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .offset(y = offset)
            .clip(RoundedCornerShape(0.dp, 12.dp, 12.dp, 0.dp))
            .background(backgroundColor)
            .padding(start = 17.dp, end = 17.dp)
            .height(24.dp)) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxHeight(0.9f)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = item.type.uppercase(),
            lineHeight = 22.sp,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            fontFamily = FontFamily(Font(R.font.proximanova_semibold)),
        )
    }

}

@Composable
fun LearnAndActTextsColumn(
    y: Dp = 0.dp,
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
            color = FirefoxTheme.colors.textSecondary,
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
fun NetworkImage(url: String?, defaultImageName: Int, modifier: Modifier, contentScale: ContentScale) {

    var image by remember { mutableStateOf<ImageBitmap?>(null) }
    var drawable by remember { mutableStateOf<Drawable?>(null) }

    DisposableEffect(url) {
        val picasso = Picasso.get()

        val target = object : com.squareup.picasso.Target {
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                drawable = placeHolderDrawable
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                drawable = errorDrawable
            }

            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                image = bitmap?.asImageBitmap()
            }
        }

        picasso
            .load(url)
            .placeholder(defaultImageName)
            .error(defaultImageName)
            .into(target)


        onDispose {
            image = null
            drawable = null
            picasso.cancelRequest(target)
        }
    }

    if (image != null) {
        Image(bitmap = image!!, contentDescription = null, modifier = modifier, contentScale = contentScale)
    } else  {
        Image(
            painter = painterResource(id = defaultImageName),
            contentDescription = null,
            modifier = modifier,
            contentScale = contentScale,
        )
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
