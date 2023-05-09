package com.njbrady.nusic.profile.composables

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.njbrady.nusic.R
import com.njbrady.nusic.login.composables.ErrorWithField
import com.njbrady.nusic.login.model.GeneralStates
import com.njbrady.nusic.profile.requests.SongListType
import com.njbrady.nusic.ui.theme.NusicSeeThroughBlack
import com.njbrady.nusic.utils.composables.Keyboard
import com.njbrady.nusic.utils.composables.keyboardAsState


@Composable
fun ProfileUsername(modifier: Modifier = Modifier, username: String) {
    val adaptiveTextName = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = 0.25.sp,
        background = MaterialTheme.colors.onBackground
    )
    Text(
        modifier = Modifier,
        text = username,
        style = adaptiveTextName,
        color = MaterialTheme.colors.background
    )
}

@Composable
fun ProfileBio(
    modifier: Modifier = Modifier,
    bio: String?,
    state: GeneralStates,
    uploadedBio: String?,
    errorMessages: List<String>? = null,
    onValueChanged: (String) -> Unit,
    onFocusChanged: () -> Unit,
    onDone: () -> Unit,
    onFocusing: () -> Unit,
    visiting: Boolean
) {
    val keyBoard by keyboardAsState()
    val focusManager = LocalFocusManager.current
    val localContext = LocalContext.current
    var saving = false
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(id = R.dimen.NusicDimenX1)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state != GeneralStates.Loading) {
                if (!visiting) {
                    OutlinedTextField(
                        modifier = Modifier.onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                onFocusing()
                            }

                            if (!focusState.isFocused && bio != uploadedBio && !saving && state != GeneralStates.Success && state != GeneralStates.Error) {
                                onFocusChanged()
                                Toast.makeText(
                                    localContext,
                                    R.string.bio_not_saved,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        value = bio ?: "",
                        onValueChange = { new -> onValueChanged(new) },
                        placeholder = {
                            Text(
                                textAlign = TextAlign.Center,
                                text = stringResource(R.string.add_bio),
                            )
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            saving = true
                            focusManager.clearFocus()
                            onDone()
                        })
                    )

                    if (state == GeneralStates.Error) {
                        errorMessages?.forEach {
                            ErrorWithField(message = it)
                        }
                    }
                } else {
                    bio?.let {
                        Text(textAlign = TextAlign.Center, text = bio)
                    }
                }
            } else {
                CircularProgressIndicator()
            }

            LaunchedEffect(keyBoard) {
                if (keyBoard == Keyboard.Opened) {
                    Toast.makeText(localContext, R.string.bio_warning, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}


@Composable
fun MusicSelectionTab(
    modifier: Modifier = Modifier,
    currentlySelected: SongListType,
    onFilter: (SongListType) -> Unit
) {
    TabRow(
        modifier = modifier,
        selectedTabIndex = currentlySelected.ordinal,
        backgroundColor = MaterialTheme.colors.background,
        indicator = { tabPositions: List<TabPosition> ->
            Box(
                Modifier
                    .tabIndicatorOffset(tabPositions[currentlySelected.ordinal])
                    .fillMaxSize()
            ) {
                Divider(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    color = MaterialTheme.colors.onBackground
                )
            }
        },
    ) {
        SongListType.values().forEachIndexed() { index, type ->
            val selected = index == currentlySelected.ordinal
            Tab(modifier = Modifier, selected = selected, onClick = {
                onFilter(type)
            }) {
                Text(
                    modifier = Modifier.padding(
                        vertical = dimensionResource(id = R.dimen.NusicDimenX1),
                        horizontal = dimensionResource(id = R.dimen.NusicDimenX2)
                    ), text = type.name
                )
            }
        }
    }
}

