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
    @State private var showChangePasswordAuthScreen = false
    @State private var isLoading = false
    
    var body: some View {
        LoadingStack(isPresented: $isLoading) {
            VStack(spacing: AppConfig.Dimensions.Padding.XXXL) {
                navigation()
                
                Spacer()
                
                header()
                
                Spacer()
                Spacer()
                
                NumpadView(reset: $resetPin, shouldShowBiometrics: false, didReachedCodeLength: { pin in
                    let fullPin = pin.map({ String($0) }).joined()
                    isLoading = true
                    
                    viewModel.verify(pin: fullPin) { error in
                        isLoading = false
                        
                        if let error = error {
                            resetPin = true
                            appState.alertItem = AlertProvider.errorAlert(message: error)
                        }
                    }
                })
                .padding([.leading, .trailing], RegisterFlowConstants.padding)
                
                Spacer()
                
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
            .padding([.leading, .trailing], RegisterFlowConstants.padding)
        }
        .onChange(of: viewModel.nextScreen) { nextScreen in
            switch nextScreen {
            case .none: break
            case .biometrics:
                showConfirmBiometrics = true
            case .forgottenPin:
                showChangePasswordAuthScreen = true
            }
        }
        .alert(item: $appState.alertItem) { alertItem in
            AlertProvider.getAlertFor(alertItem: alertItem)
        }
        .background(DSColors.background)
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
            
            NavigationLink(destination: ForgottenPasswordAuthenticationView()
                .environmentObject(appState)
                .environmentObject(networkMonitor),
                           isActive: $showChangePasswordAuthScreen) { EmptyView() }
        }
    }
}

#Preview {
    VerifyPINView()
}


@MainActor class VerifyPINViewModel: ObservableObject {
    @Published var nextScreen: VerifyPINNextScreen = .none
    
    func verify(pin: String, completion: @escaping (String?) -> ()) {
        NetworkManager.verifyPIN(pin: pin) { [weak self] response in
            switch response {
            case .success(let pinVerification):
                if pinVerification.matches {
                    var user = UserProvider.shared.getUser()
                    user?.pin = pin
                    UserProvider.shared.save(user: user)
                    
                    self?.nextScreen = .biometrics
                } else {
                    completion(AppConfig.UI.Alert.verifyPinErrorWrongCurrent.localized)
                }
            case .failure(_):
                completion(AppConfig.UI.Alert.welcomeUserErrorTitle.localized)
            }
        }
    }
}

enum VerifyPINNextScreen {
    case none, biometrics, forgottenPin
}
