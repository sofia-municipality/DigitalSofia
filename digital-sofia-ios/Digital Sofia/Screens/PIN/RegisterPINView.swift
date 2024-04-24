//
//  RegisterPINView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 22.08.23.
//

import SwiftUI

struct RegisterPINView: View {
    @EnvironmentObject var networkMonitor: NetworkMonitor
    @EnvironmentObject var appState: AppState
    
    @State var state: PINViewState = .new
    @State private var newPin = ""
    @State private var resetPin = false
    @State private var showConfirmBiometrics = false
    @State private var showConfirmAuth = false
    
    var body: some View {
        VStack(spacing: RegisterFlowConstants.padding) {
            navigation()
            
            Text(state.title)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, 
                                            weight: DSFonts.FontWeight.regular,
                                            size: DSFonts.FontSize.XXXL))
                .foregroundColor(DSColors.Text.indigoDark)
                .frame(maxWidth: .infinity, alignment: .center)
                .padding(.bottom, RegisterFlowConstants.padding)
            
            Spacer()
            
            PINView(resetPin: $resetPin, 
                    shouldVerifyPin: state == .new,
                    details: state.details) { pin in
                resetPin = true
                
                switch state {
                case .old: break
                case .new:
                    newPin = pin
                    state = .confirm
                case .confirm:
                    if newPin == pin {
                        UserProvider.shared.update(pin: pin)
                        if BiometricProvider.biometricsAvailable {
                            showConfirmBiometrics = true
                        } else {
                            showConfirmAuth = true
                        }
                    } else {
                        appState.alertItem = PrimaryAndSecondaryAlertItem(title: Text(AppConfig.UI.Alert.pinMismatchAlertTitle.localized),
                                                                          message: Text(AppConfig.UI.Alert.pinMismatchAlertDetails.localized),
                                                                          primaryButton: .cancel(Text(AppConfig.UI.Alert.verifyPinTryAgainAlertText.localized)),
                                                                          secondaryButton: .default(Text(AppConfig.UI.Alert.verifyPinCreateAgainAlertText.localized)) {
                            state = .new
                        })
                    }
                }
            }
            .environmentObject(appState)
            
            Spacer()
            Spacer()
        }
        .padding([.leading, .trailing, .top], RegisterFlowConstants.padding)
        .log(view: self)
        .alert()
        .environmentObject(appState)
        .environmentObject(networkMonitor)
    }
    
    private func navigation() -> some View {
        HStack {
            NavigationLink(destination: RegisterBiometricsAuthView()
                .environmentObject(appState)
                .environmentObject(networkMonitor),
                           isActive: $showConfirmBiometrics) { EmptyView() }
            
            NavigationLink(destination: ConfirmAuthenticationView()
                .environmentObject(appState)
                .environmentObject(networkMonitor),
                           isActive: $showConfirmAuth) { EmptyView() }
        }
    }
}

struct RegisterPINView_Previews: PreviewProvider {
    static var previews: some View {
        RegisterPINView()
    }
}
