//
//  NetworkManager+User.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 12.01.24.
//

import UIKit

extension NetworkManager {
    static func change(pin: String, completion: @escaping (NetworkResponse<Bool>) -> ()) {
        let parameters = ChangePinParameters(pin: pin)
        provider.runRequest(type: EmptyEntity.self, service: KeycloakService.changePIN(parameters: parameters))
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
    
    static func deleteUser(completion: @escaping (NetworkResponse<Bool>) -> ()) {
        provider.runRequest(type: EmptyEntity.self, service: KeycloakService.deleteUser)
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
    
    static func changeFCM(completion: @escaping (NetworkResponse<Bool>) -> ()) {
        let parameters = ChangeFcmParameters(fcm: fcm)
        provider.runRequest(type: EmptyEntity.self, service: KeycloakService.changeFCM(parameters: parameters))
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
    
    static func checkDebugMode(completion: @escaping (NetworkResponse<CheckDebugModeResponse>) -> ()) {
        let personalIdentificationNumber = user?.personalIdentificationNumber ?? ""
        provider.runRequest(type: CheckDebugModeResponse.self,
                            service: KeycloakService.checkDebugMode(egn: personalIdentificationNumber))
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
    
    static func sendDebugFile(files: [String], completion: @escaping (NetworkResponse<Bool>) -> ()) {
        let personalIdentificationNumber = user?.personalIdentificationNumber ?? ""
        provider.uploadRequest(type: EmptyEntity.self, files: files, service: APIService.uploadLogFile(egn: personalIdentificationNumber))
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
