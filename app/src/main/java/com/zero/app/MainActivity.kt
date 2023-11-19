package com.zero.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tencent.mmkv.MMKV
import com.zero.app.ui.theme.AppTheme
import com.zero.app.util.Util
import com.zero.app.view.AnalyzePage
import com.zero.app.view.HomePage
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MMKV.initialize(this)
        Util.context = this
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Page()
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Page() {
    val coroutineScope = rememberCoroutineScope()
    var selectedTabIndex by remember {
        mutableStateOf(0)
    }
    val tabNames = listOf("Tab 1", "Tab 2")
    val pagerState = rememberPagerState(
        0
    )
    Column {
        ScrollableTabRow(
            modifier = Modifier.padding(bottom = 5.dp) ,
            selectedTabIndex = selectedTabIndex,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    color = Color.White,
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex])
                )
            }
        ) {
            tabNames.forEachIndexed { index, title ->
                Tab(
                    text = { Text(text = title) },
                    selected = selectedTabIndex == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.scrollToPage(index)
                        }
                        selectedTabIndex = index
                    },
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        HorizontalPager(pageCount = 2, state = pagerState) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                when (page) {
                    0 -> HomePage()
                    1 -> AnalyzePage()
                    else -> {
                        Text(text = "error")
                    }
                }
            }
        }

        // 监听页面索引的变化
        LaunchedEffect(pagerState.currentPage) {
            selectedTabIndex = pagerState.currentPage
        }
    }
}