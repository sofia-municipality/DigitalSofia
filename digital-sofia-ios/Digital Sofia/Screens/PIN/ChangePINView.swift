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
    @State var shouldGoToHome: Bool
    
    @State private var showHomeScreen = false
    @State private var newPin = ""
    @State private var viewModel = ChangePINViewModel()
    @State private var resetPin = false
    @State private var isLoading = false
    
    private var user = UserProvider.shared.getUser()
    
    init(state: PINViewState, shouldGoToHome: Bool) {
        self.state = state
        self.shouldGoToHome = shouldGoToHome
    }
    
    var body: some View {
        LoadingStack(isPresented: $isLoading) {
            VStack {
                navigation()
                
                if shouldGoToHome == false {
                    CustomNavigationBar()
                }
                
                Spacer()
                
                ProfileTiltleView(title: SettingsType.pin.description)
                
                PINView(resetPin: $resetPin, details: state.details) { pin in
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
                            
                            viewModel.change(pin: pin) { error in
                                isLoading = false
                                
                                if let etError = error {
                                    state = .old
                                    newPin = ""
                                    appState.alertItem = AlertProvider.errorAlert(message: etError.description)
                                } else {
                                    appState.alertItem = AlertProvider.successfullPINChange {
                                        if shouldGoToHome {
                                            showHomeScreen = true
                                        } else {
                                            appState.settingsViewId = UUID()
                                        }
                                    }
                                }
                            }
                        } else {
                            appState.alertItem = AlertProvider.mismatchPINAlert()
                        }
                    }
                }
                .background {
                    Color(.white)
                        .cornerRadius(AppConfig.Dimensions.CornerRadius.mini)
                        .clipped()
                        .shadow(color: .gray.opacity(0.3), radius: AppConfig.Dimensions.CornerRadius.mini)
                }
                .padding([.leading, .trailing, .bottom], AppConfig.Dimensions.Padding.XXXL)
            }
            .alert(item: $appState.alertItem) { alertItem in
                AlertProvider.getAlertFor(alertItem: alertItem)
            }
        }
        .background(DSColors.background)
        .navigationBarHidden(true)
    }
    
    private func navigation() -> some View {
        VStack {
            NavigationLink(destination: TabbarView(appState: appState).environmentObject(networkMonitor),
                           isActive: $showHomeScreen) { EmptyView() }
            
        }
    }
}

struct ChangePINView_Previews: PreviewProvider {
    static var previews: some View {
        ChangePINView(state: .old, shouldGoToHome: false)
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
