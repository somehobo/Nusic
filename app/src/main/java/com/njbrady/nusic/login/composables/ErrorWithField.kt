package com.njbrady.nusic.login.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.njbrady.nusic.R

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    ErrorWithField(message = "Test")
}

@Composable
fun ErrorWithField(
    modifier: Modifier = Modifier,
    message: String,
    textColor: Color = LocalContentColor.current,
    textStyle: TextStyle =  LocalTextStyle.current
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Error,
            contentDescription = stringResource(id = R.string.error),
            tint = MaterialTheme.colors.error
        )
        Text(
            text = message,
            color = textColor,
            modifier = Modifier.padding(start = 8.dp),
            style = textStyle
        )
    }
}