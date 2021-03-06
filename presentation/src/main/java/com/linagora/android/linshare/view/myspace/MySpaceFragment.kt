/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 *
 * Copyright (C) 2020 LINAGORA
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Affero General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version,
 * provided you comply with the Additional Terms applicable for LinShare software by
 * Linagora pursuant to Section 7 of the GNU Affero General Public License,
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain the
 * display in the interface of the “LinShare™” trademark/logo, the "Libre & Free" mention,
 * the words “You are using the Free and Open Source version of LinShare™, powered by
 * Linagora © 2009–2020. Contribute to Linshare R&D by subscribing to an Enterprise
 * offer!”. You must also retain the latter notice in all asynchronous messages such as
 * e-mails sent with the Program, (ii) retain all hypertext links between LinShare and
 * http://www.linshare.org, between linagora.com and Linagora, and (iii) refrain from
 * infringing Linagora intellectual property rights over its trademarks and commercial
 * brands. Other Additional Terms apply, see
 * <http://www.linshare.org/licenses/LinShare-License_AfferoGPL-v3.pdf>
 * for more details.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for
 * more details.
 * You should have received a copy of the GNU Affero General Public License and its
 * applicable Additional Terms for LinShare along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General Public License version
 *  3 and <http://www.linshare.org/licenses/LinShare-License_AfferoGPL-v3.pdf> for
 *  the Additional Terms applicable to LinShare software.
 */

package com.linagora.android.linshare.view.myspace

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import arrow.core.Either
import com.google.android.material.snackbar.Snackbar
import com.linagora.android.linshare.R
import com.linagora.android.linshare.databinding.FragmentMySpaceBinding
import com.linagora.android.linshare.domain.model.OperatorType
import com.linagora.android.linshare.domain.model.document.Document
import com.linagora.android.linshare.domain.model.order.OrderListConfigurationType
import com.linagora.android.linshare.domain.model.properties.PreviousUserPermissionAction.DENIED
import com.linagora.android.linshare.domain.model.search.QueryString
import com.linagora.android.linshare.domain.usecases.myspace.ContextMenuClick
import com.linagora.android.linshare.domain.usecases.myspace.DocumentDetailsClick
import com.linagora.android.linshare.domain.usecases.myspace.DocumentItemClick
import com.linagora.android.linshare.domain.usecases.myspace.DownloadClick
import com.linagora.android.linshare.domain.usecases.myspace.RemoveClick
import com.linagora.android.linshare.domain.usecases.myspace.RemoveDocumentSuccessViewState
import com.linagora.android.linshare.domain.usecases.myspace.ShareItemClick
import com.linagora.android.linshare.domain.usecases.myspace.UploadButtonBottomBarClick
import com.linagora.android.linshare.domain.usecases.myspace.DuplicateDocumentMySpaceClick
import com.linagora.android.linshare.domain.usecases.myspace.CopyInMySpaceSuccess
import com.linagora.android.linshare.domain.usecases.myspace.CopyInMySpaceFailure
import com.linagora.android.linshare.domain.usecases.myspace.RenameDocumentMySpaceClick
import com.linagora.android.linshare.domain.usecases.myspace.RenameDocumentSuccess
import com.linagora.android.linshare.domain.usecases.myspace.RenameDocumentFailure

