//
//  AlamofireProvider.swift
//  webrtc-test
//
//  Created by Teodora Georgieva on 10.01.23.
//

import Foundation
import Combine
import Alamofire

final class AlamofireProvider: ProviderProtocol {
    
    // MARK: - Singleton properties
    
    // MARK: - Static properties
    
    // MARK: - Public properties
    
    // MARK: - Public methods
    
    func runRequest<T: Decodable>(type: T.Type, service: ServiceProtocol) -> AnyPublisher<T, NetworkError> {
        let request = AF.initWith(service: service, interceptor: self)
        return run(request: request)
    }
    
    func downloadRequest(filename: String, service: ServiceProtocol) -> AnyPublisher<URL, NetworkError> {
        let destination: DownloadRequest.Destination? = { _, _ in
            var documentsURL = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
            documentsURL.appendPathComponent(filename)
            return (documentsURL, [.removePreviousFile])
        }
        
        let request = AF.initDownloadWith(service: service, interceptor: self, to: destination)
        
        return request
            .validate()
            .publishURL()
            .value()
            .mapError({ return self.mapError(afError: $0) })
            .receive(on: RunLoop.main)
            .eraseToAnyPublisher()
    }
    
    // MARK: - Initialize/Livecycle methods
    
    // MARK: - Override methods
    
    // MARK: - Private properties
    
    fileprivate let retryLimit = 3
    fileprivate let verifyDocumentRetryLimit = 40
    
    // MARK: - Private methods
    
    private func run<T: Decodable>(request: DataRequest) -> AnyPublisher<T, NetworkError> {
        return request
            .responseDecodable(of: T.self) { _ in }
            .validate { request, response, data in
                print(request)
                print(response)
                
                var errorDescription: String?
                guard let data = data else { return .failure(NetworkError.noJSONData) }
                let json = try? JSONSerialization.jsonObject(with: data, options: []) as? [String : Any]
                errorDescription = json?["error"] as? String
                print(json ?? [:])
                
                if errorDescription == nil && (200...299).contains(response.statusCode) {
                    return .success(())
                } else {
                    return .failure(errorDescription == nil
                                    ? NetworkError.mapErrorBy(statusCode: response.statusCode)
                                    : NetworkError.message(errorDescription ?? ""))
                }
            }
            .publishDecodable(type: T.self)
            .value()
            .mapError({ return self.mapError(afError: $0) })
            .receive(on: RunLoop.main)
            .eraseToAnyPublisher()
    }
    
    private func mapError(afError: AFError) -> NetworkError {
        switch afError.responseCode {
        case 400:
            return NetworkError.badRequest
        case 401:
            NotificationCenter.default.post(name: NSNotification.Name.tokenExpiredNotification,
                                            object: nil,
                                            userInfo: [:])
            return NetworkError.tokenExpired
        case 403:
            return NetworkError.forbidden
        case 404:
            return NetworkError.notFound
        case 503:
            return NetworkError.unavailable
        case 500:
            return NetworkError.internal
        default:
            var message = afError.localizedDescription
            
            switch afError {
            case .responseValidationFailed(reason: let reason):
                switch reason {
                case .customValidationFailed(error: let error):
                    if let networkError = error as? NetworkError {
                        message = networkError.description
                    }
                default: break
                }
            default: break
            }
            
            return NetworkError.message(message)
        }
    }
}

extension AlamofireProvider: RequestInterceptor {
    func adapt(_ urlRequest: URLRequest, for session: Session, completion: @escaping (Result<URLRequest, Error>) -> Void) {
        var request = urlRequest
        
        guard let token = UserProvider.shared.getUser()?.token else {
            completion(.success(request))
            return
        }
        
        let bearerToken = NetworkConfig.Headers.token + token
        request.setValue(bearerToken, forHTTPHeaderField: NetworkConfig.Headers.authorization)
        completion(.success(request))
    }
    
    func retry(_ request: Request, for session: Session, dueTo error: Error,
               completion: @escaping (RetryResult) -> Void) {
        guard let statusCode = request.response?.statusCode else {
            completion(.doNotRetry)
            return
        }
        
        let retryCount = statusCode == 443 ? verifyDocumentRetryLimit : retryLimit
        
        guard request.retryCount < retryCount else {
            completion(.doNotRetry)
            return
        }
        
        switch statusCode {
        case 200...299:
            completion(.doNotRetry)
        case 400:
            if request.cURLDescription().contains("\(NetworkConfig.Parameters.grantType)=\(NetworkConfig.Variables.grantTypeRefresh)") {
                register { isSuccess in isSuccess ? completion(.retry) : completion(.doNotRetry) }
            } else {
                completion(.retry)
            }
        case 401:
            if request.cURLDescription().contains("\(NetworkConfig.Parameters.grantType)=\(NetworkConfig.Variables.grantTypeRefresh)") {
                register { isSuccess in isSuccess ? completion(.retry) : completion(.doNotRetry) }
            } else {
                refreshToken { isSuccess in isSuccess ? completion(.retry) : completion(.doNotRetry) }
            }
        case 443:
            DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                completion(.retry)
            }
        default:
            completion(.retry)
        }
    }
    
    func refreshToken(completion: @escaping (_ isSuccess: Bool) -> Void) {
        NetworkManager.refreshToken() { response in
            switch response {
            case .failure(let error):
#if DEBUG
                print("Error when refreshing token: \(error)")
#endif
                completion(false)
            case .success(let success):
                completion(success)
            }
        }
    }
    
    func register(completion: @escaping (_ isSuccess: Bool) -> Void) {
        if let user = UserProvider.shared.getUser() {
            NetworkManager.registerUser(user: user, fcmToken: nil) { response in
                switch response {
                case .failure(let error):
#if DEBUG
                    print("Error when refreshing token from register: \(error)")
#endif
                    completion(false)
                case .success(let success):
                    completion(success)
                }
            }
        }
    }
}

