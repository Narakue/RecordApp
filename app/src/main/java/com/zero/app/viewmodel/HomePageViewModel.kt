package com.zero.app.viewmodel

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.ViewModel

class HomePageViewModel : ViewModel() {
    var name = mutableStateOf("")
    var medicine = mutableStateOf("")
    val interactionSource = MutableInteractionSource()
}