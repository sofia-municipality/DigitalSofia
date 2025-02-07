//
//  EvrotrustError.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 11.08.23.
//

import EvrotrustSDK

public enum EvrotrustError: Error {
    case errorInput
    case userCancelled
    case userNotSetUp
    case sdkNotSetUp
    case editUser
    case userNotReadyToSign
    
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
        case .userNotReadyToSign:
            return AppConfig.UI.Evrotrust.etUserNotReadyToSign.evrotrustLocalized
        default: return ""
        }
    }
    
    public static func getError(for status: EvrotrustResultStatus) -> EvrotrustError? {
        switch status {
        case .errorInput:
            return EvrotrustError.errorInput
        case .userCanceled:
            return EvrotrustError.userCancelled
        case .userNotSetUp:
            return EvrotrustError.userNotSetUp
        case .sdkNotSetUp:
            return EvrotrustError.sdkNotSetUp
        default:
            return nil
        }
    }
    
    public static func sdkNotSetupHandler() {
        UserProvider.shared.invalidateOldUserSession()
        NotificationCenter.default.post(name: NSNotification.Name.evrotrustSDKNotSetupNotification,
                                        object: nil,
                                        userInfo: [:])
    }
}
