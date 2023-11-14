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
    @State private var isTouchIdEnabled: Bool = UserProvider.shared.getUser()?.useBiometrics ?? false
    @State private var isFaceIdEnabled: Bool = UserProvider.shared.getUser()?.useBiometrics ?? false
    
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
        .background(DSColors.background)
        .navigationBarHidden(true)
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
            }
        }
    }
    
    private func setUSerBiometricsStatus(active: Bool) {
        var user = UserProvider.shared.getUser()
        user?.useBiometrics = active
        UserProvider.shared.save(user: user)
    }
}

struct BiometricAuthenticationView_Previews: PreviewProvider {
    static var previews: some View {
        BiometricAuthenticationView()
    }
}
