package com.example.androiddemo.auth

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

    fun signOut() {
        auth.signOut()
    }
}
