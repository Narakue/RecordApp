package com.zero.app.view

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV
import com.zero.app.util.Util

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomePage() {
    var name by remember { mutableStateOf("") }
    var medicine by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .clickable(
                enabled = true,
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    keyboardController?.hide()
                },

                )
    ) {
        // 输入姓名
        OutlinedTextField(
            value = name,
            onValueChange = { newName -> name = newName },
            label = { Text("姓名") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) {
                        // 当失去焦点时，隐藏键盘
                        keyboardController?.hide()
                    }
                }
        )

        // 输入药品
        OutlinedTextField(
            value = medicine,
            onValueChange = { newMedicine -> medicine = newMedicine },
            label = { Text("药品") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) {
                        // 当失去焦点时，隐藏键盘
                        keyboardController?.hide()
                    }
                }
        )

        // 提交按钮
        Button(
            onClick = {
                try {
                    val medicines = handleMedicine(medicine)
                    val gson = Gson()
                    val kv = MMKV.defaultMMKV()
                    val pmStr = kv.decodeString("people-medicine")
                    var pmMap = mutableMapOf<String, MutableList<String>>()
                    pmStr?.let {
                        val typeToken = object : TypeToken<Map<String, MutableList<String>>>() {}.type
                        pmMap = gson.fromJson(it, typeToken)
                    }
                    if (pmMap.containsKey(name)) {
                        val list = pmMap[name]
                        list?.addAll(medicines)
                    } else {
                        pmMap[name] = medicines
                    }
                    kv.encode("people-medicine", gson.toJson(pmMap))

                    val nmStr = kv.decodeString("medicine-num")
                    var nmMap = mutableMapOf<String, Int>()
                    nmStr?.let {
                        val typeToken = object : TypeToken<Map<String, Int>>() {}.type
                        nmMap = gson.fromJson(nmStr, typeToken)
                    }
                    for (m in medicines) {
                        if (!nmMap.containsKey(m)) {
                            nmMap[m] = 1
                        } else {
                            nmMap[m]?.plus(1)?.let {
                                nmMap[m] = it
                            }
                        }
                    }

                    kv.encode("medicine-num", gson.toJson(nmMap))

                    name = ""
                    medicine = ""
                    keyboardController?.hide()
                    val toast =
                        Toast.makeText(Util.context, "添加成功", Toast.LENGTH_SHORT)
                    toast.show()
                } catch (e: Exception) {
                    val toast =
                        Toast.makeText(Util.context, "添加失败!", Toast.LENGTH_LONG)
                    toast.show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(text = "提交")
        }
    }
}

fun handleMedicine(medicine: String): MutableList<String> {
    return medicine.split("，").toMutableList()
}
