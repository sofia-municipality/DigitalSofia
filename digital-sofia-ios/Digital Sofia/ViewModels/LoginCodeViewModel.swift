//
//  LoginCodeViewModel.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 4.12.23.
//

import Foundation

@MainActor class LoginCodeViewModel: ObservableObject {
    var loginCode: String?
    
    func getAuthenticationCode(completion: ((GetCodeResponse?, NetworkError?) -> ())?) {
        NetworkManager.getAuthenticationCode { [weak self] response in
            switch response {
            case .failure(let error):
                completion?(nil, error)
            case .success(let response):
                self?.loginCode = response.code
                completion?(response, nil)
            }
        }
    }
    
    func updateAuthenticationCodeStatus(response: LoginRequestStatus, completion: ((Bool?, NetworkError?) -> ())?) {
        NetworkManager.updateAuthenticationCodeStatus(code: loginCode ?? "", status: response) { response in
            switch response {
            case .failure(let error):
                completion?(false, error)
            case .success(let response):
                completion?(response.codeUpdated, nil)
            }
        }
    }
}
