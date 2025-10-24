package com.admobads.ads.utils

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.admobads.ads.R

@Composable
fun AdLoadingComposable(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFF8F8F8),
    textColor: Color = Color.Black,
    progressColor: Color = Color.Black,
    onDismissRequest: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier.size(100.dp),
            elevation = CardDefaults.cardElevation(0.dp),
            shape = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(30.dp),
                    color = progressColor,
                    strokeWidth = 2.dp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.tlib_loading_ad),
                    color = textColor,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun YourComposableScreen() {
    var showLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome to My App")

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showLoading = true }
        ) {
            Text("Show Ad Loading")
        }
    }

    if (showLoading) {
        AdLoadingComposable(
            backgroundColor = Color(0xFFF8F8F8),
            textColor = Color.Black,
            progressColor = Color.Black,
            onDismissRequest = { showLoading = false }
        )
    }
}