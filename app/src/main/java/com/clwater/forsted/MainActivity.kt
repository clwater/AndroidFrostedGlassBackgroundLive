package com.clwater.forsted

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.clwater.forsted.ui.theme.AndroidFrostedGlassBackgroundLiveTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DefaultPreview()
        }
    }



    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        AndroidFrostedGlassBackgroundLiveTheme {
            // A surface container using the 'background' color from the theme
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Column() {
//                    SmallTopAppBar(
//                        modifier = Modifier.background(MaterialTheme.colorScheme.primary),
//                            title = { Text(text = stringResource(id = R.string.app_name)) },
//                    )
                }
            }
        }
    }
}


