package com.zero.app.view

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV
import com.zero.app.model.Record
import com.zero.app.util.Util
import kotlinx.coroutines.launch

@SuppressLint("MutableCollectionMutableState")
@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun AnalyzePage() {
    val kv = MMKV.defaultMMKV()
    val gson = Gson()
    var pmMap = mutableMapOf<String, MutableList<String>>()
    var nmMap = mutableMapOf<String, Int>()
    var pmData by remember {
        mutableStateOf(pmMap)
    }
    var nmData by remember {
        mutableStateOf(nmMap)
    }

    DisposableEffect(Unit) {
        val pmStr = kv.decodeString("people-medicine")
        val nmStr = kv.decodeString("medicine-num")
        pmStr?.let {
            val typeToken = object : TypeToken<Map<String, MutableList<String>>>() {}.type
            pmMap = gson.fromJson(it, typeToken)
            pmData = pmMap
        }
        nmStr?.let {
            val typeToken = object : TypeToken<Map<String, Int>>() {}.type
            nmMap = gson.fromJson(nmStr, typeToken)
            nmData = nmMap
        }
        onDispose {
            // 在页面销毁时执行清理操作
        }
    }


    val pagerState = rememberPagerState(
        0
    )
    val coroutineScope = rememberCoroutineScope()
    var selectedTabIndex by remember { mutableStateOf(0) }
    var searchText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    var showDialog by remember {
        mutableStateOf(false)
    }
    var record: Record? = null

    Scaffold(
        topBar = {
            // 顶部搜索框
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 输入框
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { newValue ->
                                searchText = newValue
                            },
                            placeholder = { Text("Search...") },
                            modifier = Modifier
                                .weight(1f)
                        )

                        // 清除按钮
                        IconButton(
                            onClick = {
                                searchText = ""
                                keyboardController?.hide()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }

                        // 搜索按钮
                        IconButton(
                            onClick = {
                                if (searchText.isEmpty()) {
                                    pmData = pmMap
                                    nmData = nmMap
                                } else {
                                    if (pagerState.currentPage == 0) {
                                        pmData = mutableMapOf()
                                        val list = pmMap[searchText]
                                        list?.let {
                                            pmData[searchText] = it
                                        }
                                    } else {
                                        nmData = mutableMapOf()
                                        val n = nmMap[searchText]
                                        n?.let {
                                            nmData[searchText] = it
                                        }
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        content = {
            // 搜索框、按钮和列表
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 两个按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(it),
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.scrollToPage(0)
                            }
                        }
                    ) {
                        Text("姓名-药品")
                    }
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.scrollToPage(1)
                            }
                        }
                    ) {
                        Text("药品-数量")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                HorizontalPager(
                    pageCount = 2, state = pagerState
                ) { page ->
                    val npmData = mutableListOf<Record>()
                    if (page == 0) {
                        for (entry in pmData) {
                            npmData.add(Record(entry.key, entry.value))
                        }
                    }
                    when (page) {
                        0 -> PMPage(data = npmData, deleteItem = {
                            val list = pmMap[it]
                            pmMap.remove(it)
                            pmData = mutableMapOf()
                            pmData.putAll(pmMap)
                            kv.encode("people-medicine", gson.toJson(pmMap))

                            if (list != null) {
                                for (d in list) {
                                    val res = nmMap[d]?.minus(1)
                                    nmMap[d] = res ?: 0
                                    if (res != null) {
                                        if (res < 1) {
                                            nmMap.remove(d)
                                        }
                                    }
                                }
                            }
                            nmData = mutableMapOf()
                            nmData.putAll(nmMap)
                            kv.encode("medicine-num", gson.toJson(nmMap))
                        },
                            editItem = {
                                record = it
                                showDialog = true
                            })

                        1 -> NMPage(data = nmData)
                    }
                    if (showDialog) {
                        ModifyDialog(
                            initialName = record?.name ?: "",
                            initialDescription = toStr(record?.medicine),
                            onNameChanged = { newName ->
                                record?.medicine?.let {
                                    pmMap[newName] = record!!.medicine.toMutableList()
                                }
                                pmMap.remove(record?.name)
                                pmData = mutableMapOf()
                                pmData.putAll(pmMap)
                                kv.encode("people-medicine", gson.toJson(pmMap))
                            },
                            onDescriptionChanged = { newDescription ->
                                val newMedicine = handleMedicine(newDescription)
                                record?.name?.let {
                                    pmMap[it] = newMedicine
                                }
                                pmData = mutableMapOf()
                                pmData.putAll(pmMap)
                                kv.encode("people-medicine", gson.toJson(pmMap))

                                if (record?.medicine != null) {
                                    for (d in record?.medicine!!) {
                                        val res = nmMap[d]?.minus(1)
                                        nmMap[d] = res ?: 0
                                        if (res != null) {
                                            if (res < 1) {
                                                nmMap.remove(d)
                                            }
                                        }
                                    }
                                }
                                for (d in newMedicine) {
                                    if (!nmMap.containsKey(d)) {
                                        nmMap[d] = 1
                                    } else {
                                        val res = nmMap[d]?.plus(1)
                                        nmMap[d] = res ?: 0
                                    }
                                }
                                nmData = mutableMapOf()
                                nmData.putAll(nmMap)
                                kv.encode("medicine-num", gson.toJson(nmMap))
                            },
                            onDismiss = {
                                showDialog = false
                            },
                            onSave = {
                                showDialog = false
                            }
                        )
                    }

                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PMPage(data: MutableList<Record>, deleteItem: (String) -> Unit, editItem: (Record) -> Unit) {
    LazyColumn {
        items(data.size) { index ->
            var medicines = ""
            for (m in data[index].medicine) {
                medicines += "$m "
            }
            ListItem(headlineText = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(text = data[index].name)
                        Row {
                            IconButton(
                                onClick = { editItem(data[index]) },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Modify"
                                )
                            }

                            // 删除按钮
                            IconButton(
                                onClick = { deleteItem(data[index].name) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete"
                                )
                            }
                        }
                    }
                    Text(text = medicines)
                    if (index < data.size - 1) {
                        Divider(
                            color = Color.Gray,
                            thickness = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        )
                    }
                }
            })
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NMPage(data: Map<String, Int>) {
    val mapList = data.entries.toList()
    val sortedMapList = mapList.sortedByDescending { it.value }
    LazyColumn {
        items(sortedMapList.size) { index ->
            ListItem(headlineText = {
                Column {
                    Text("${sortedMapList[index].key} ${sortedMapList[index].value}")
                    if (index < data.size - 1) {
                        Divider(
                            color = Color.Gray,
                            thickness = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        )
                    }
                }
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifyDialog(
    initialName: String,
    onNameChanged: (String) -> Unit,
    initialDescription: String,
    onDescriptionChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var nameText by remember { mutableStateOf(initialName) }
    var descriptionText by remember { mutableStateOf(initialDescription) }

    Dialog(
        onDismissRequest = {
            // 处理弹窗关闭事件
            onDismiss()
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.White)
        ) {
            Column {
                Text(text = "Modify Item")

                // 输入框1：Name
                TextField(
                    value = nameText,
                    onValueChange = {
                        nameText = it
                    },
                    label = { Text("Name") }
                )

                // 输入框2：Description
                TextField(
                    value = descriptionText,
                    onValueChange = {
                        descriptionText = it
                    },
                    label = { Text("Description") }
                )
                // 保存和取消按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextButton(
                        onClick = {
                            // 处理取消按钮点击事件
                            onDismiss()
                        }
                    ) {
                        Text("Cancel")
                    }

                    TextButton(
                        onClick = {
                            // 处理保存按钮点击事件
                            if (nameText != initialName && descriptionText != initialDescription) {
                                val toast =
                                    Toast.makeText(
                                        Util.context,
                                        "不支持两个都修改,建议删除",
                                        Toast.LENGTH_SHORT
                                    )
                                toast.show()
                            } else if (nameText != initialName) {
                                onNameChanged(nameText)
                            } else if (descriptionText != initialDescription) {
                                onDescriptionChanged(descriptionText)
                            }
                            onSave()
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

fun toStr(list: List<String>?): String {
    var str = ""
    if (list != null) {
        for (i in list) {
            str += "$i，"
        }
    }
    return str.dropLast(1)
}
