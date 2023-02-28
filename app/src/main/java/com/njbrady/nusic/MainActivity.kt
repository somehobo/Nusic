package com.njbrady.nusic

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.njbrady.nusic.home.HomeScreen
import com.njbrady.nusic.home.HomeScreenViewModel
import com.njbrady.nusic.home.RetryButton
import com.njbrady.nusic.login.LoginActivity
import com.njbrady.nusic.login.composables.CenteredProgressIndicator
import com.njbrady.nusic.ui.theme.NusicTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val homeScreenViewModel: HomeScreenViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var mainSocketHandler: MainSocketHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel.setOnLogoutHit {
            val intent = Intent(this, LoginActivity::class.java)
            finish()
            startActivity(intent)
        }
        setContent {
            NusicTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
                ) {
                    MainContent(homeScreenViewModel, mainViewModel, mainSocketHandler)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mainViewModel.pauseCurrent()
        homeScreenViewModel.forcePauseCurrent()
    }

}


@Composable
private fun MainContent(
    homeScreenViewModel: HomeScreenViewModel,
    mainViewModel: MainViewModel,
    mainSocketHandler: MainSocketHandler
) {
    val navController = rememberNavController()
    val socketState by mainSocketHandler.connected.collectAsState()
    val loadingConnection by mainSocketHandler.loadingConnection.collectAsState()

    Scaffold(bottomBar = {
        Column {
            BottomNavigation(backgroundColor = MaterialTheme.colors.background) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                screens.forEach { screen ->
                    BottomNavigationItem(icon = {
                        Icon(
                            Icons.Filled.Favorite, contentDescription = null
                        )
                    },
                        label = { Text(stringResource(screen.resourceId)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        })
                }
            }
            if (!socketState) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colors.error)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (loadingConnection) {
                        CenteredProgressIndicator()
                    } else {
                        Row {
                            Text(
                                text = stringResource(R.string.lost_socket_connection),
                                style = MaterialTheme.typography.h5,
                                color = MaterialTheme.colors.onError,
                                modifier = Modifier.padding(
                                    horizontal = dimensionResource(
                                        id = R.dimen.NusicDimenX2
                                    ), vertical = dimensionResource(id = R.dimen.NusicDimenX1)
                                )
                            )
                            RetryButton(callback = { mainSocketHandler.retryConnection() })
                        }
                    }
                }
            }
        }
    }) { innerPadding ->
        NavHost(
            navController, startDestination = Screen.Home.route, Modifier.padding(innerPadding)
        ) {
            composable(Screen.Profile.route) { ProfileScreen(mainViewModel, navController) }
            composable(Screen.Home.route) {
                HomeScreen(homeScreenViewModel, navController)
            }
            composable(Screen.Conversation.route) { ConversationScreen() }
        }
    }
}

sealed class Screen(val route: String, @StringRes val resourceId: Int) {
    object Home : Screen("home", R.string.home_screen)
    object Conversation : Screen("conversation", R.string.conversation_screen)
    object Profile : Screen("profile", R.string.profile_screen)
}

val screens = listOf(
    Screen.Profile, Screen.Home, Screen.Conversation
)
