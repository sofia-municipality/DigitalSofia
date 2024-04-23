//
//  ChangePINView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 22.08.23.
//

import SwiftUI

struct ChangePINView: View {
    @EnvironmentObject var networkMonitor: NetworkMonitor
    @EnvironmentObject var appState: AppState
    
    @State var state: PINViewState
    
    @State private var newPin = ""
    @State private var viewModel = ChangePINViewModel()
    @State private var resetPin = false
    @State private var isLoading = false
    private var user = UserProvider.currentUser
    
    init(state: PINViewState) {
        self.state = state
    }
    
    var body: some View {
        LoadingStack(isPresented: $isLoading) {
            VStack {
                CustomNavigationBar()
                
                ProfileTiltleView(title: SettingsType.pin.description)
                    .padding(.bottom, AppConfig.Dimensions.Custom.numpadPadding / 2)
                
                VStack(spacing: 0) {
                    PINView(resetPin: $resetPin, 
                            shouldVerifyPin: state != .old,
                            details: state.details) { pin in
                        resetPin = true
                        
                        switch state {
                        case .old:
                            if user?.pin == pin {
                                state = .new
                            } else {
                                appState.alertItem = AlertProvider.errorAlert(message: AppConfig.UI.Alert.wrongPINAlertTitle.localized)
                            }
                        case .new:
                            newPin = pin
                            state = .confirm
                        case .confirm:
                            if newPin == pin {
                                isLoading = true
                                changePIN(pin: pin)
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
        }
        .log(view: self)
        .alert()
        .lockScreen()
        .loginNotification()
        .environmentObject(appState)
        .environmentObject(networkMonitor)
        .backgroundAndNavigation()
    }
    
    private func changePIN(pin: String) {
        viewModel.change(pin: pin) { error in
            isLoading = false
            
            if error != nil {
                state = .old
                newPin = ""
                appState.alertItem = AlertProvider.errorAlert(message: error?.description ?? "")
            } else {
                appState.alertItem = AlertProvider.successfullPINChange {
                    DispatchQueue.main.async {
                        appState.settingsViewId = UUID()
                    }
                }
            }
        }
    }
}

struct ChangePINView_Previews: PreviewProvider {
    static var previews: some View {
        ChangePINView(state: .old)
    }
}

enum PINViewState {
    case old, new, confirm
    
    var title: String {
        switch self {
        case .new:
            return AppConfig.UI.Text.createPinTitle.localized
        case .confirm:
            return AppConfig.UI.Text.confirmPinTitle.localized
        default: return ""
        }
    }
    
    var details: String {
        switch self {
        case .old:
            return AppConfig.UI.Text.changePINOldConfirmText.localized
        case .new:
            return AppConfig.UI.Text.createPinDetails.localized
        case .confirm:
            return AppConfig.UI.Text.confirmPinDetails.localized
        }
    }
}
