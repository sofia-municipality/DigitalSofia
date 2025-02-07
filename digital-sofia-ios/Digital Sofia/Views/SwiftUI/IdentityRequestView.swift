//
//  IdentityRequestView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 12.01.24.
//

import SwiftUI
import EvrotrustSDK

class IdentityRequestConfig: ObservableObject {
    @Published var fetchRequest: Bool = false
    var newPin: String = ""
}

enum IdentityRequestViewType {
    case authenticate, resetPIN, forgotPIN
}

struct IdentityRequestView<Content: View>: View {
    @EnvironmentObject var networkMonitor: NetworkMonitor
    @EnvironmentObject var appState: AppState
    @EnvironmentObject var identityConfig: IdentityRequestConfig
    @Environment(\.presentationMode) var presentationMode
    
    @State private var etTransactionId = ""
    @State private var isLoading = false
    
    @State private var openDocument = false
    @State private var openETSetup = false
    @State private var openEditProfile = false
    
    @State private var showWelcomeView = false
    @State private var showDataHomeScreen = false
    
    @State private var openDocumentViewModel: OpenDocumentViewModel?
    @State private var dataShareViewModel = DataShareViewModel()
    @State private var welcomeViewModel = WelcomeUserViewModel()
    @State private var viewModel = IdentityRequestViewViewModel()
    
    @ViewBuilder let content: Content
    var type: IdentityRequestViewType
    
    @State private var oldSecurityContext = ""
    
    var body: some View {
        LoadingStack(isPresented: $isLoading) {
            navigation()
            content
        }
        .onAppear {
            openDocumentViewModel = OpenDocumentViewModel(openDocumentUserDecision: { decision in
                closeDocumentDetails()
                switch decision {
                case .approved:
                    isLoading = true
                    if type != .authenticate {
                        oldSecurityContext = UserProvider.currentUser?.securityContext ?? ""
                        UserProvider.shared.update(pin: identityConfig.newPin)
                    }
                    openDocumentViewModel?.verifyIdentityRequest(transactionId: etTransactionId, type: type)
                default:
                    DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
                        if type == .authenticate {
                            showWelcomeView(success: false)
                        } else {
                            UserProvider.shared.logout()
                        }
                    }
                }
                
            }, openDocumentErrorHandler: { error in
                if type == .authenticate {
                    appState.alertItem = AlertProvider.errorAlertWithCompletion(message: error.baseDescription, completion: {
                        if openDocument { closeDocumentDetails() }
                        reinitialise()
                    })
                } else {
                    UserProvider.shared.logout()
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
            }, receivedDocumentStatus: { _ in
            }, verifyIdentityRequest: { _ in
                switch type {
                case .authenticate:
                    reinitialise()
                    showWelcomeView(success: true)
                case .forgotPIN:
                    viewModel.completion = { error in
                        if error != nil {
                            UserProvider.shared.logout()
                        } else {
                            appState.alertItem = AlertProvider.successfullPINChange {
                                reinitialise()
                                showDataHomeScreen = true
                            }
                        }
                    }
                    viewModel.changeSecurityContext(old: oldSecurityContext, to: UserProvider.currentUser?.securityContext ?? "")
                case .resetPIN:
                    appState.alertItem = AlertProvider.successfullPINChange {
                        reinitialise()
                        showDataHomeScreen = true
                    }
                }
            }, userClosedDocumentView: {
                if openEditProfile { openEditProfile = false }
                if openDocument { closeDocumentDetails() }
                reinitialise()
            })
        }
        .onChange(of: identityConfig.fetchRequest) { shouldFetch in
            if shouldFetch {
                getIdentityRequest()
            }
        }
        .alert()
        .backgroundAndNavigation()
        .environmentObject(appState)
        .environmentObject(networkMonitor)
    }
    
    func getIdentityRequest() {
        isLoading = true
        
        dataShareViewModel.getIdentityRequest() { transactionId, error in
            isLoading = false
            if let transactionId = transactionId {
                etTransactionId = transactionId
                openDocumentViewModel?.checkUserStatus()
            } else if let error = error {
                if type == .authenticate {
                    appState.alertItem = AlertProvider.errorAlert(message: error.description)
                } else {
                    UserProvider.shared.logout()
                }
            }
        }
    }
    
    private func showWelcomeView(success: Bool) {
        if success {
            welcomeViewModel.state = .success
            showWelcomeView = true
        } else {
            welcomeViewModel.state = .error(description: AppConfig.UI.Alert.shareDataDocDeclineAlertText.localized)
            showWelcomeView = true
        }
    }
    
    private func navigation() -> some View {
        HStack {
            NavigationLink(destination: WelcomeUserView(viewModel: welcomeViewModel)
                .environmentObject(appState)
                .environmentObject(networkMonitor),
                           isActive: $showWelcomeView) { EmptyView() }
            
            NavigationLink(destination: TabbarView()
                .environmentObject(appState)
                .environmentObject(networkMonitor),
                           isActive: $showDataHomeScreen) { EmptyView() }
            
            NavigationLink(destination: openDocumentViewModel?.openDocument(transactionId: etTransactionId),
                           isActive: $openDocument) { EmptyView() }
            
            NavigationLink(destination: openDocumentViewModel?.openETSetup(),
                           isActive: $openETSetup) { EmptyView() }
            
            NavigationLink(destination: openDocumentViewModel?.openEditProfile(),
                           isActive: $openEditProfile) { EmptyView() }
        }
    }
    
    private func reinitialise() {
        if isLoading { isLoading = false }
        if identityConfig.fetchRequest { identityConfig.fetchRequest = false }
    }
    
    private func closeDocumentDetails() {
        openDocument = false
        if String(describing: UIApplication.topMostViewController()).contains("EvrotrustDocumentDetailsViewController") {
            UIApplication.topMostViewController()?.navigationController?.popToRootViewController(animated: true)
        }
    }
}
