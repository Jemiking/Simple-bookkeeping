package com.example.myapplication.presentation.account.archive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentArchiveAccountBinding
import com.example.myapplication.domain.model.Account
import com.example.myapplication.presentation.account.AccountAdapter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ArchiveAccountFragment : Fragment() {

    private var _binding: FragmentArchiveAccountBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ArchiveAccountViewModel by viewModels()
    private val activeAccountsAdapter = AccountAdapter(::onActiveAccountClick)
    private val archivedAccountsAdapter = AccountAdapter(::onArchivedAccountClick)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArchiveAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeState()
    }

    private fun setupUI() {
        // 设置工具栏
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // 设置活跃账户列表
        binding.recyclerActiveAccounts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = activeAccountsAdapter
        }

        // 设置归档账户列表
        binding.recyclerArchivedAccounts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = archivedAccountsAdapter
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    // 更新加载状态
                    binding.progressBar.isVisible = state.isLoading

                    // 更新账户列表
                    activeAccountsAdapter.submitList(state.activeAccounts)
                    archivedAccountsAdapter.submitList(state.archivedAccounts)

                    // 显示错误信息
                    state.error?.let { error ->
                        Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG)
                            .setAction("确定") {
                                viewModel.onEvent(ArchiveAccountEvent.DismissError)
                            }
                            .show()
                    }
                }
            }
        }
    }

    private fun onActiveAccountClick(account: Account) {
        viewModel.onEvent(ArchiveAccountEvent.ArchiveAccount(account))
    }

    private fun onArchivedAccountClick(account: Account) {
        viewModel.onEvent(ArchiveAccountEvent.UnarchiveAccount(account))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 