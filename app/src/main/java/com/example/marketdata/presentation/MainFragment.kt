package com.example.marketdata.presentation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.marketdata.R
import com.example.marketdata.databinding.FragmentMainBinding
import com.example.marketdata.data.network.RetrofitInstance
import com.example.marketdata.data.repository.MarketDataRepositoryImpl
import com.example.marketdata.domain.model.Instrument
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var validSymbols: List<String>

    private val viewModel: MarketDataViewModel by viewModels {
        MarketDataViewModelFactory(MarketDataRepositoryImpl(RetrofitInstance.api))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupChart()

        // Set initial value for symbolInput
        val defaultSymbol = getString(R.string.default_symbol)
        binding.symbolInput.setText(defaultSymbol)

        // Automatically subscribe to market data when the fragment is loaded
        subscribeToMarketData(defaultSymbol)

        // Observe loading state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                if (isLoading) {
                    binding.loadingOverlay.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.VISIBLE
                } else {
                    binding.loadingOverlay.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                }
            }
        }

        // Set up autocomplete for symbolInput and populate valid symbols
        viewLifecycleOwner.lifecycleScope.launch {
            validSymbols = viewModel.getAllSymbols()  // Get all symbols from ViewModel
            setupAutoComplete(validSymbols)
        }

        // Set up the subscribe button click listener with validation
        binding.subscribeButton.setOnClickListener {
            val symbol = binding.symbolInput.text.toString().trim()
            if (symbol.isNotEmpty() && validSymbols.contains(symbol)) {
                subscribeToMarketData(symbol)
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.select_symbol_message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Observe symbol updates
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.symbol.collectLatest { symbol ->
                binding.symbolText.text = symbol
            }
        }

        // Observe market data updates
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.marketData.collectLatest { data ->
                if (data != null) {
                    binding.priceText.text = data.price.toString()
                    binding.timeText.text = data.timestamp
                } else {
                    binding.priceText.text = getString(R.string.data_unavailable)
                    binding.timeText.text = getString(R.string.time_unavailable)
                }
            }
        }

        // Observe historical data for chart updates
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.historicalData.collectLatest { data ->
                if (data.isNotEmpty()) {
                    updateChart(data)
                }
            }
        }
    }

    private fun setupAutoComplete(symbols: List<String>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, symbols)
        binding.symbolInput.setAdapter(adapter)
        binding.symbolInput.setOnItemClickListener { _, _, position, _ ->
            val selectedSymbol = adapter.getItem(position)
            binding.symbolInput.setText(selectedSymbol)
        }
    }

    private fun subscribeToMarketData(symbol: String) {
        viewModel.fetchMarketDataForSymbol(symbol)

        // Get instrumentId based on symbol and fetch historical data
        viewLifecycleOwner.lifecycleScope.launch {
            val instrumentId = viewModel.getInstrumentIdForSymbol(symbol)
            instrumentId?.let {
                viewModel.fetchHistoricalData(it)
            } ?: Log.e("MainFragment", "Instrument ID for $symbol not found.")
        }
    }

    private fun setupChart() {
        binding.chart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(true)

            // Display text when data is unavailable
            setNoDataText(getString(R.string.no_data_text))

            // Configure the X-axis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 86400f // One day in seconds
            xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val date = ZonedDateTime.ofInstant(
                        java.time.Instant.ofEpochSecond(value.toLong()),
                        ZoneId.systemDefault()
                    )
                    return date.format(DateTimeFormatter.ofPattern("dd MMM"))
                }
            }

            axisRight.isEnabled = false  // Disable right axis
            legend.isEnabled = false     // Disable legend if not needed
        }
    }

    private fun updateChart(data: List<Instrument>) {
        val entries = data.mapNotNull { instrument ->
            instrument.t?.let { time ->
                instrument.c?.let { closingPrice ->
                    Entry(
                        ZonedDateTime.parse(time).toEpochSecond().toFloat(),
                        closingPrice.toFloat()
                    )
                }
            }
        }

        val lineDataSet = LineDataSet(entries, "Closing Prices").apply {
            setDrawCircles(false)
            lineWidth = 2f
        }

        val lineData = LineData(lineDataSet)
        binding.chart.data = lineData
        binding.chart.invalidate() // Refresh the chart
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}