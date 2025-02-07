//
//  ForgottenPIN.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 12.01.24.
//

import SwiftUI

struct ForgottenPIN: View {
    // MARK: Properties
    @EnvironmentObject var networkMonitor: NetworkMonitor
    @EnvironmentObject var appState: AppState
    @EnvironmentObject var identityConfig: IdentityRequestConfig
    var isResetPin = false
    var readyToSign = false
    @State private var state: PINViewState = .new
    @State private var resetPin = false
    @State private var newPin = ""
    @State private var showETAuthenticationFailedView = false
    @State private var showETSetup = false
    
    // MARK: Body
    var body: some View {
        IdentityRequestView(content: {
            VStack {
                navigation()
                
                if UserProvider.shouldContinueResetPasswordFlow == false {
                    CustomNavigationBar()
                } else {
                    Spacer()
                }
                
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
        .onAppear {
            if UserProvider.shouldContinueResetPasswordFlow {
                identityConfig.newPin = KeychainWrapper.standard.string(forKey: AppConfig.KeychainKeys.newUserPin) ?? ""
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
                    if readyToSign {
                        identityConfig.fetchRequest = true
                    } else {
                        if identityConfig.fetchRequest == false {
                            showETAuthenticationFailedView = true
                        }
                    }
                }
            }
        }
    }
    
    private func navigation() -> some View {
        VStack {
            NavigationLink(destination: EvrotrustFullSetupView(shouldAddUserInformation: isResetPin,
                                                               completion: { success, error in
                showETSetup = false
                DispatchQueue.main.asyncAfter(deadline: .now() + 4) {
                    if let error = error {
                        if error == .userNotReadyToSign {
                            UserDefaults.standard.set(true, forKey: AppConfig.UserDefaultsKeys.userInitiatedForgottenPasswordFlow)
                            KeychainDatastore.standard.save(data: newPin, key: AppConfig.KeychainKeys.newUserPin)
                            showETAuthenticationFailedView = true
                            
                        } else {
                            UserProvider.shared.logout()
                        }
                    } else {
                        identityConfig.fetchRequest = true
                        
                    }
                }
            })
                .ignoresSafeArea()
                .environmentObject(appState)
                .environmentObject(identityConfig),
                           isActive: $showETSetup) { EmptyView() }
            
            NavigationLink(destination: ETSdkAuthenticationFailedView(onReadyToSign: {
                showETAuthenticationFailedView = false
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                    identityConfig.fetchRequest = true
                }
            })
                .environmentObject(appState),
                           isActive: $showETAuthenticationFailedView) { EmptyView() }
        }
    }
}

#Preview {
    ForgottenPIN()
}
