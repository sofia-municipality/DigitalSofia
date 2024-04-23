//
//  NetworkManager+Login.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 12.01.24.
//

import UIKit

extension NetworkManager {    
    static func getAuthenticationCode(completion: @escaping (NetworkResponse<GetCodeResponse>) -> ()) {
        provider.runRequest(type: GetCodeResponse.self, service: KeycloakService.getAuthenticationCode)
            .sink(receiveCompletion: { result in
                switch result {
                case .finished:
                    break
                case .failure(let error):
                    completion(.failure(error))
                }
            }) { response in
                completion(.success(response))
            }
            .store(in: &cancellables)
    }
    
    static func updateAuthenticationCodeStatus(code: String, status: LoginRequestStatus, completion: @escaping (NetworkResponse<UpdateStatusCodeResponse>) -> ()) {
        let parameters = UpdateStatusCodeParameters(code: code, status: status)
        
        provider.runRequest(type: UpdateStatusCodeResponse.self, service: KeycloakService.updateAuthenticationCodeStatus(parameters: parameters))
            .sink(receiveCompletion: { result in
                switch result {
                case .finished:
                    break
                case .failure(let error):
                    completion(.failure(error))
                }
            }) { response in
                completion(.success(response))
            }
            .store(in: &cancellables)
    }
}
