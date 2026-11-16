package com.example.androiddemo.ui.places

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.androiddemo.R
import com.example.androiddemo.databinding.FragmentAddPlaceBinding
import com.example.androiddemo.location.LocationHelper
import java.util.Locale

class AddPlaceFragment : Fragment() {

    private var _binding: FragmentAddPlaceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddPlaceViewModel by viewModels()
    private lateinit var locationHelper: LocationHelper

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                fetchCurrentLocation()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Location permission denied. Please enter coordinates manually.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPlaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationHelper = LocationHelper(requireContext())

        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = {
                    Toast.makeText(requireContext(), "Location saved successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                },
                onFailure = { error ->
                    Toast.makeText(requireContext(), error.message ?: "Failed to save location", Toast.LENGTH_LONG).show()
                }
            )
        }

        binding.btnUseGps.setOnClickListener {
            checkAndRequestLocationPermission()
        }

        binding.btnSavePlace.setOnClickListener {
            val name = binding.etName.text?.toString() ?: ""
            val lat = binding.etLat.text?.toString() ?: ""
            val lon = binding.etLon.text?.toString() ?: ""
            viewModel.savePlace(name, lat, lon)
        }
    }

    private fun checkAndRequestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                fetchCurrentLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(
                    requireContext(),
                    "Location access is required to automatically populate your coordinates.",
                    Toast.LENGTH_LONG
                ).show()
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            else -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun fetchCurrentLocation() {
        Toast.makeText(requireContext(), "Fetching current GPS location...", Toast.LENGTH_SHORT).show()
        locationHelper.getCurrentLocation(
            onSuccess = { location ->
                if (location != null) {
                    if (binding.etName.text.isNull_or_blank()) {
                        binding.etName.setText(getString(R.string.label_my_location))
                    }
                    binding.etLat.setText(String.format(Locale.US, "%.4f", location.latitude))
                    binding.etLon.setText(String.format(Locale.US, "%.4f", location.longitude))
                    Toast.makeText(requireContext(), "GPS Location updated!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "GPS location unavailable. Ensure GPS is enabled or enter coordinates manually.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            onFailure = { exception ->
                Toast.makeText(
                    requireContext(),
                    "Failed to fetch GPS location: ${exception.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    private fun CharSequence?.isNull_or_blank(): Boolean {
        return this == null || this.trim().isEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
