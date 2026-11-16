package com.example.androiddemo.ui.login

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androiddemo.auth.FirebaseAuthManager
import com.google.firebase.auth.FirebaseUser

class LoginViewModel : ViewModel() {

    private val _userState = MutableLiveData<FirebaseUser?>(FirebaseAuthManager.currentUser)
    val userState: LiveData<FirebaseUser?> = _userState

    private val _loadingState = MutableLiveData<Boolean>(false)
    val loadingState: LiveData<Boolean> = _loadingState

    private val _errorState = MutableLiveData<String?>()
    val errorState: LiveData<String?> = _errorState

    fun checkUserSession() {
        _userState.value = FirebaseAuthManager.currentUser
    }

    fun handleGoogleIdToken(idToken: String) {
        _loadingState.value = true
        _errorState.value = null
        FirebaseAuthManager.signInWithGoogleIdToken(
            idToken = idToken,
            onSuccess = { user ->
                _loadingState.value = false
                _userState.value = user
            },
            onFailure = { exception ->
                _loadingState.value = false
                _errorState.value = exception.localizedMessage ?: "Firebase Sign-In failed"
            }
        )
    }

    fun signOut(context: Context? = null, webClientId: String? = null, onComplete: (() -> Unit)? = null) {
        FirebaseAuthManager.signOut(context, webClientId) {
            _userState.value = null
            onComplete?.invoke()
        }
    }
}
