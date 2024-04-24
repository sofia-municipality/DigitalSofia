//
//  BiometricProvider.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 7.06.23.
//

import LocalAuthentication

class BiometricProvider {
    static let context = LAContext()
    
    static var biometricType: BiometricType {
        let authContext = LAContext()
        let _ = authContext.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: nil)
        switch(authContext.biometryType) {
        case .touchID:
            return .touch
        case .faceID:
            return .face
        default:
            return .none
        }
    }
    
    static var biometricsAvailable: Bool {
        var error: NSError?
        return context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error)
    }
    
    static func authenticate(completion: @escaping (Bool, BiometricError?) -> ()) {
        var error: NSError?
        
        if context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error) {
            context.evaluatePolicy(.deviceOwnerAuthenticationWithBiometrics,
                                   localizedReason: AppConfig.UI.Permissions.biometricts.localized) { success, authenticationError in
                if success {
                    completion(success, nil)
                } else {
                    if let errorCode = (authenticationError as? NSError)?.code {
                        if errorCode == -3 {
                            completion(false, BiometricError.fallback)
                        } else if errorCode == -2 {
                            completion(false, BiometricError.cancelled)
                        } else {
                            completion(false, BiometricError.message(authenticationError?.localizedDescription ?? ""))
                        }
                    } else {
                        completion(false, BiometricError.message(authenticationError?.localizedDescription ?? ""))
                    }
                }
            }
        } else {
            completion(false, BiometricError.noBiometrics)
        }
    }
}

enum BiometricType {
    case none
    case touch
    case face
}
