//
//  VerifyPINView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 27.09.23.
//

import SwiftUI

struct VerifyPINView: View {
    @EnvironmentObject var appState: AppState
    @EnvironmentObject var networkMonitor: NetworkMonitor
    
    @StateObject private var viewModel = VerifyPINViewModel()
    @State private var resetPin = false
    @State private var showConfirmBiometrics = false
    @State private var showForgottenPIN = false
    @State private var showConfirmAuth = false
    @State private var isLoading = false
    
    var body: some View {
        LoadingStack(isPresented: $isLoading) {
            VStack(spacing: AppConfig.Dimensions.Padding.XXXL) {
                navigation()
                
                Spacer()
                
                header()
                
                Spacer()
                Spacer()
                
                PINView(resetPin: $resetPin, shouldVerifyPin: false, didReachedCodeLength: { pin in
                    isLoading = true
                    
                    viewModel.verify(pin: pin) { error in
                        isLoading = false
                        
                        if let error = error {
                            resetPin = true
                            appState.alertItem = AlertProvider.errorAlert(message: error)
                        }
                    }
                })
                .environmentObject(appState)
                
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
            .padding([.leading, .trailing], RegisterFlowConstants.padding)
        }
        .onChange(of: viewModel.nextScreen) { nextScreen in
            switch nextScreen {
            case .none: break
            case .biometrics:
                showConfirmBiometrics = true
            case .forgottenPin:
                showForgottenPIN = true
            case .authentication:
                showConfirmAuth = true
            }
        }
        .log(view: self)
        .alert()
        .environmentObject(appState)
        .environmentObject(networkMonitor)
        .backgroundAndNavigation()
    }
    
    private func header() -> some View {
        VStack {
            HStack {
                Spacer()
                Image(ImageProvider.person)
                Spacer()
            }
            
            Text(AppConfig.UI.Alert.verifyPinOnLoginTitleText.localized)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.firaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.XXXL))
                .foregroundColor(DSColors.Text.indigoDark)
                .frame(maxWidth: .infinity, alignment: .center)
                .padding(.top, AppConfig.Dimensions.Padding.large)
        }
    }
    
    private func navigation() -> some View {
        VStack {
            NavigationLink(destination: RegisterBiometricsAuthView()
                .environmentObject(appState)
                .environmentObject(networkMonitor),
                           isActive: $showConfirmBiometrics) { EmptyView() }
            
            NavigationLink(destination: ForgottenPIN(isResetPin: true)
                .environmentObject(appState)
                .environmentObject(networkMonitor)
                .environmentObject(IdentityRequestConfig()),
                           isActive: $showForgottenPIN) { EmptyView() }
            
            NavigationLink(destination: ConfirmAuthenticationView()
                .environmentObject(appState)
                .environmentObject(networkMonitor),
                           isActive: $showConfirmAuth) { EmptyView() }
        }
    }
}

#Preview {
    VerifyPINView()
}

enum VerifyPINNextScreen {
    case none, biometrics, forgottenPin, authentication
}
