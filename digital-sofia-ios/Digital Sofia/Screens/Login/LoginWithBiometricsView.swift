//
//  LoginWithBiometrics.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 14.08.23.
//

import SwiftUI

struct LoginWithBiometricsView: View {
    @EnvironmentObject var appState: AppState
    @EnvironmentObject var networkMonitor: NetworkMonitor
    @Environment(\.dismiss) var dismiss
    
    var onLockScreenCompletion: (() -> ())?
    @State private var showHomeScreen = false
    @State private var showPIN = false
    @State private var loginViewModel: LoginViewModel?
    fileprivate var user: User? {
        return UserProvider.currentUser
    }
    
    var body: some View {
        VStack(spacing: AppConfig.Dimensions.Padding.XXL) {
            navigation()
            
            loginViewModel?.welcomeView()
            
            Spacer()
            Spacer()
            
            Text(AppConfig.UI.Text.loginTitleText.localized)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.firaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.medium))
                .foregroundColor(DSColors.Text.indigoDark)
                .frame(maxWidth: .infinity, alignment: .center)
                .padding(.bottom, AppConfig.Dimensions.Padding.large)
            
            Button {
                loginViewModel?.authenticateWithBiometrics()
            } label: {
                HStack {
                    Spacer()
                    Image(systemName: BiometricProvider.biometricType == .face ? ImageProvider.SystemImages.faceID : ImageProvider.SystemImages.touchID)
                        .resizable()
                        .scaledToFit()
                        .foregroundColor(DSColors.Blue.regular)
                        .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.firaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.medium))
                        .frame(width: AppConfig.Dimensions.Standart.iconHeight * 1.5, height: AppConfig.Dimensions.Standart.iconHeight * 1.5)
                    Spacer()
                }
            }
            .padding(.bottom, AppConfig.Dimensions.Padding.large)
            
            Button {
                showPIN = true
            } label: {
                Text(AppConfig.UI.Titles.Button.pinCode.localized)
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
                    if appState.shouldLockScreen {
                        dismiss()
                        onLockScreenCompletion?()
                    } else {
                        showHomeScreen = true
                    }
                }
            }, biometricFallbackAction: {
                showPIN = true
            })
        }
        .log(view: self)
        .alert()
        .environmentObject(appState)
        .environmentObject(networkMonitor)
        .backgroundAndNavigation()
    }
    
    private func navigation() -> some View {
        VStack {
            NavigationLink(destination: TabbarView()
                .environmentObject(appState)
                .environmentObject(networkMonitor),
                           isActive: $showHomeScreen) { EmptyView() }
            
            NavigationLink(destination: LoginPINView(successfullyEnteredPIN: {
                showPIN = false
                showHomeScreen = true
            })
                .environmentObject(appState)
                .environmentObject(networkMonitor),
                           isActive: $showPIN) { EmptyView() }
        }
    }
}

struct LoginWithBiometrics_Previews: PreviewProvider {
    static var previews: some View {
        LoginWithBiometricsView()
    }
}
