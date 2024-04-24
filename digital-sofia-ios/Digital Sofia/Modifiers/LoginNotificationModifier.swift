//
//  LoginNotificationModifier.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 5.01.24.
//

import SwiftUI

struct LoginNotificationModifier: ViewModifier {
    @EnvironmentObject var appState: AppState
    @EnvironmentObject var networkMonitor: NetworkMonitor
    
    @State private var showLoginRequestScreen = false
    @StateObject private var viewModel = LoginCodeViewModel()
    
    func body(content: Content) -> some View {
        content
            .onAppear {
                if let code = appState.loginRequestCode {
                    viewModel.loginCode = code
                    showLoginRequestScreen = true
                }
            }
            .onChange(of: appState.loginRequestCode) { newValue in
                if newValue != nil {
                    viewModel.loginCode = newValue
                    showLoginRequestScreen = true
                    appState.loginRequestCode = nil
                }
            }
            .fullScreenCover(isPresented: $showLoginRequestScreen, content: {
                LoginFromPortalNotificationView(loginRequestResponse: { response in
                    viewModel.updateAuthenticationCodeStatus(response: response) { status, error in
                        if error != nil || status == false {
                            hideViewOnError()
                        }
                    }
                })
                .environmentObject(appState)
            })
            .lockScreen()
            .environmentObject(appState)
            .environmentObject(networkMonitor)
    }
    
    private func fetchLogin() {
        viewModel.getAuthenticationCode { response, error in
            if error != nil {
                hideViewOnError()
            } else if response?.codeExists == true {
                showLoginRequestScreen = true
                appState.loginRequestCode = nil
            }
        }
    }
    
    private func hideViewOnError() {
        appState.alertItem = AlertProvider.generalAlertWithCompletion {
            showLoginRequestScreen = false
        }
    }
}

extension View {
    func loginNotification() -> some View {
        self.modifier(LoginNotificationModifier())
    }
}
