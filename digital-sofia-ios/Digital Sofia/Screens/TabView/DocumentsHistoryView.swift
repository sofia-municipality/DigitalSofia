//
//  DocumentsHistoryView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 5.04.23.
//

import SwiftUI

struct DocumentsHistoryView: View {
    @EnvironmentObject var appState: AppState
    @EnvironmentObject var networkMonitor: NetworkMonitor
    
    @StateObject private var viewModel = DocumentsViewModel(statuses: [.signed, .expired, .rejected, .failed])
    @State private var openDocument = false
    @State private var openDownloadedDocument = false
    @State private var selectedDocumentURL: URL?
    @State private var isLoading = false
    @State private var shouldScrollToTop = false
    
    var body: some View {
        LoadingStack(isPresented: $isLoading) {
            VStack(spacing: 0) {
                headerView()
                
                NetworkStack(content: {
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
                }, centered: true)
                .environmentObject(networkMonitor)
            }
        }
        .refreshable {
            viewModel.refreshData()
        }
        .onChange(of: viewModel.networkError) { newValue in
            appState.alertItem = AlertProvider.errorAlert(message: newValue ?? "")
        }
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
    
    private func headerView() -> some View {
        HStack {
            Image(ImageProvider.TabView.documentsTabLogo)
            
            Text(AppConfig.UI.Titles.Tabbar.docs.localized)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.XXXL))
                .foregroundColor(DSColors.Text.indigoDark)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(.leading, AppConfig.Dimensions.Padding.standart + AppConfig.Dimensions.Padding.large)
        .padding(.top, AppConfig.Dimensions.Padding.large)
        .padding(.bottom, AppConfig.Dimensions.Padding.medium)
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
        ScrollViewReader { reader in
            ScrollView {
                ScrollerToTop(reader: reader, scrollOnChange: $shouldScrollToTop)
                
                LazyVStack(spacing: AppConfig.Dimensions.Padding.medium) {
                    ForEach(viewModel.documents, id: \.self) { item in
                        DocumentsListViewRow(document: item, downloadDocument: {
                            isLoading = true
                            NetworkManager.downloadFile(filename: item.fileName ?? "", service: APIService.downloadPDF(formioId: item.formioId ?? "")) { response in
                                isLoading = false
                                switch response {
                                case .failure(let error):
                                    appState.alertItem = AlertProvider.errorAlert(message: error.description)
                                case .success(let path):
                                    selectedDocumentURL = path
                                    openDownloadedDocument = true
                                }
                            }
                        })
                        .environmentObject(appState)
                        .onAppear {
                            if viewModel.documents.last == item {
                                viewModel.loadData()
                            }
                        }
                        .onTapGesture {
                            if item.formioId != nil {
                                selectedDocumentURL = URL(string: item.pdfPath)
                                openDocument = true
                            } else {
                                appState.alertItem = AlertProvider.generalAlert()
                            }
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
                .padding(.bottom, AppConfig.Dimensions.Padding.small)
            }
        }
    }
    
    private func navigation() -> some View {
        VStack {
            NavigationLink(destination: PDFWebViewer(url: selectedDocumentURL)
                .environmentObject(appState),
                           isActive: $openDocument) { EmptyView() }
            
            NavigationLink(destination: PDFKitView(pdfUrl: selectedDocumentURL),
                           isActive: $openDownloadedDocument) { EmptyView() }
        }
    }
}

struct History_Previews: PreviewProvider {
    static var previews: some View {
        DocumentsHistoryView()
    }
}
