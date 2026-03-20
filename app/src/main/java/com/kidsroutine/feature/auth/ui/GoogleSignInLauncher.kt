package com.kidsroutine.feature.auth.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.kidsroutine.feature.auth.data.GoogleSignInHelper

@Composable
fun rememberGoogleSignInLauncher(
    googleSignInHelper: GoogleSignInHelper,
    onSignInSuccess: (String) -> Unit,
    onSignInFailure: (String) -> Unit
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
) { result ->
    try {
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        val account = task.getResult(ApiException::class.java)
        val idToken = account?.idToken
        if (idToken != null) {
            onSignInSuccess(idToken)
        } else {
            onSignInFailure("ID token is null")
        }
    } catch (e: ApiException) {
        onSignInFailure(e.message ?: "Google sign in failed")
    }
}