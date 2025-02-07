/**
 * Please follow code style when editing project
 * Please follow principles of clean architecture
 * Created 2023 by Roman Kryvolapov
 **/
package com.digital.sofia.ui.fragments.main.documents.preview

import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.digital.sofia.databinding.FragmentDocumentPreviewBinding
import com.digital.sofia.domain.utils.LogUtil.logDebug
import com.digital.sofia.domain.utils.LogUtil.logError
import com.digital.sofia.extensions.launchInScope
import com.digital.sofia.models.common.StringSource
import com.digital.sofia.ui.fragments.base.BaseFragment
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel

class DocumentPreviewFragment :
    BaseFragment<FragmentDocumentPreviewBinding, DocumentPreviewViewModel>() {

    companion object {
        private const val TAG = "DocumentPreviewFragmentTag"
    }

    override fun getViewBinding() = FragmentDocumentPreviewBinding.inflate(layoutInflater)

    override val viewModel: DocumentPreviewViewModel by viewModel()

    private val args: DocumentPreviewFragmentArgs by navArgs()

    override fun setupControls() {
        binding.refreshLayout.setOnRefreshListener(viewModel::downloadDocument)
        binding.customToolbar.navigationClickListener = {
            logDebug("customToolbar navigationClickListener", TAG)
            onBackPressed()
        }
        binding.errorView.actionOneClickListener = {
            logDebug("errorView actionOneClickListener", TAG)
            viewModel.downloadDocument()
        }
        binding.errorView.actionTwoClickListener = {
            logDebug("errorView actionTwoClickListener", TAG)
            onBackPressed()
        }
    }

    override fun onCreated() {
        try {
            val documentFormIOId = args.documentFormIOId
            logDebug("pdfView from documentFormIOId: $documentFormIOId", TAG)
            if (documentFormIOId.isEmpty()) {
                showErrorState(
                    title = StringSource.Text("Document url not found"),
                    showActionOneButton = false,
                )
                return
            }
            viewModel.setDocumentFormIOId(documentFormIOId = documentFormIOId)
            viewModel.downloadDocument()
        } catch (e: IllegalStateException) {
            logError("loadWebPage Exception: ${e.message}", e, TAG)
        }
    }

    override fun subscribeToLiveData() {
        viewModel.documentPdfLiveData.onEach {
            logDebug("documentPdfLiveData file: ${it?.absoluteFile}", TAG)
            if (it == null) {
                logError("documentPdfLiveData document == null", TAG)
                return@onEach
            }
            binding.refreshLayout.isRefreshing = false
            binding.pdfView.fromFile(it)
                .enableSwipe(false)
                .enableDoubletap(true)
                .enableAntialiasing(true)
                .enableAnnotationRendering(true)
                .onError {
                    logError("pdfView onError: ${it.message}", TAG)
                    showErrorState(
                        title = StringSource.Text("Error open document"),
                        showActionOneButton = false,
                    )
                }
                .load()
        }.launchInScope(lifecycleScope)
    }

}