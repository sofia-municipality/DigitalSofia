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
    
    @StateObject private var viewModel = DocumentsViewModel(statuses: [.signing])
    @State private var openDocument = false
    @State private var openETSetup = false
    @State private var openEditProfile = false
    
    @State private var selectedDocument: DocumentModel?
    @State private var openDocumentViewModel: OpenDocumentViewModel?
    
    private let rowHeight: CGFloat = AppConfig.Dimensions.Standart.rowHeight * 1.6
    private let maxNumberOrRows = 5
    
    var body: some View {
        VStack(spacing: 0) {
            if networkMonitor.isConnected {
                logoView()
                
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
            } else {
                NoNetworkConnetionView() {
                    viewModel.refreshData()
                }
            }
        }
        .onAppear {
            viewModel.successfullyFetchedDocuments = {
                appState.hasPendingDocuments = viewModel.documents.count > 0
            }
            
            openDocumentViewModel = OpenDocumentViewModel(openDocumentUserDecision: { decision in
                switch decision {
                case .approved:
                    openDocumentViewModel?.sendDocumentStatus(transactionId: selectedDocument?.evrotrustTransactionId)
                default:
                    openDocument = false
                }
            }, openDocumentErrorHandler: { error in
                appState.alertItem = AlertProvider.errorAlertWithCompletion(message: error.description, completion: {
                    if openDocument { openDocument = false }
                })
                
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
            }, successfullySignedDocument: {
                appState.alertItem = AlertProvider.errorAlertWithCompletion(message: AppConfig.UI.Alert.successfullySignedDocumentAlertText.localized, completion: {
                    if openDocument { openDocument = false }
                    
                    DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
                        viewModel.refreshData()
                    }
                })
            })
        }
        .refreshable {
            viewModel.refreshData()
        }
        .onChange(of: viewModel.networkError) { newValue in
            if let description = newValue {
                appState.alertItem = AlertProvider.errorAlert(message: description)
            }
        }
        .alert(item: $appState.alertItem) { alertItem in
            AlertProvider.getAlertFor(alertItem: alertItem)
        }
        .onChange(of: viewModel.documents, perform: { newValue in
            appState.hasPendingDocuments = newValue.count > 0
        })
        .background(DSColors.background)
        .task {
            viewModel.loadData()
        }
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
        }
    }
}

struct HomeView_Previews: PreviewProvider {
    static var previews: some View {
        PendingDocumentsView()
    }
}
