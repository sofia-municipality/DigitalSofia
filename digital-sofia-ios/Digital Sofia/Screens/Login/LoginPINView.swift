//
//  LoginPINView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 14.08.23.
//

import SwiftUI

struct LoginPINView: View {
    @EnvironmentObject var appState: AppState
    @EnvironmentObject var networkMonitor: NetworkMonitor
    @Environment(\.dismiss) var dismiss
    
    @State private var resetPin = false
    @State private var showHomeScreen = false
    @State private var showForgottenPIN = false
    @State private var pinAttempts = 3
    @State private var loginViewModel: LoginViewModel?
    
    var successfullyEnteredPIN: (() -> ())?
    var onLockScreenCompletion: (() -> ())?
    fileprivate var user: User? {
        return UserProvider.currentUser
    }
    
    var body: some View {
        VStack(spacing: AppConfig.Dimensions.Padding.XXL) {
            navigation()
            
            Spacer()
            
            loginViewModel?.welcomeView()
            
            Spacer()
            Spacer()
            Spacer()
            
            PINView(resetPin: $resetPin,
                    shouldShowBiometrics: UserProvider.biometricsAvailable,
                    shouldVerifyPin: false,
                    didReachedCodeLength: { pin in
                handleResponseFor(pin: pin)
            }, didClickOnBiometrics: {
                loginViewModel?.authenticateWithBiometrics()
            })
            .environmentObject(appState)
            .padding([.leading, .trailing], AppConfig.Dimensions.Padding.XL)
            
            Spacer()
            
            Button {
                showForgottenPIN = true
            } label: {
                Text(AppConfig.UI.Titles.Button.forgottenPin.localized)
                    .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.firaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.medium))
                    .foregroundColor(DSColors.Blue.regular)
                    .frame(maxWidth: .infinity, alignment: .center)
            }
            
            Spacer()
        }
        .onAppear {
            self.loginViewModel = LoginViewModel(authenticationResult: { error in
                if let error = error {
                    appState.alertItem = AlertProvider.errorAlert(message: error.description)
                } else {
                    navigateOnSuccessfullPIN()
                }
            })
        }
        .log(view: self)
        .alert()
        .environmentObject(appState)
        .environmentObject(networkMonitor)
        .backgroundAndNavigation()
    }
    
    private func handleResponseFor(pin: String) {
        resetPin = true
        
        if PINBlockProvider.shared.isBlocked {
            showBlockAlert()
        } else {
            if pinAttempts == 0 {
                pinAttempts = 3
            }
            check(pin: pin)
        }
    }
    
    private func check(pin: String) {
        if pin == user?.pin {
            navigateOnSuccessfullPIN()
        } else {
            pinAttempts -= 1
            
            if pinAttempts > 0 {
                appState.alertItem = AlertProvider.wrongPINAlert(attempts: pinAttempts)
            } else {
                PINBlockProvider.shared.setBlockTime()
                showBlockAlert()
            }
        }
    }
    
    private func navigateOnSuccessfullPIN() {
        PINBlockProvider.shared.resetBlock()
        
        if successfullyEnteredPIN == nil {
            if appState.shouldLockScreen {
                dismiss()
                onLockScreenCompletion?()
            } else {
                showHomeScreen = true
            }
        } else {
            successfullyEnteredPIN?()
        }
    }
    
    private func showBlockAlert() {
        appState.alertItem = AlertProvider.pinBlockAlert(message: PINBlockProvider.shared.blockAlertMessage)
    }
    
    private func navigation() -> some View {
        VStack {
            NavigationLink(destination: TabbarView()
                .environmentObject(appState)
                .environmentObject(networkMonitor),
                           isActive: $showHomeScreen) { EmptyView() }
            
            NavigationLink(destination: ForgottenPIN(isResetPin: false)
                .environmentObject(appState)
                .environmentObject(networkMonitor)
                .environmentObject(IdentityRequestConfig()),
                           isActive: $showForgottenPIN) { EmptyView() }
        }
    }
}

struct LoginPINView_Previews: PreviewProvider {
    static var previews: some View {
        LoginPINView()
    }
}
