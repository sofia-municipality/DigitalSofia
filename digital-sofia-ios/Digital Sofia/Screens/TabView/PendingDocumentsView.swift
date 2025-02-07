//
//  HomeView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 25.04.23.
//

import SwiftUI

struct PendingDocumentsView: View {
    @EnvironmentObject var appState: AppState
    @EnvironmentObject var networkMonitor: NetworkMonitor
    
    @StateObject private var viewModel = DocumentsViewModel(statuses: [.signing, .delivering])
    @State private var openDocument = false
    @State private var openETSetup = false
    @State private var openEditProfile = false
    @State private var openETAuthenticationFailedView = false
    
    @State private var selectedDocument: DocumentModel?
    @State private var openDocumentViewModel: OpenDocumentViewModel?
    
    private let rowHeight: CGFloat = AppConfig.Dimensions.Standart.rowHeight * 1.6
    private let maxNumberOrRows = 5
    
    var body: some View {
        VStack(spacing: 0) {
            logoView()
            
            NetworkStack {
                navigation()
                
                if viewModel.isLoading {
                    loadingView()
                } else {
                    if viewModel.documents.isEmpty == true {
                        EmptyListView {
                            viewModel.refreshData()
                        }
                        
                        Spacer()
                    } else {
                        documentsList()
                    }
                }
            }
            .environmentObject(networkMonitor)
        }
        .onAppear {
            openDocumentViewModel = OpenDocumentViewModel(openDocumentUserDecision: { decision in
                if openDocument { openDocument = false }
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                    viewModel.isLoading = true
                    if selectedDocument?.docStatus == .delivering {
                        openDocumentViewModel?.sendReceipt(threadId: selectedDocument?.evrotrustThreadId)
                    } else {
                        openDocumentViewModel?.sendDocumentStatus(transactionId: selectedDocument?.evrotrustTransactionId)
                    }
                }
            }, openDocumentErrorHandler: { error in
                if let etError = error as? EvrotrustError, etError == .userNotReadyToSign {
                    documentStatusCompletion()
                    openETAuthenticationFailedView = true
                } else {
                    appState.alertItem = AlertProvider.errorAlertWithCompletion(message: error.baseDescription, completion: {
                        documentStatusCompletion()
                    })
                }
            }, checkUserStatusResult: { state in
                switch state {
                case .showDocument:
                    openDocument = true
                case .showEditProfile:
                    openEditProfile = true
                case .showSetupSDK:
                    appState.alertItem = AlertProvider.errorAlertWithCompletion(message: AppConfig.UI.Alert.openDocumentSdkSetupAlertText.localized,
                                                                                completion: {
                        openETSetup = true
                    })
                }
            }, receivedDocumentStatus: { response in
                let status = DocumentStatus(rawValue: response?.status ?? "") ?? .unsigned
                
                switch status {
                case .signed, .rejected:
                    let alertMessage = status == .signed ? AppConfig.UI.Alert.successfullySignedDocumentAlertText.localized
                    : AppConfig.UI.Alert.successfullyRejectedDocumentAlertText.localized
                    
                    appState.alertItem = AlertProvider.errorAlertWithCompletion(message: alertMessage, completion: {
                        documentStatusCompletion()
                    })
                default:
                    documentStatusCompletion()
                }
            }, verifyIdentityRequest: { _ in
            }, userClosedDocumentView: {
                if selectedDocument?.docStatus == .delivering {
                    openDocumentViewModel?.sendReceipt(threadId: selectedDocument?.evrotrustThreadId)
                } else {
                    documentStatusCompletion()
                }
            })
        }
        .refreshable {
            viewModel.refreshData()
        }
        .onChange(of: viewModel.networkError) { newValue in
            appState.alertItem = AlertProvider.errorAlert(message: newValue ?? "")
        }
        .onChange(of: viewModel.documents, perform: { newValue in
            appState.hasPendingDocuments = newValue.count > 0
        })
        .onChange(of: appState.refreshDocuments) { newValue in
            if newValue {
                viewModel.refreshData()
                appState.refreshDocuments = false
            }
        }
        .task {
            viewModel.loadData()
        }
        .log(view: self)
        .alert()
        .environmentObject(appState)
        .environmentObject(networkMonitor)
        .backgroundAndNavigation()
    }
    
    private func logoView() -> some View {
        VStack(spacing: 0) {
            Image(ImageProvider.logo)
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: UIScreen.main.bounds.width * 0.5, height: UIScreen.main.bounds.height * 0.15)
                .padding(.top, UIScreen.main.bounds.width * 0.15)
                .padding(.bottom, UIScreen.main.bounds.width * 0.2)
            
            Spacer()
            
            Text(AppConfig.UI.Titles.Screens.homeListTitle.localized)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.bold, size: DSFonts.FontSize.medium))
                .foregroundColor(DSColors.Text.indigoDark)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding([.leading, .trailing], AppConfig.Dimensions.Padding.XXL)
                .padding(.bottom, AppConfig.Dimensions.Padding.XL)
        }
        .frame(maxWidth: .infinity)
    }
    
    private func loadingView() -> some View {
        VStack {
            Spacer()
            
            LoadingLottieView(loopMode: .loop)
                .scaleEffect(0.2)
            
            Spacer()
        }
    }
    
    private func documentsList() -> some View {
        ScrollView {
            LazyVStack(spacing: 0) {
                ForEach(viewModel.documents, id: \.self) { item in
                    HomeListViewRow(text: item.fileName ?? "")
                        .frame(height: rowHeight)
                        .onAppear {
                            if viewModel.documents.last == item {
                                viewModel.loadData()
                            }
                        }
                        .onTapGesture {
                            selectedDocument = item
                            openDocumentViewModel?.checkUserStatus()
                        }
                }
                
                if viewModel.isLoadingMore {
                    HStack {
                        Spacer()
                        ProgressView()
                        Spacer()
                    }
                    .padding(EdgeInsets(top: AppConfig.Dimensions.Padding.small, leading: 0, bottom: AppConfig.Dimensions.Padding.small, trailing: 0))
                }
            }
            .frame(width: .infinity)
        }
        .frame(width: .infinity, height: (viewModel.documents.count < maxNumberOrRows ? CGFloat(viewModel.documents.count) : CGFloat(maxNumberOrRows)) * rowHeight)
        .padding(.bottom, AppConfig.Dimensions.Padding.large)
    }
    
    private func navigation() -> some View {
        VStack {
            NavigationLink(destination: openDocumentViewModel?.openDocument(transactionId: selectedDocument?.evrotrustTransactionId),
                           isActive: $openDocument) { EmptyView() }
            
            NavigationLink(destination: openDocumentViewModel?.openETSetup(),
                           isActive: $openETSetup) { EmptyView() }
            
            NavigationLink(destination: openDocumentViewModel?.openEditProfile(),
                           isActive: $openEditProfile) { EmptyView() }
            
            NavigationLink(destination: ETSdkAuthenticationFailedView(shouldDismiss: true, onReadyToSign: {
                openETAuthenticationFailedView = false
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                    documentStatusCompletion()
                }
            })
                .environmentObject(appState),
                           isActive: $openETAuthenticationFailedView) { EmptyView() }
        }
    }
    
    private func documentStatusCompletion() {
        if openEditProfile { openEditProfile = false }
        if openDocument { openDocument = false }
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
            if viewModel.isLoading { viewModel.isLoading = false }
            viewModel.refreshData()
        }
    }
}

struct HomeView_Previews: PreviewProvider {
    static var previews: some View {
        PendingDocumentsView()
    }
}
