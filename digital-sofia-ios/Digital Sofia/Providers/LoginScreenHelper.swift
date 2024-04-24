//
//  LoginScreenHelper.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 8.01.24.
//

import SwiftUI

final class LoginScreenHelper {
    static var onLockScreenCompletion: (() -> ())?
    
    @ViewBuilder
    static var loginScreen: some View {
        if UserProvider.biometricsAvailable {
            LoginWithBiometricsView(onLockScreenCompletion: {
                onLockScreenCompletion?()
            })
        } else {
            LoginPINView(onLockScreenCompletion: {
                onLockScreenCompletion?()
            })
        }
    }
}