import com.linagora.android.linshare.domain.usecases.order.GetOrderListConfigurationSuccess
import com.linagora.android.linshare.domain.usecases.search.CloseSearchView
import com.linagora.android.linshare.domain.usecases.search.OpenSearchView
import com.linagora.android.linshare.domain.usecases.sharedspace.CopyToSharedSpaceFailure
import com.linagora.android.linshare.domain.usecases.sharedspace.CopyToSharedSpaceSuccess
import com.linagora.android.linshare.domain.usecases.sharedspace.OnOrderByRowItemClick
import com.linagora.android.linshare.domain.usecases.sharedspace.OpenOrderByDialog
import com.linagora.android.linshare.domain.usecases.utils.Failure
import com.linagora.android.linshare.domain.usecases.utils.Failure.CannotExecuteWithoutNetwork
import com.linagora.android.linshare.domain.usecases.utils.Success
import com.linagora.android.linshare.model.parcelable.QueryStringParcelable
import com.linagora.android.linshare.model.parcelable.SelectedDestinationInfoForOperateDocument
import com.linagora.android.linshare.model.parcelable.toDocument
import com.linagora.android.linshare.model.parcelable.toParcelable
import com.linagora.android.linshare.model.parcelable.toQueryString
import com.linagora.android.linshare.model.parcelable.toSharedSpaceId
import com.linagora.android.linshare.model.parcelable.toWorkGroupNodeId
import com.linagora.android.linshare.model.permission.PermissionResult
import com.linagora.android.linshare.model.properties.RuntimePermissionRequest.ShouldShowWriteStorage
import com.linagora.android.linshare.util.Constant
import com.linagora.android.linshare.util.dismissDialogFragmentByTag
import com.linagora.android.linshare.util.dismissKeyboard
import com.linagora.android.linshare.util.filterNetworkViewEvent
import com.linagora.android.linshare.util.getViewModel
import com.linagora.android.linshare.util.openFilePicker
import com.linagora.android.linshare.util.showKeyboard
import com.linagora.android.linshare.view.Event
import com.linagora.android.linshare.view.MainActivityViewModel
import com.linagora.android.linshare.view.MainNavigationFragment
import com.linagora.android.linshare.view.Navigation.MainNavigationType
import com.linagora.android.linshare.view.Navigation.UploadType.INSIDE_APP
import com.linagora.android.linshare.view.OpenFilePickerRequestCode
import com.linagora.android.linshare.view.WriteExternalPermissionRequestCode
import com.linagora.android.linshare.view.base.event.CopyDocumentToSharedSpaceClick
import com.linagora.android.linshare.view.share.ShareFragment.Companion.SHARE_DOCUMENT_BUNDLE_KEY
import com.linagora.android.linshare.view.upload.UploadFragmentArgs
import com.linagora.android.linshare.view.widget.errorLayout
import com.linagora.android.linshare.view.widget.withLinShare
import kotlinx.android.synthetic.main.fragment_my_space.swipeLayoutMySpace
import org.slf4j.LoggerFactory

class MySpaceFragment : MainNavigationFragment() {

    private val mainActivityViewModel: MainActivityViewModel
            by activityViewModels { viewModelFactory }

    private lateinit var mySpaceViewModel: MySpaceViewModel

    private lateinit var binding: FragmentMySpaceBinding

    private val args: MySpaceFragmentArgs by navArgs()

