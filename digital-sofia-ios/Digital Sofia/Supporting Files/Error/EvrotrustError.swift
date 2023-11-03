//
//  EvrotrustError.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 11.08.23.
//

import Foundation

public enum EvrotrustError: Error {
    case errorInput
    case userCancelled
    case userNotSetUp
    case sdkNotSetUp
    
    public var description: String {
        switch self {
        case .errorInput:
            return AppConfig.ErrorLocalisations.Evrotrust.errorInput.localized
        case .userCancelled:
            return AppConfig.ErrorLocalisations.Evrotrust.userCancelled.localized
        case .userNotSetUp:
            return AppConfig.ErrorLocalisations.Evrotrust.userNotSetUp.localized
        case .sdkNotSetUp:
            return AppConfig.ErrorLocalisations.Evrotrust.sdkNotSetUp.localized
        }
    }
}
