//
//  ConfirmAuthenticationViewModel.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 22.01.24.
//

import Foundation

@MainActor class ConfirmAuthenticationViewModel: ObservableObject {
    
    func register(completion: @escaping (String?) -> ()) {
        if let user = UserProvider.currentUser {
            let isLogin = user.exists ? true : false
            NetworkManager.registerUser(isLogin: isLogin) { response in
                switch response {
                case .failure(let networkError):
                    completion(networkError.description)
                case .success(_):
                    completion(nil)
                }
            }
        }
    }
}
