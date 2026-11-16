package com.example.androiddemo.ui.places

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androiddemo.R
import com.example.androiddemo.databinding.FragmentPlacesBinding
import com.example.androiddemo.ui.forecast.ForecastViewModel

class PlacesFragment : Fragment() {

    private var _binding: FragmentPlacesBinding? = null
    private val binding get() = _binding!!

    private val placesViewModel: PlacesViewModel by viewModels()
    private val forecastViewModel: ForecastViewModel by activityViewModels()

    private lateinit var adapter: PlacesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlacesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        binding.btnAddPlaceHeader.setOnClickListener {
            findNavController().navigate(R.id.action_placesFragment_to_addPlaceFragment)
        }

        binding.btnAddFirstPlace.setOnClickListener {
            findNavController().navigate(R.id.action_placesFragment_to_addPlaceFragment)
        }
    }

    private fun setupRecyclerView() {
        adapter = PlacesAdapter(
            onSelectClick = { place ->
                forecastViewModel.selectPlace(place)
                Toast.makeText(requireContext(), "Selected ${place.name}", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            },
            onDeleteClick = { place ->
                placesViewModel.deletePlace(place)
                Toast.makeText(requireContext(), "Deleted ${place.name}", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvPlaces.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPlaces.adapter = adapter
    }

    private fun observeViewModel() {
        placesViewModel.places.observe(viewLifecycleOwner) { placeList ->
            if (placeList.isNullOrEmpty()) {
                binding.rvPlaces.visibility = View.GONE
                binding.layoutEmptyPlaces.visibility = View.VISIBLE
            } else {
                binding.rvPlaces.visibility = View.VISIBLE
                binding.layoutEmptyPlaces.visibility = View.GONE
                adapter.submitList(placeList)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
