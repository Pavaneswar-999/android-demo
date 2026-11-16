package com.example.androiddemo.ui.forecast

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androiddemo.R
import com.example.androiddemo.auth.FirebaseAuthManager
import com.example.androiddemo.databinding.FragmentForecastBinding
import java.util.Locale

class ForecastFragment : Fragment() {

    private var _binding: FragmentForecastBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ForecastViewModel by viewModels()
    private val adapter = ForecastAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForecastBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        binding.btnManagePlaces.setOnClickListener {
            findNavController().navigate(R.id.action_forecastFragment_to_placesFragment)
        }

        binding.btnLogout.setOnClickListener {
            val resId = resources.getIdentifier("default_web_client_id", "string", requireContext().packageName)
            val webClientId = if (resId != 0) getString(resId) else null
            FirebaseAuthManager.signOut(requireContext(), webClientId) {
                Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_forecastFragment_to_loginFragment)
            }
        }

        binding.btnRetry.setOnClickListener {
            viewModel.refreshCurrentForecast()
        }
    }

    private fun setupRecyclerView() {
        binding.rvForecast.layoutManager = LinearLayoutManager(requireContext())
        binding.rvForecast.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.currentPlace.observe(viewLifecycleOwner) { place ->
            binding.tvPlaceTitle.text = place.name
            binding.tvPlaceCoords.text = String.format(
                Locale.US,
                "Lat: %.4f, Lon: %.4f",
                place.latitude,
                place.longitude
            )
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ForecastUiState.Loading -> {
                    binding.layoutLoading.visibility = View.VISIBLE
                    binding.scrollContent.visibility = View.GONE
                    binding.layoutError.visibility = View.GONE
                }
                is ForecastUiState.Success -> {
                    binding.layoutLoading.visibility = View.GONE
                    binding.scrollContent.visibility = View.VISIBLE
                    binding.layoutError.visibility = View.GONE

                    val summary = state.summary
                    binding.tvPlaceTitle.text = summary.placeName
                    binding.tvPlaceCoords.text = String.format(
                        Locale.US,
                        "Lat: %.4f, Lon: %.4f",
                        summary.latitude,
                        summary.longitude
                    )

                    val peakText = if (summary.peakDischarge != null && summary.peakDate != null) {
                        String.format(
                            Locale.US,
                            "Peak: %s – %.1f m³/s",
                            summary.peakDate,
                            summary.peakDischarge
                        )
                    } else {
                        "Peak: No peak data"
                    }
                    binding.tvPeakInfo.text = peakText

                    val todayText = if (summary.todayDischarge != null) {
                        String.format(Locale.US, "Today: %.1f m³/s", summary.todayDischarge)
                    } else {
                        "Today: No data for today"
                    }
                    binding.tvTodayInfo.text = todayText

                    adapter.submitList(summary.dailyItems)
                }
                is ForecastUiState.Error -> {
                    binding.layoutLoading.visibility = View.GONE
                    binding.scrollContent.visibility = View.GONE
                    binding.layoutError.visibility = View.VISIBLE
                    binding.tvErrorMsg.text = state.message
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
