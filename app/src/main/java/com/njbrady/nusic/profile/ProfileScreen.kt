package com.njbrady.nusic

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.njbrady.nusic.ui.theme.NusicTheme

@Composable
fun ProfileScreen(mainViewModel: MainViewModel) {
    ProfileScreenContent(mainViewModel = mainViewModel)
}

@Composable
private fun ProfileScreenContent(
    mainViewModel: MainViewModel
) {
    Scaffold(topBar = { ProfileScreenHeader(mainViewModel) }) { paddingValues ->

        Column(
            modifier = Modifier.padding(paddingValues.calculateBottomPadding())
        ) {
            Row {
                Button(onClick = {
                    mainViewModel.logout()
                    mainViewModel.getOnLogoutHit()()
                }) {
                    Text(text = "Logout")
                }
            }
        }
    }
}

@Composable
private fun ProfileScreenHeader(mainViewModel: MainViewModel) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.Transparent,
        elevation = 0.dp,
        title = {},
        actions = {
            IconButton(
                onClick = { /*on upload*/ },
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.NusicDimenX4))
                    .padding(
                        end = dimensionResource(id = R.dimen.NusicDimenX1)
                    )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.nusic_upload_icon),
                    contentDescription = "Upload Button"
                )
            }
            Box(
                modifier = Modifier
                    .wrapContentSize(Alignment.TopStart)
            ) {
                IconButton(
                    onClick = { expanded = true },
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.NusicDimenX5))
                        .padding(
                            end = dimensionResource(id = R.dimen.NusicDimenX1)
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.nusic_settings_icon),
                        contentDescription = "Settings Button"
                    )
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(onClick = { mainViewModel.logout() }) {
                        Text("Logout", color = MaterialTheme.colors.error)
                    }
                }
            }
        })
}

//@Composable
//private fun ProfileScreenDropDownMenu(expanded: Boolean, onLogout: () -> Unit) {
//    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
//        DropdownMenuItem(onClick = { onLogout() }) {
//            Text("Settings")
//        }
//    }
//}

@Composable
@Preview(showBackground = true)
private fun viewer() {
    NusicTheme {
//        ProfileScreenHeader(null)
    }
}