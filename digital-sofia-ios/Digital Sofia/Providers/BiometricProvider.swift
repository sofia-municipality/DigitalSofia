//
//  BiometricProvider.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 7.06.23.
//

import LocalAuthentication

class BiometricProvider {
    static var biometricType: BiometricType {
        let authContext = LAContext()
        let _ = authContext.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: nil)
        switch(authContext.biometryType) {
        case .none:
            return .none
        case .touchID:
            return .touch
        case .faceID:
            return .face
        default:
            return .none
        }
    }
    
    static func authenticate(completion: @escaping (Bool, BiometricError?) -> ()) {
        let context = LAContext()
        var error: NSError?
        
        if context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error) {
            let reason = "We need to unlock your data."
            
            context.evaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, localizedReason: reason) { success, authenticationError in
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
