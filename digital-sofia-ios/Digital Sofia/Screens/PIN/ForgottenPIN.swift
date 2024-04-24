//
//  ForgottenPIN.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 12.01.24.
//

import SwiftUI

struct ForgottenPIN: View {
    @EnvironmentObject var networkMonitor: NetworkMonitor
    @EnvironmentObject var appState: AppState
    @EnvironmentObject var identityConfig: IdentityRequestConfig
    
    var isResetPin = false
    
    @State private var state: PINViewState = .new
    @State private var resetPin = false
    @State private var newPin = ""
    
    @State private var showETSetup = false
    
    var body: some View {
        IdentityRequestView(content: {
            VStack {
                navigation()
                
                CustomNavigationBar()
                
                ProfileTiltleView(title: SettingsType.pin.description)
                    .padding(.bottom, AppConfig.Dimensions.Custom.numpadPadding / 2)
                
                VStack(spacing: 0) {
                    PINView(resetPin: $resetPin, shouldVerifyPin: state != .old, details: state.details) { pin in
                        resetPin = true
                        
                        switch state {
                        case .old: break
                        case .new:
                            newPin = pin
                            state = .confirm
                        case .confirm:
                            if newPin == pin {
                                identityConfig.newPin = pin
                                showETSetup = true
                            } else {
                                appState.alertItem = AlertProvider.mismatchPINAlert()
                            }
                        }
                    }
                    .environmentObject(appState)
                    .padding(.bottom, AppConfig.Dimensions.Custom.numpadPadding)
                    .padding(.top, AppConfig.Dimensions.Custom.numpadPadding / 2)
                }
                .background {
                    Color(.white)
                        .cornerRadius(AppConfig.Dimensions.CornerRadius.mini)
                        .clipped()
                        .shadow(color: .gray.opacity(0.3), radius: AppConfig.Dimensions.CornerRadius.mini)
                }
                .padding([.leading, .trailing, .bottom], AppConfig.Dimensions.Padding.XXXL)
                
                Spacer()
            }
        }, type: isResetPin ? .resetPIN : .forgotPIN)
        .log(view: self)
        .alert()
        .backgroundAndNavigation()
        .environmentObject(appState)
        .environmentObject(networkMonitor)
    }
    
    private func navigation() -> some View {
        VStack {
            NavigationLink(destination: EvrotrustFullSetupView(shouldAddUserInformation: isResetPin, completion: { success, error in
                if let _ = error {
                    UserProvider.shared.logout()
                } else {
                    showETSetup = false
                    
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                        identityConfig.fetchRequest = true
                    }
                }
            })
                .ignoresSafeArea()
                .environmentObject(identityConfig),
                           isActive: $showETSetup) { EmptyView() }
        }
    }
}

#Preview {
    ForgottenPIN()
}
