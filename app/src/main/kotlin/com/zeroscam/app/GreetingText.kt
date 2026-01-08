package com.zeroscam.app

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.zeroscam.app.ui.theme.zeroScamTheme

@Composable
fun greetingText(
    name: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "Hello $name!",
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
fun greetingTextPreview() {
    zeroScamTheme {
        greetingText("Android")
    }
}
