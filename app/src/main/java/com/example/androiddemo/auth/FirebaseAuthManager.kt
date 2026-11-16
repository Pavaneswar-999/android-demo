package com.example.androiddemo.auth

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

object FirebaseAuthManager {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun isUserSignedIn(): Boolean {
        return currentUser != null
    }

    fun signInWithGoogleIdToken(
        idToken: String,
        onSuccess: (FirebaseUser) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    onSuccess(user)
                } else {
                    onFailure(IllegalStateException("FirebaseUser is null after sign in"))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun signOut(context: Context? = null, webClientId: String? = null, onComplete: (() -> Unit)? = null) {
        auth.signOut()

        if (context != null) {
            try {
                val gsoBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail()
                if (!webClientId.isNullOrBlank()) {
                    gsoBuilder.requestIdToken(webClientId)
                }
                val googleSignInClient = GoogleSignIn.getClient(context, gsoBuilder.build())
                googleSignInClient.signOut().addOnCompleteListener {
                    onComplete?.invoke()
                }
                return
            } catch (_: Exception) {}
        }
        onComplete?.invoke()
    }
}
