package com.upro.byyum.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.upro.byyum.UproApplication
import com.upro.byyum.databinding.FragmentDashboardBinding
import com.upro.byyum.ui.addmonitor.AddMonitorActivity

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModelFactory((requireActivity().application as UproApplication).repository)
    }

    private lateinit var adapter: MonitorAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeData()

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddMonitorActivity::class.java))
        }

        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun setupRecyclerView() {
        adapter = MonitorAdapter { monitor ->
            val intent = Intent(requireContext(), com.upro.byyum.ui.detail.MonitorDetailActivity::class.java)
            intent.putExtra("monitor_id", monitor.id)
            startActivity(intent)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun observeData() {
        viewModel.monitors.observe(viewLifecycleOwner) { monitors ->
            adapter.submitList(monitors)
            val isEmpty = monitors.isEmpty()
            binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }

        viewModel.healthyCount.observe(viewLifecycleOwner) { binding.tvHealthy.text = it.toString() }
        viewModel.downCount.observe(viewLifecycleOwner) { binding.tvDown.text = it.toString() }
        viewModel.errorCount.observe(viewLifecycleOwner) { binding.tvError.text = it.toString() }
        viewModel.totalCount.observe(viewLifecycleOwner) { binding.tvTotal.text = it.toString() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
