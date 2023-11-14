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
    
    @State private var resetPin = false
    @State private var showHomeScreen = false
    @State private var showChangePasswordAuthScreen = false
    @State private var pinAttempts = 3
    @State private var loginViewModel: LoginViewModel?
    
    var successfullyEnteredPIN: (() -> ())?
    let user = UserProvider.shared.getUser()
    
    var body: some View {
        VStack(spacing: AppConfig.Dimensions.Padding.XXL) {
            navigation()
            
            Spacer()
            
            loginViewModel?.welcomeView()
            
            Spacer()
            Spacer()
            Spacer()
            
            NumpadView(reset: $resetPin, shouldShowBiometrics: user?.useBiometrics == true, didReachedCodeLength: { pin in
                let pin = pin.map({ String($0) }).joined()
                handleResponseFor(pin: pin)
            }, didClickOnBiometrics: {
                loginViewModel?.authenticateWithBiometrics()
            })
            
            Button {
                showChangePasswordAuthScreen = true
            } label: {
                Text(AppConfig.UI.Titles.Button.forgottenPin.localized)
                    .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.firaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.medium))
                    .foregroundColor(DSColors.Blue.blue)
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
        .alert(item: $appState.alertItem) { alertItem in
            AlertProvider.getAlertFor(alertItem: alertItem)
        }
        .background(DSColors.background)
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
        if successfullyEnteredPIN == nil {
            showHomeScreen = true
        } else {
            successfullyEnteredPIN?()
        }
    }
    
    private func showBlockAlert() {
        appState.alertItem = AlertProvider.pinBlockAlert(seconds: PINBlockProvider.shared.blockLength)
    }
    
    private func navigation() -> some View {
        VStack {
            NavigationLink(destination: TabbarView(appState: appState).environmentObject(networkMonitor),
                           isActive: $showHomeScreen) { EmptyView() }
            
            NavigationLink(destination: ForgottenPasswordAuthenticationView()
                .environmentObject(appState)
                .environmentObject(networkMonitor),
                           isActive: $showChangePasswordAuthScreen) { EmptyView() }
        }
    }
}

struct LoginPINView_Previews: PreviewProvider {
    static var previews: some View {
        LoginPINView()
    }
}
