//
//  BiometricError.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 7.06.23.
//

import Foundation

enum BiometricError: DSError, Equatable {
    var description: String {
        switch self {
        case .noBiometrics:
            return  AppConfig.ErrorLocalisations.Biometric.noBiometrics.localized
        case .cancelled:
            return AppConfig.ErrorLocalisations.Biometric.cancelled.localized
        case .fallback:
            return AppConfig.ErrorLocalisations.Biometric.fallback.localized
        case .message(let message):
            return message
        }
    }
    
    case noBiometrics
    case cancelled
    case fallback
    case message(String)
}
