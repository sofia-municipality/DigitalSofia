//
//  ConfirmDataShareView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 5.10.23.
//

import SwiftUI

struct ConfirmDataShareView: View {
    @EnvironmentObject var networkMonitor: NetworkMonitor
    @EnvironmentObject var appState: AppState
    @Environment(\.presentationMode) var presentationMode
    
    @State private var showWelcomeView = false
    @State private var etTransactionId = ""
    @State private var isLoading = false
    
    @State private var openDocumentViewModel: OpenDocumentViewModel?
    @State private var openDocument = false
    @State private var openETSetup = false
    @State private var openEditProfile = false
    
    @State private var welcomeViewModel = WelcomeUserViewModel()
    @State private var viewModel = DataShareViewModel()
    
    private let verticalPadding = AppConfig.Dimensions.Padding.XXXL
    private let horizontalPadding = AppConfig.Dimensions.Padding.XXXL
    
    var body: some View {
        LoadingStack(isPresented: $isLoading) {
            VStack(spacing: verticalPadding) {
                navigation()
                
                Text(AppConfig.UI.Text.shareDataTitleText.localized)
                    .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.XXXL))
                    .foregroundColor(DSColors.Text.indigoDark)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding([.top, .bottom], verticalPadding)
                
                Text(AppConfig.UI.Text.shareDataDetailsText.localized)
                    .multilineTextAlignment(.center)
                    .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.large))
                    .foregroundColor(DSColors.Text.indigoDark)
                    .frame(maxWidth: .infinity, alignment: .center)
                
                Spacer()
                
                HStack() {
                    BlueTextButton(title: AppConfig.UI.Titles.Button.no.localized) {
                        presentationMode.wrappedValue.dismiss()
                    }
                    
                    BlueBackgroundButton(title: AppConfig.UI.Titles.Button.yes.localized, action: {
                        isLoading = true
                        
                        viewModel.fetchDocuments { transactionId, error in
                            isLoading = false
                            
                            if let transactionId = transactionId {
                                etTransactionId = transactionId
                                openDocumentViewModel?.checkUserStatus()
                            } else if let error = error {
                                appState.alertItem = AlertProvider.errorAlert(message: error.description)
                            }
                        }
                    })
                }
                .frame(width: .infinity)
                .padding(.bottom, verticalPadding)
            }
            .padding([.leading, .trailing], horizontalPadding)
        }
        .onAppear {
            self.openDocumentViewModel = OpenDocumentViewModel(openDocumentUserDecision: { decision in
                openDocument = false
                
                switch decision {
                case .approved:
                    isLoading = true
                    openDocumentViewModel?.verifyDocument(transactionId: etTransactionId)
                default:
                    welcomeViewModel.state = .error(description: AppConfig.UI.Alert.shareDataDocDeclineAlertText.localized)
                    showWelcomeView = true
                }
                
            }, openDocumentErrorHandler: { error in
                appState.alertItem = AlertProvider.errorAlertWithCompletion(message: error.description, completion: {
                    if openDocument { openDocument = false }
                    if isLoading { isLoading = false }
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
                isLoading = false
                welcomeViewModel.state = .success
                showWelcomeView = true
            })
        }
        .alert(item: $appState.alertItem) { alertItem in
            AlertProvider.getAlertFor(alertItem: alertItem)
        }
        .background(DSColors.background)
        .navigationBarHidden(true)
    }
    
    private func navigation() -> some View {
        HStack {
            NavigationLink(destination: WelcomeUserView(viewModel: welcomeViewModel)
                .environmentObject(appState)
                .environmentObject(networkMonitor),
                           isActive: $showWelcomeView) { EmptyView() }
            
            NavigationLink(destination: openDocumentViewModel?.openDocument(transactionId: etTransactionId),
                           isActive: $openDocument) { EmptyView() }
            
            NavigationLink(destination: openDocumentViewModel?.openETSetup(),
                           isActive: $openETSetup) { EmptyView() }
            
            NavigationLink(destination: openDocumentViewModel?.openEditProfile(),
                           isActive: $openEditProfile) { EmptyView() }
        }
    }
}

@MainActor class DataShareViewModel: ObservableObject {
    func fetchDocuments(completion: @escaping (String?, NetworkError?) -> ()) {
        let parameters = DocumentsParameters(statuses: [.signing], cursor: nil)
        
        NetworkManager.getDocuments(parameters: parameters) { response in
            switch response {
            case .success(let documentsResponse):
                completion(documentsResponse.documents.first?.evrotrustTransactionId ?? "", nil)
            case .failure(let error):
                if let networkError = error as? NetworkError {
                    completion(nil, networkError)
                }
            }
        }
    }
}

#Preview {
    ConfirmDataShareView()
}
