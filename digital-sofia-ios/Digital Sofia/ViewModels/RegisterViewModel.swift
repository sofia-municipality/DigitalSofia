//
//  RegisterViewModel.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 14.11.23.
//

import Foundation

@MainActor class RegisterViewModel: ObservableObject {
    @Published var nextScreen: RegisterFlowNextScreen = .none
    
    func check(personalNumber: String, completion: @escaping (NetworkError?) -> ()) {
        NetworkManager.verifyPersonalId(egn: personalNumber) { [weak self] response in
            switch response {
            case .success(let userVerification):
                var user = UserProvider.currentUser
                user?.personalIdentificationNumber = personalNumber
                user?.exists = userVerification.exists
                user?.verified = userVerification.isVerified
                UserProvider.shared.save(user: user)
                
                LoggingHelper.checkUserDebugMode()
                
                if userVerification.hasContactInfo {
                    completion(nil)
                    self?.nextScreen = userVerification.hasPin ? .verifyPin : .createPin
                } else {
                    self?.registerUser(personalNumber: personalNumber) { networkError in
                        if let error = networkError {
                            completion(error)
                        } else {
                            completion(nil)
                            self?.nextScreen = .contactInfo
                        }
                    }
                }
                
            case .failure(let error):
                completion(error)
            }
        }
    }
    
    private func registerUser(personalNumber: String, completion: @escaping (NetworkError?) -> ()) {
        NetworkManager.firstRegister(personIdentifier: personalNumber) { response in
            switch response {
            case .success(_):
                completion(nil)
            case .failure(let error):
                completion(error)
            }
        }
    }
}
