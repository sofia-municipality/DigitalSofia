//
//  BiometricAuthenticationView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 6.06.23.
//

import SwiftUI

enum DSSecurityOptions: CaseIterable {
    case faceId, touchId
    
    var description: String {
        switch self {
        case .faceId:
            return AppConfig.UI.Profile.Security.faceID.localized
        case .touchId:
            return AppConfig.UI.Profile.Security.touchID.localized
        }
    }
}

struct BiometricAuthenticationView: View {
    @EnvironmentObject var appState: AppState
    @EnvironmentObject var networkMonitor: NetworkMonitor
    
    @State private var isTouchIdEnabled: Bool = UserProvider.biometricsAvailable
    @State private var isFaceIdEnabled: Bool = UserProvider.biometricsAvailable
    
    var body: some View {
        VStack() {
            CustomNavigationBar()
            
            ProfileTiltleView(title: SettingsType.security.description)
                .padding(.bottom, AppConfig.Dimensions.Padding.XXXXL)
            
            VStack {
                if BiometricProvider.biometricType == .touch {
                    CustomToggleView(title: DSSecurityOptions.touchId.description, fontSize: DSFonts.FontSize.XL, toggle: $isTouchIdEnabled)
                        .onChange(of: isTouchIdEnabled, perform: { value in
                            if value {
                                setUpBiometrics()
                            } else {
                                setUSerBiometricsStatus(active: false)
                            }
                        })
                }
                
                if BiometricProvider.biometricType == .face {
                    let _ = print(isFaceIdEnabled)
                    CustomToggleView(title: DSSecurityOptions.faceId.description, fontSize: DSFonts.FontSize.XL, toggle: $isFaceIdEnabled)
                        .onChange(of: isFaceIdEnabled, perform: { value in
                            if value {
                                setUpBiometrics()
                            } else {
                                setUSerBiometricsStatus(active: false)
                            }
                        })
                }
            }
            .padding([.leading, .trailing], AppConfig.Dimensions.Padding.XXXXL)
            
            Spacer()
        }
        .alert()
        .lockScreen()
        .loginNotification()
        .environmentObject(appState)
        .environmentObject(networkMonitor)
        .backgroundAndNavigation()
    }
    
    private func setUpBiometrics() {
        BiometricProvider.authenticate { success, error in
            if success {
                setUSerBiometricsStatus(active: true)
                
                if BiometricProvider.biometricType == .face {
                    isFaceIdEnabled = true
                } else {
                    isTouchIdEnabled = true
                }
            } else {
                if BiometricProvider.biometricType == .face {
                    isFaceIdEnabled = false
                } else {
                    isTouchIdEnabled = false
                }
                
                appState.alertItem = AlertProvider.errorAlert(message: error?.description ?? "")
            }
        }
    }
    
    private func setUSerBiometricsStatus(active: Bool) {
        var user = UserProvider.currentUser
        user?.useBiometrics = active
        UserProvider.shared.save(user: user)
    }
}

struct BiometricAuthenticationView_Previews: PreviewProvider {
    static var previews: some View {
        BiometricAuthenticationView()
    }
}
