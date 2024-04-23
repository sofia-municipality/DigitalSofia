//
//  LoginViewModel.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 14.08.23.
//

import SwiftUI

class LoginViewModel {
    var authenticationResult: ((BiometricError?) -> ())?
    var biometricFallbackAction: (() -> ())?
    
    fileprivate var user: User? {
        return UserProvider.currentUser
    }
    
    init(authenticationResult: ((BiometricError?) -> ())? = nil,
         biometricFallbackAction: (() -> ())? = nil) {
        self.authenticationResult = authenticationResult
        self.biometricFallbackAction = biometricFallbackAction
    }
    
    
    
    func welcomeView() -> some View {
        return VStack {
            HStack {
                Spacer()
                Image(ImageProvider.person)
                Spacer()
            }
            
            let userName = LanguageProvider.shared.appLanguage == .bulgarian ? user?.firstName : user?.firstLatinName
            Text(AppConfig.UI.Text.loginGreetingText.localized.format(userName?.capitalized ?? ""))
                .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.firaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.XXXL))
                .foregroundColor(DSColors.Text.indigoDark)
                .frame(maxWidth: .infinity, alignment: .center)
                .padding(.top, AppConfig.Dimensions.Padding.large)
        }
    }
    
    func authenticateWithBiometrics() {
        BiometricProvider.authenticate { [weak self] success, error in
            if let error = error {
                self?.authenticationResult?(error)
            } else {
                PINBlockProvider.shared.resetBlock()
                self?.authenticationResult?(nil)
            }
        }
    }
}
