//
//  VerifyPINViewModel.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 14.11.23.
//

import Foundation

@MainActor class VerifyPINViewModel: ObservableObject {
    @Published var nextScreen: VerifyPINNextScreen = .none
    
    func verify(pin: String, completion: @escaping (String?) -> ()) {
        NetworkManager.registerUser(pin: pin, isLogin: true) { response in
            switch response {
            case .success(_):
                UserProvider.shared.update(pin: pin)
                self.nextScreen = BiometricProvider.biometricsAvailable ? .biometrics : .authentication
            case .failure(let error):
                if error == .invalidUserData || error == .logoutCountExceeded {
                    completion(error.description)
                } else {
                    completion(error.description)
                }
            }
        }
    }
}
