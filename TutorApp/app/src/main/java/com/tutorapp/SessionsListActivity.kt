package com.tutorapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tutorapp.adapters.SessionsAdapter
import com.tutorapp.databinding.ActivitySessionsListBinding
import com.tutorapp.models.BookedSession
import com.tutorapp.viewModels.CalendarViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SessionsListActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySessionsListBinding
    private val viewModel: CalendarViewModel by viewModels()
    private lateinit var sessionsAdapter: SessionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySessionsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        loadSessions()
    }

    private fun setupRecyclerView() {
        sessionsAdapter = SessionsAdapter { session ->
            navigateToSessionDetail(session)
        }
        binding.recyclerViewSessions.apply {
            layoutManager = LinearLayoutManager(this@SessionsListActivity)
            adapter = sessionsAdapter
        }
    }

    private fun setupObservers() {
        viewModel.sessionsForSelectedDate.observe(this) { sessions ->
            updateUI(sessions)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.isOffline.observe(this) { isOffline ->
            if (isOffline) {
                Toast.makeText(this, "You are offline. Showing cached data.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadSessions() {
        val userId = intent.getIntExtra("userId", -1)
        if (userId == -1) {
            Toast.makeText(this, "Error: User ID not provided", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        viewModel.loadBookedSessions(userId)
        
        // Get the selected date from intent or use current date
        val selectedDateStr = intent.getStringExtra("selectedDate")
        val selectedDate = if (selectedDateStr != null) {
            LocalDate.parse(selectedDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        } else {
            LocalDate.now()
        }

        lifecycleScope.launch {
            viewModel.selectDateAndLoadSessions(selectedDate)
        }
    }

    private fun updateUI(sessions: List<BookedSession>) {
        if (sessions.isEmpty()) {
            binding.emptyView.visibility = View.VISIBLE
            binding.recyclerViewSessions.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.GONE
            binding.recyclerViewSessions.visibility = View.VISIBLE
            sessionsAdapter.submitList(sessions)
        }
    }

    private fun navigateToSessionDetail(session: BookedSession) {
        val intent = Intent(this, SessionDetailActivity::class.java).apply {
            putExtra("session", session)
        }
        startActivity(intent)
    }
} 