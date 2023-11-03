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
    
    var body: some View {
        VStack(spacing: RegisterFlowConstants.padding) {
            navigation()
            
            Text(state.title)
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.XXXL))
                .foregroundColor(DSColors.Text.indigoDark)
                .frame(maxWidth: .infinity, alignment: .center)
                .padding(.bottom, RegisterFlowConstants.padding)
            
            PINView(resetPin: $resetPin, details: state.details) { pin in
                resetPin = true
                
                switch state {
                case .old: break
                case .new:
                    newPin = pin
                    state = .confirm
                case .confirm:
                    if newPin == pin {
                        showConfirmBiometrics = true
                        
                        var user = UserProvider.shared.getUser()
                        user?.pin = pin
                        UserProvider.shared.save(user: user)
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
        }
        .padding([.leading, .trailing, .top], RegisterFlowConstants.padding)
        .alert(item: $appState.alertItem) { alertItem in
            AlertProvider.getAlertFor(alertItem: alertItem)
        }
    }
    
    private func navigation() -> some View {
        HStack {
            NavigationLink(destination: RegisterBiometricsAuthView()
                .environmentObject(appState)
                .environmentObject(networkMonitor),
                           isActive: $showConfirmBiometrics) { EmptyView() }
        }
    }
}

struct RegisterPINView_Previews: PreviewProvider {
    static var previews: some View {
        RegisterPINView()
    }
}
