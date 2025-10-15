package com.erabigroup.htmltobitmapkmp

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import android.os.Bundle
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.remember


val html = """
        <!doctype html>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <body style="font-family:sans-serif;margin:16px;text-align:center">
          <h1>My First Heading</h1>
<img src="https://www.w3schools.com/images/w3schools_green.jpg" alt="W3Schools.com">
          <p>My first paragraph.</p>
          <p>My first paragraph.</p>
          <p>My first paragraph.</p>
          <p>My first paragraph.</p>
          <p>My first paragraph.</p>
          <p>My first paragraph.</p>
          <p>My first paragraph.</p>
          <p>My first paragraph.</p>
          <p>My first paragraph.</p>
          <p>My first paragraph.</p>
          <p>My first paragraph.</p>
          <p>My first paragraph.</p>
          <p>My first paragraph.</p>
          <p>My first paragraph.</p>
          <p>My first paragraph.</p>
          <p>My first paragraph.</p>
          <p>My first paragraph.</p>
          <p>My first paragraph.</p>
          <p>My last paragraph.</p>
        </body>
    """.trimIndent()

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            var bitmap by remember {
                mutableStateOf<ImageBitmap?>(null)
            }
            Scaffold { padding ->

                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(20.dp)
                ) {
                    LaunchedEffect(Unit) {
                        val callback = AndroidPrintCallbackManager()
                        val converter = AndroidHtmlToBitmap(this@MainActivity, callback)
                        bitmap = converter.convert(
                            params = HtmlToBitmapParams(
                                width = 500,
                                timeout = 15_000L,
                                html = html
                            )
                        )
                    }
                    bitmap?.let {
                        Image(it, "", Modifier.wrapContentSize())
                    }
                }
            }
        }
    }


}

