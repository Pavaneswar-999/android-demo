package com.example.androiddemo.ui.login

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.androiddemo.R
import com.example.androiddemo.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    val idToken = account?.idToken
                    if (!idToken.isNullOrBlank()) {
                        viewModel.handleGoogleIdToken(idToken)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Google Sign-In failed: No ID Token returned. Continuing in Guest mode.",
                            Toast.LENGTH_LONG
                        ).show()
                        navigateToMainForecast()
                    }
                } catch (e: ApiException) {
                    Toast.makeText(
                        requireContext(),
                        "Google Sign-In error (${e.statusCode}). Proceeding in Guest mode.",
                        Toast.LENGTH_LONG
                    ).show()
                    navigateToMainForecast()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Google Sign-In cancelled. You can continue as Guest.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.checkUserSession()

        viewModel.userState.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.cardUserInfo.visibility = View.VISIBLE
                binding.tvUserName.text = "Signed in as: ${user.displayName ?: "User"}"
                binding.tvUserEmail.text = user.email ?: ""
                navigateToMainForecast()
            } else {
                binding.cardUserInfo.visibility = View.GONE
            }
        }

        viewModel.loadingState.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnGoogleSignin.isEnabled = !isLoading
            binding.btnGuestContinue.isEnabled = !isLoading
        }

        viewModel.errorState.observe(viewLifecycleOwner) { errorMsg ->
            if (!errorMsg.isNullOrBlank()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
            }
        }

        binding.btnGoogleSignin.setOnClickListener {
            startGoogleSignInFlow()
        }

        binding.btnGuestContinue.setOnClickListener {
            navigateToMainForecast()
        }
    }

    private fun navigateToMainForecast() {
        try {
            val navController = findNavController()
            if (navController.currentDestination?.id == R.id.loginFragment) {
                navController.navigate(R.id.action_loginFragment_to_forecastFragment)
            }
        } catch (_: Exception) {}
    }

    private fun startGoogleSignInFlow() {
        val webClientId = getStringResourceByName("default_web_client_id")
        if (webClientId.isNullOrBlank() || webClientId.contains("webclient.apps.googleusercontent.com")) {
            Toast.makeText(
                requireContext(),
                "Opening River Flood Watch Forecast...",
                Toast.LENGTH_SHORT
            ).show()
            navigateToMainForecast()
            return
        }

        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Sign-In error: ${e.localizedMessage}. Proceeding in Guest mode.",
                Toast.LENGTH_LONG
            ).show()
            navigateToMainForecast()
        }
    }

    private fun getStringResourceByName(name: String): String? {
        val resId = resources.getIdentifier(name, "string", requireContext().packageName)
        return if (resId != 0) getString(resId) else null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
