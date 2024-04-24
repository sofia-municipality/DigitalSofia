//
//  PINView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 22.08.23.
//

import SwiftUI

struct PINView: View {
    @EnvironmentObject var appState: AppState
    @Binding var resetPin: Bool
    var shouldShowBiometrics = false
    var shouldVerifyPin = true
    var details: String = ""
    var didReachedCodeLength: ((String) -> ())?
    var didClickOnBiometrics: (() -> ())?
    
    private let padding = AppConfig.Dimensions.Padding.XXXL
    private let rules: [ValidationRule] = [SixDigitNumberRule(), NoConsecutiveDigitsRule(), NotEGNRule(), NoRepeatRule(), NotDOBRule(), OldPasswordRule()]
    
    var body: some View {
        VStack(spacing: padding) {
            if details.isEmpty == false {
                Text(details)
                    .font(DSFonts.getCustomFont(family: DSFonts.FontFamily.sofiaSans, 
                                                weight: DSFonts.FontWeight.regular,
                                                size: DSFonts.FontSize.medium))
                    .foregroundColor(DSColors.Text.indigoDark)
                    .multilineTextAlignment(.center)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(.bottom, AppConfig.Dimensions.Custom.numpadPadding)
            }
            
            NumpadView(reset: $resetPin, shouldShowBiometrics: shouldShowBiometrics, didReachedCodeLength: { pin in
                let stringPIN = pin.map({ String($0) }).joined()
                let pinIsValid = stringPIN.isValid(rules: rules)
                
                if shouldVerifyPin {
                    if pinIsValid {
                        didReachedCodeLength?(stringPIN)
                    } else {
                        resetPin = true
                        appState.alertItem = AlertProvider.errorAlert(message: AppConfig.UI.Evrotrust.createPinError.evrotrustLocalized)
                    }
                } else {
                    didReachedCodeLength?(stringPIN)
                }
            }, didClickOnBiometrics: {
                didClickOnBiometrics?()
            })
        }
        .padding([.leading, .trailing, .top], padding)
    }
}

struct PINView_Previews: PreviewProvider {
    static var previews: some View {
        PINView(resetPin: .constant(false))
    }
}
