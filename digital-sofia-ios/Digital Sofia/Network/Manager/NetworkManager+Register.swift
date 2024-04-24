//
//  NetworkManager+Register.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 12.01.24.
//

import UIKit

extension NetworkManager {
    static func firstRegister(personIdentifier: String, completion: @escaping (NetworkResponse<Bool>) -> ()) {
        let parameters = FirstRegisterParameters(personIdentifier: personIdentifier)
        provider.runRequest(type: EmptyEntity.self, service: KeycloakService.firstRegister(parameters: parameters))
            .sink(receiveCompletion: { result in
                switch result {
                case .finished:
                    break
                case .failure(let error):
                    print(error)
                    completion(.failure(error))
                }
            }) { response in
                completion(.success(true))
            }
            .store(in: &cancellables)
    }
    
    static func requestIdentity(personIdentifier: String, completion: @escaping (NetworkResponse<DocumentModel>) -> ()) {
        provider.runRequest(type: DocumentModel.self, service: APIService.requestIdentity(egn: personIdentifier))
            .sink(receiveCompletion: { result in
                switch result {
                case .finished:
                    break
                case .failure(let error):
                    print(error)
                    completion(.failure(error))
                }
            }) { response in
                completion(.success(response))
            }
            .store(in: &cancellables)
    }
    
    static func authenticateIdentityRequest(evrotrustTransactionId: String,
                                            shouldAddContactInfo: Bool = false,
                                            completion: @escaping (NetworkResponse<Bool>) -> ()) {
        var parameters = AuthenticateIdentityRequestParamaters(pin: user?.pin?.getHashedPassword,
                                                               fcm: fcm,
                                                               evrotrustTransactionId: evrotrustTransactionId)
        
        if shouldAddContactInfo {
            parameters.email = user?.email
            parameters.phoneNumber = user?.phone
        }
        
        provider.runRequest(type: EmptyEntity.self, service: APIService.authenticateIdentityRequest(parameters: parameters))
            .sink(receiveCompletion: { result in
                switch result {
                case .finished:
                    break
                case .failure(let error):
                    completion(.failure(error))
                }
            }) { response in
                completion(.success(true))
            }
            .store(in: &cancellables)
    }
    
    static func registerUser(pin: String? = nil, isLogin: Bool, completion: @escaping (NetworkResponse<Bool>) -> ()) {
        DispatchQueue.main.async {
            var params = RegisterParameters(clientID: NetworkConfig.Variables.clientID,
                                            scope: NetworkConfig.Variables.scope,
                                            grantType: NetworkConfig.Variables.grantTypePassword,
                                            pin: user?.securityContext ?? pin?.getHashedPassword,
                                            egn: user?.personalIdentificationNumber,
                                            fcm: fcm)
            
            if isLogin == false {
                params.phoneNumber = user?.phone
                params.email = user?.email
            }
            
            provider.runRequest(type: TokenInfo.self, service: KeycloakService.register(parameters: params))
                .sink(receiveCompletion: { result in
                    switch result {
                    case .finished:
                        break
                    case .failure(let error):
                        completion(.failure(error))
                    }
                }) { response in
                    UserProvider.shared.updateUserToken(info: response, shouldVerify:  UserProvider.isVerified == false)
                    completion(.success(true))
                }
                .store(in: &cancellables)
        }
    }
    
    static func verifyPersonalId(egn: String, completion: @escaping (NetworkResponse<UserVerification>) -> ()) {
        provider.runRequest(type: UserVerification.self, service: KeycloakService.verifyPersonalId(egn: egn))
            .sink(receiveCompletion: { result in
                switch result {
                case .finished:
                    break
                case .failure(let error):
                    print(error)
                    completion(.failure(error))
                }
            }) { response in
                completion(.success(response))
            }
            .store(in: &cancellables)
    }
}
