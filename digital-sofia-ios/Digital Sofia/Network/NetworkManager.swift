//
//  NetworkManager.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 4.04.23.
//

import Combine
import UIKit

class NetworkManager {
    private static let provider = AlamofireProvider()
    private static var cancellables = Set<AnyCancellable>()
    private static let appDelegate = UIApplication.shared.delegate as? AppDelegate
}

extension NetworkManager {
    static func registerUser(user: User, fcmToken: String?, completion: @escaping (NetworkResponse<Bool>) -> ()) {
        let params = RegisterParameters(clientID: NetworkConfig.Variables.clientID,
                                        clientSecret: NetworkConfig.Addresses.clientSecret,
                                        scope: NetworkConfig.Variables.scope,
                                        grantType: NetworkConfig.Variables.grantTypePassword,
                                        pin: user.securityContext,
                                        egn: user.personalIdentificationNumber,
                                        phoneNumber: user.phone,
                                        email: user.email ?? "",
                                        fcm: fcmToken ?? "")
        
        provider.runRequest(type: TokenInfo.self, service: DSService.register(parameters: params))
            .sink(receiveCompletion: { result in
                switch result {
                case .finished:
                    break
                case .failure(let error):
                    completion(.failure(error))
                }
            }) { response in
                saveUserToken(info: response)
                completion(.success(true))
            }
            .store(in: &cancellables)
    }
    
    static func refreshToken(completion: @escaping (NetworkResponse<Bool>) -> ()) {
        let user = UserProvider.shared.getUser()
        let params = RefreshTokeParameters(refreshToken: user?.refreshToken,
                                           clientID: NetworkConfig.Variables.clientID,
                                           clientSecret: NetworkConfig.Addresses.clientSecret,
                                           scope: NetworkConfig.Variables.scope,
                                           grantType: NetworkConfig.Variables.grantTypeRefresh)
        
        provider.runRequest(type: TokenInfo.self, service: DSService.refreshToken(parameters: params))
            .sink(receiveCompletion: { result in
                switch result {
                case .finished:
                    break
                case .failure(let error):
                    completion(.failure(error))
                }
            }) { response in
                saveUserToken(info: response)
                completion(.success(true))
            }
            .store(in: &cancellables)
    }
    
    private static func saveUserToken(info: TokenInfo) {
        var user = UserProvider.shared.getUser()
        user?.token = info.accessToken
        user?.refreshToken = info.refreshToken
        UserProvider.shared.save(user: user)
    }
    
    static func getDocuments(parameters: DocumentsParameters, completion: @escaping (NetworkResponse<DocumentsModel>) -> ()) {
        provider.runRequest(type: DocumentsModel.self, service: DSService.documents(documentsParameters: parameters))
            .sink(receiveCompletion: { result in
                switch result {
                case .finished:
                    break
                case .failure(let error):
                    completion(.failure(error))
                }
            }) { documents in
                completion(.success(documents))
            }
            .store(in: &cancellables)
    }
    
    static func downloadFile(filename: String, service: ServiceProtocol, completion: @escaping (NetworkResponse<URL>) -> ()) {
        provider.downloadRequest(filename: filename, service: service)
            .sink(receiveCompletion: { result in
                switch result {
                case .finished:
                    break
                case .failure(let error):
                    completion(.failure(error))
                }
            }) { url in
                completion(.success(url))
            }
            .store(in: &cancellables)
        
    }
    
    static func verifyTransaction(id: String, completion: @escaping (NetworkResponse<Bool>) -> ()) {
        provider.runRequest(type: TokenInfo.self, service: DSService.verifyTransaction(id: id))
            .sink(receiveCompletion: { result in
                switch result {
                case .finished:
                    break
                case .failure(let error):
                    completion(.failure(error))
                }
            }) { response in
                saveUserToken(info: response)
                
                var user = UserProvider.shared.getUser()
                if user?.verified == false {
                    user?.verified = true
                    UserProvider.shared.save(user: user)
                }
                
                completion(.success(true))
            }
            .store(in: &cancellables)
    }
    
    static func verifyPersonalId(egn: String, completion: @escaping (NetworkResponse<UserVerification>) -> ()) {
        provider.runRequest(type: UserVerification.self, service: DSService.verifyPersonalId(egn: egn))
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
    
    static func verifyPIN(pin: String, completion: @escaping (NetworkResponse<PINVerification>) -> ()) {
        let user = UserProvider.shared.getUser()
        let hashedPin = pin.getHashedPassword ?? ""
        
        provider.runRequest(type: PINVerification.self, service: DSService.verifyPIN(egn: user?.personalIdentificationNumber ?? "", pin: hashedPin))
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
    
    static func sendDocumentStatus(transactionId: String, completion: @escaping (NetworkResponse<Bool>) -> ()) {
        provider.runRequest(type: TokenInfo.self, service: DSService.sendDocumentStatus(transactionId: transactionId))
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
}
