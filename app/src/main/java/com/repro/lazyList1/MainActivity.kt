@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.repro.lazyList1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.repro.lazyList1.ui.theme.ReproLazyList1Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReproLazyList1Theme { Repro(Modifier.fillMaxSize()) }
        }
    }
}

@Composable
fun Repro(modifier: Modifier = Modifier) {
    Column(modifier) {
        TopAppBar(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth(),
            title = { Text("Repro") },
        )

        Divider()

        val lazyList = rememberLazyListState()
        LazyColumn(
            state = lazyList,
            modifier = Modifier.weight(1f),
            contentPadding = WindowInsets.navigationBars.asPaddingValues()
        ) {
            list.fastForEach {
                item(key = null, contentType = { "text-type" }) {
                    Surface(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .aspectRatio(16f / 9f),
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 2.dp,
                    ) {
                        Text(text = it, modifier = Modifier.wrapContentSize())
                    }
                }
            }

            if (showLoading) {
                item(key = "<loading>", contentType = "loading-type") {
                    LoadingIndicator(
                        Modifier
                            .animateItemPlacement()
                            .fillMaxWidth()
                            .height(112.dp)
                        /*.wrapContentSize()*/
                    )
                }
            }
        }
        LaunchedEffect(Unit) {
            snapshotFlow {
                val layoutInfo = lazyList.layoutInfo
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                lastVisibleItem != null &&
                        lastVisibleItem.index == layoutInfo.totalItemsCount - 1 &&
                        lastVisibleItem.key == "<loading>"
            }.collect { if (it) loadMore() }
        }
    }
}

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {

        // toggle commenting the working or failing blocks below to see the behavior

        // region Failing
        val infiniteTransition = rememberInfiniteTransition()
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0F,
            targetValue = 360F,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing)
            )
        )
        Icon(
            modifier = Modifier
                .rotate(rotation) /* Crash doesn't happen if we remove rotation + the transition above*/
                .size(24.dp),
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
        )
        // endregion

        // region Working: no transition/rotation
        /*Icon(
            modifier = Modifier.size(24.dp),
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
        )*/
        // endregion
    }
}

val listState = mutableStateListOf(
    elements = Array(10) { "Text ${it + 1}" }
)

val list by derivedStateOf { listState.toList() }
val showLoading by derivedStateOf { list.size < 30 }

fun CoroutineScope.loadMore() {
    if (showLoading) launch {
        delay(1000.milliseconds) // simulate network call that loads more
        val start = list.size
        listState.addAll(List(10) { "Text ${it + start + 1}" })
    }
}

