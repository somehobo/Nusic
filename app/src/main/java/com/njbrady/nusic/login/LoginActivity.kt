package com.njbrady.nusic.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.njbrady.nusic.MainActivity
import com.njbrady.nusic.login.model.LoginScreenViewModel
import com.njbrady.nusic.ui.theme.NusicTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {

    private val loginScreenViewModel: LoginScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val loginState = loginScreenViewModel.localStorage.containsToken.collectAsState()
            NusicTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
                ) {
                    if (loginState.value) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        LoginScreen(loginScreenViewModel = loginScreenViewModel)
                    }
                }
            }
        }
    }
}