    companion object {
        private val LOGGER = LoggerFactory.getLogger(MySpaceFragment::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentMySpaceBinding.inflate(inflater, container, false)
        initViewModel(binding)
        return binding.root
    }

    private fun initViewModel(binding: FragmentMySpaceBinding) {
        mySpaceViewModel = getViewModel(viewModelFactory)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.internetAvailable = mainActivityViewModel.internetAvailable
        binding.viewModel = mySpaceViewModel
        binding.searchInfo = args.searchInfo

        observeFunctionality()
        observeViewState()
        observeRequestPermission()
    }

    private fun observeFunctionality() {
        viewLifecycleOwner.lifecycle.addObserver(mySpaceViewModel.functionalityObserver)
    }

    private fun observeViewState() {
        mySpaceViewModel.viewState.observe(viewLifecycleOwner, Observer { it.fold(
            ifLeft = this@MySpaceFragment::reactToFailure,
            ifRight = this@MySpaceFragment::reactToSuccess
        ) })
    }

    private fun reactToFailure(failure: Failure) {
        when (failure) {
            is CannotExecuteWithoutNetwork -> handleCannotExecuteWithoutNetwork(failure.operatorType)
            is CopyToSharedSpaceFailure -> errorSnackBar(getString(R.string.copy_to_shared_space_fail_message)).show()
            is CopyInMySpaceFailure -> errorSnackBar(getString(R.string.copy_failed)).show()
            is RenameDocumentFailure -> handleRenameDocumentFailure()
        }
    }

    private fun reactToSuccess(success: Success) {
        when (success) {
            is Success.ViewEvent -> reactToViewEvent(success)
            is RemoveDocumentSuccessViewState -> getAllDocuments()
            is CopyToSharedSpaceSuccess -> successSnackBar(getString(R.string.copy_to_shared_space_message)).show()
            is GetOrderListConfigurationSuccess -> handleGetOrderListConfigSuccess(success.orderListConfigurationType)
            is CopyInMySpaceSuccess -> {
                successSnackBar(getString(R.string.copy_success, success.documents.first().name)).show()
                getAllDocuments()
            }
            is RenameDocumentSuccess -> getAllDocuments()
        }
    }

    private fun reactToViewEvent(viewEvent: Success.ViewEvent) {
        when (val filteredViewEvent = viewEvent.filterNetworkViewEvent(mySpaceViewModel.internetAvailable.value)) {
            is Success.CancelViewEvent -> handleCannotExecuteViewEvent(filteredViewEvent.operatorType)
            else -> handleViewEvent(filteredViewEvent)
        }
    }

    private fun handleViewEvent(viewEvent: Success.ViewEvent) {
        when (viewEvent) {
            is ContextMenuClick -> showContextMenu(viewEvent.document)
            is DocumentItemClick -> navigateToDetails(viewEvent.document)
            is DownloadClick -> handleDownloadDocument(viewEvent.document)
            is UploadButtonBottomBarClick -> openFilePicker()
            is RemoveClick -> confirmRemoveDocument(viewEvent.document)
            is OpenSearchView -> handleOpenSearch()
            is CloseSearchView -> handleCloseSearch()
            is ShareItemClick -> navigateToShare(viewEvent.document)
            is CopyDocumentToSharedSpaceClick -> selectCopyDestination(viewEvent.document)
            is DocumentDetailsClick -> navigateToDetails(viewEvent.document)
            is OpenOrderByDialog -> showOrderByDialog()
            is OnOrderByRowItemClick -> handleOrderRowItemClick(viewEvent.orderListConfigurationType)
            is DuplicateDocumentMySpaceClick -> duplicateDocument(viewEvent.document)
            is RenameDocumentMySpaceClick -> handleRenameDocumentClick(viewEvent.document)
        }
        mySpaceViewModel.dispatchResetState()
    }

    private fun confirmRemoveDocument(document: Document) {
        dismissContextMenu()
        ConfirmRemoveDocumentDialog(
            document = document,
            title = getString(R.string.confirm_delete_file, document.name),
            negativeText = getString(R.string.cancel),
            positiveText = getString(R.string.delete),
            onPositiveCallback = { handleRemoveDocument(document) }
        ).show(childFragmentManager, "confirm_remove_document_dialog")
    }

    private fun showContextMenu(document: Document) {
        binding.includeSearchContainer.searchView.clearFocus()
        MySpaceContextMenuDialog(document)
            .show(childFragmentManager, MySpaceContextMenuDialog.TAG)
    }

    private fun handleRemoveDocument(document: Document) {
        mySpaceViewModel.removeDocument(document)
    }

    private fun observeRequestPermission() {
        mainActivityViewModel.shouldShowPermissionRequestState.observe(viewLifecycleOwner, Observer {
            if (it is ShouldShowWriteStorage) {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    WriteExternalPermissionRequestCode.code
                )
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LOGGER.info("onViewCreated")
        setUpSwipeRefreshLayout()
        setUpSearchView()
        handleArguments()
        getOrderListConfiguration()
    }

    override fun onResume() {
        super.onResume()
        if (binding.includeSearchContainer.searchView.query.isNotEmpty()) {
            mySpaceViewModel.searchAction.openSearchView()
        }
    }

    private fun setUpSwipeRefreshLayout() {
        swipeLayoutMySpace.setColorSchemeResources(R.color.colorPrimary)
    }

    private fun getOrderListConfiguration() {
        mySpaceViewModel.getOrderListConfiguration()
    }

    private fun getAllDocuments() {
        LOGGER.info("getAllDocuments")
        mySpaceViewModel.getAllDocuments()
    }

    private fun handleArguments() {
        args.selectedDestinationInfoForOperate
            ?.let(this@MySpaceFragment::operateSelectedDestinationInfo)

        args.searchInfo?.toQueryString()?.let {
            mySpaceViewModel.searchAction.openSearchView() }
    }

    private fun operateSelectedDestinationInfo(selectedDestinationInfo: SelectedDestinationInfoForOperateDocument) {
        LOGGER.info("operateSelectedDestinationInfo(): $selectedDestinationInfo")
        when (selectedDestinationInfo.operatorPickDestination) {
            Event.OperatorPickDestination.COPY -> copyToSharedSpace(selectedDestinationInfo)
        }
    }

    private fun handleDownloadDocument(document: Document) {
        dismissContextMenu()
        when (mainActivityViewModel.checkWriteStoragePermission(requireContext())) {
            PermissionResult.PermissionGranted -> { download(document) }
            else -> { shouldRequestWriteStoragePermission() }
        }
    }

    private fun download(document: Document) {
        LOGGER.info("download() $document")
        mainActivityViewModel.currentAuthentication.value
            ?.let { authentication ->
                mySpaceViewModel.downloadDocument(authentication.credential, authentication.token, document)
            }
    }

    private fun shouldRequestWriteStoragePermission() {
        mainActivityViewModel.shouldShowWriteStoragePermissionRequest(requireActivity())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        LOGGER.info("onRequestPermissionsResult() $requestCode")
        when (requestCode) {
            WriteExternalPermissionRequestCode.code -> {
                Either.cond(
                    test = grantResults.all { grantResult -> grantResult == PackageManager.PERMISSION_GRANTED },
                    ifTrue = { mySpaceViewModel.getDownloadingDocument()?.let { download(it) } },
                    ifFalse = { mainActivityViewModel.setActionForWriteStoragePermissionRequest(DENIED) }
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        LOGGER.info("onActivityResult() $requestCode - $data")
        requestCode.takeIf { it == OpenFilePickerRequestCode.code }
            ?.let { data?.data }
            ?.let(this@MySpaceFragment::navigateToUpload)
    }

    private fun dismissContextMenu() {
        childFragmentManager.dismissDialogFragmentByTag(MySpaceContextMenuDialog.TAG)
    }

    private fun handleGetOrderListConfigSuccess(orderListConfigurationType: OrderListConfigurationType) {
        mySpaceViewModel.setCurrentOrderListConfigurationType(orderListConfigurationType)
        getAllDocuments()
    }

    private fun showOrderByDialog() {
        dismissListOrderByDialog()
        MySpaceOrderByDialog().show(childFragmentManager, MySpaceOrderByDialog.TAG)
    }

    private fun dismissListOrderByDialog() {
        childFragmentManager.dismissDialogFragmentByTag(MySpaceOrderByDialog.TAG)
    }

    private fun handleOrderRowItemClick(orderListConfigurationType: OrderListConfigurationType) {
        dismissListOrderByDialog()
        mySpaceViewModel.persistOrderListConfiguration(orderListConfigurationType)
    }

    private fun handleCannotExecuteWithoutNetwork(operatorType: OperatorType) {
        val messageId = when (operatorType) {
            is OperatorType.SwiftRefresh -> R.string.can_not_refresh_without_network
            else -> R.string.can_not_process_without_network
        }
        Snackbar.make(binding.root, getString(messageId), Snackbar.LENGTH_SHORT)
            .errorLayout(requireContext())
            .setAnchorView(binding.mySpaceUploadButton)
            .show()
        mySpaceViewModel.dispatchResetState()
    }

    private fun handleCannotExecuteViewEvent(operatorType: OperatorType) {
        val messageId = when (operatorType) {
            is OperatorType.DeleteDocument -> R.string.can_not_delete_file_while_offline
            else -> R.string.can_not_process_without_network
        }

        dismissContextMenu()
        Snackbar.make(binding.root, getString(messageId), Snackbar.LENGTH_SHORT)
            .errorLayout(requireContext())
            .setAnchorView(binding.mySpaceUploadButton)
            .show()
        mySpaceViewModel.dispatchResetState()
    }

    override fun onUnAuthenticatedState() {
        LOGGER.info("onUnAuthenticatedState()")
        navigateToReload()
    }

    override fun onInvalidAuthentication() {
        LOGGER.info("onInvalidAuthentication()")
        navigateToReload()
    }

    private fun selectCopyDestination(document: Document) {
        val actionToSelectDestination = MySpaceFragmentDirections
            .navigateToCopyMySpaceDestinationFragment(
                document.toParcelable(),
                searchInfo = QueryStringParcelable(binding.includeSearchContainer.searchView.query.toString())
            )

        findNavController().navigate(actionToSelectDestination)
    }

    private fun errorSnackBar(message: String): Snackbar {
        return Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .errorLayout(requireContext())
            .setAnchorView(binding.mySpaceUploadButton)
    }

    private fun successSnackBar(message: String): Snackbar {
        return Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .withLinShare(requireContext())
            .setAnchorView(binding.mySpaceUploadButton)
    }

    private fun copyToSharedSpace(selectedDestinationInfo: SelectedDestinationInfoForOperateDocument) {
        mySpaceViewModel.copyDocumentToSharedSpace(
            selectedDestinationInfo.documentParcelable.toDocument(),
            selectedDestinationInfo.selectedDestinationInfo.sharedSpaceDestinationInfo.sharedSpaceIdParcelable.toSharedSpaceId(),
            selectedDestinationInfo.selectedDestinationInfo.parentDestinationInfo.parentNodeId.toWorkGroupNodeId()
        )
    }

    private fun navigateToReload() {
        val action = MySpaceFragmentDirections
            .actionNavigationMySpaceToMainFragment(MainNavigationType.RELOAD)
        findNavController().navigate(action)
    }

    private fun navigateToUpload(uri: Uri) {
        val bundle = UploadFragmentArgs(INSIDE_APP, uri).toBundle()
        findNavController().navigate(R.id.uploadFragment, bundle)
    }

    private fun handleOpenSearch() {
        binding.apply {
            includeSearchContainer.searchContainer.visibility = View.VISIBLE
            includeSearchContainer.searchView.requestFocus()
            mySpaceBottomBar.visibility = View.GONE

            binding.searchInfo?.toQueryString()?.let {
                includeSearchContainer.searchView.setQuery(it.value, Constant.NOT_SUBMIT_TEXT)
                searchDocument(it.value)
            }
        }
    }

    private fun handleCloseSearch() {
        binding.apply {
            includeSearchContainer.searchContainer.visibility = View.GONE
            mySpaceBottomBar.visibility = View.VISIBLE
            includeSearchContainer.searchView.apply {
                setQuery(Constant.CLEAR_QUERY_STRING, Constant.NOT_SUBMIT_TEXT)
                clearFocus()
            }
        }
        binding.searchInfo = QueryStringParcelable(Constant.CLEAR_QUERY_STRING)
        getAllDocuments()
    }

    private fun navigateToShare(document: Document) {
        dismissContextMenu()
        val bundle = Bundle()
        bundle.putParcelable(SHARE_DOCUMENT_BUNDLE_KEY, document.toParcelable())
        findNavController().navigate(R.id.navigationShare, bundle)
    }

    private fun navigateToDetails(document: Document) {
        dismissContextMenu()
        val actionToDocumentDetails = MySpaceFragmentDirections
            .navigateToDocumentDetails(document.documentId.toParcelable())
        findNavController().navigate(actionToDocumentDetails)
    }

    private fun setUpSearchView() {
        binding.includeSearchContainer.searchView.apply {

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    this@apply.dismissKeyboard()
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    LOGGER.info("onQueryTextChange() $newText")
                    searchDocument(newText)
                    return true
                }
            })

            setOnQueryTextFocusChangeListener { view, hasFocus ->
                if (hasFocus && childFragmentManager.findFragmentByTag(MySpaceContextMenuDialog.TAG) === null) {
                    view.findFocus().showKeyboard()
                }
            }
        }
    }

    private fun searchDocument(query: String) {
        query.trim()
            .let(::QueryString)
            .let { sendQueryString(it) }
    }

    private fun sendQueryString(query: QueryString) {
        mySpaceViewModel.searchDocument(query)
    }

    private fun duplicateDocument(document: Document) {
        dismissContextMenu()
        mySpaceViewModel.duplicateDocumentMySpace(document)
    }

    private fun handleRenameDocumentClick(document: Document) {
        dismissContextMenu()
        RenameDocumentDialog(
            currentDocumentDialog = document,
            onRenameMySpaceDocument = mySpaceViewModel::renameDocument,
            onNewNameRequestChange = mySpaceViewModel::verifyNewName,
            viewState = mySpaceViewModel.viewState)
            .show(childFragmentManager, RenameDocumentDialog.TAG)
    }

    private fun handleRenameDocumentFailure() {
        Snackbar.make(binding.root, getString(R.string.common_error_occurred_message), Snackbar.LENGTH_SHORT)
            .errorLayout(requireContext())
            .setAnchorView(binding.mySpaceUploadButton)
            .show()
        mySpaceViewModel.dispatchResetState()
    }
}
