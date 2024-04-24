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
    
    func uploadRequest<T: Decodable>(type: T.Type, files: [String], service: ServiceProtocol) -> AnyPublisher<T, NetworkError> {
        if let uploadRequest = FileUploader.upload(files: files, service: service) {
            return uploadRequest
                .validate()
                .publishDecodable(type: T.self, emptyResponseCodes: emptyResponseCodes)
                .value()
                .mapError({ return self.mapError(afError: $0) })
                .receive(on: RunLoop.main)
                .eraseToAnyPublisher()
        }
        
        return Empty(completeImmediately: false).eraseToAnyPublisher()
    }
    
    // MARK: - Initialize/Livecycle methods
    
    // MARK: - Override methods
    
    // MARK: - Private properties
    
    private let decoder = JSONDecoder()
    private let retryLimit = 3
    private let emptyResponseCodes: Set = [201, 204, 205]
    
    // MARK: - Private methods
    
    private func run<T: Decodable>(request: DataRequest) -> AnyPublisher<T, NetworkError> {
        return request
            .responseDecodable(of: T.self, emptyResponseCodes: emptyResponseCodes) { _ in }
            .validate { [weak self] request, response, data in
                self?.printResponse(request: request, response: response, data: data)
                
                guard (200...299).contains(response.statusCode) else {
                    let error = try? self?.decoder.decode(ErrorResponse.self, from: data ?? Data())
                    return .failure(error == nil
                                    ? NetworkError.mapErrorBy(statusCode: response.statusCode)
                                    : NetworkError.message(error?.error ?? ""))
                }
                
                return .success(())
            }
            .publishDecodable(type: T.self, emptyResponseCodes: emptyResponseCodes)
            .value()
            .mapError({ return self.mapError(afError: $0) })
            .receive(on: RunLoop.main)
            .eraseToAnyPublisher()
    }
    
    private func mapError(afError: AFError) -> NetworkError {
        var mappedError: NetworkError?
        
        switch afError.responseCode ?? 0 {
        case 400:
            mappedError = NetworkError.badRequest
        case 401, 500...599:
            mappedError = tokenExpiredHandler()
        case 403:
            mappedError = NetworkError.forbidden
        case 404:
            mappedError = NetworkError.notFound
        default:
            print("------ AFERROR ------")
            print(afError)
            
            switch afError {
            case .responseValidationFailed(reason: let reason):
                switch reason {
                case .customValidationFailed(error: let error):
                    if let networkError = error as? NetworkError {
                        switch networkError.description {
                        case NetworkError.invalidUserData.serverErrorDescription:
                            if UserProvider.loginInitiated {
                                mappedError = tokenExpiredHandler()
                            } else {
                                mappedError = .invalidUserData
                            }
                        case NetworkError.logoutCountExceeded.serverErrorDescription:
                            mappedError = .logoutCountExceeded
                        case NetworkError.invalidUser.serverErrorDescription:
                            mappedError = tokenExpiredHandler()
                        default:
                            if networkError == .tokenExpired {
                                mappedError = tokenExpiredHandler()
                            } else {
                                mappedError = networkError
                            }
                        }
                    }
                default: break
                }
            default:
                if let error = (afError.underlyingError as? URLError) {
                    switch error.code {
                    case .timedOut:
                        mappedError = NetworkError.timeOut
                    case .notConnectedToInternet:
                        mappedError = NetworkError.noInternetConnection
                    default:
                        break
                    }
                }
            }
        }
        
        UserProvider.loginInitiated = false
        return mappedError ?? .unknown
    }
    
    private func tokenExpiredHandler() -> NetworkError {
        UserProvider.shared.tokenExpire()
        return NetworkError.tokenExpired
    }
    
    private func printResponse(request: URLRequest?, response: HTTPURLResponse, data: Data?) {
        let json = try? JSONSerialization.jsonObject(with: data ?? Data(), options: []) as? [String : Any]
        let response = "\(request?.cURL(pretty: true) ?? "")\nstatusCode: \(response.statusCode)\nand data: \(json ?? [:])"
        LoggingHelper.logRequest(response: response)
        
        print("------ REQUEST ------")
        print(response)
    }
}

extension AlamofireProvider: RequestInterceptor {
    func adapt(_ urlRequest: URLRequest, for session: Session, completion: @escaping (Result<URLRequest, Error>) -> Void) {
        var request = urlRequest
        
        guard let token = UserProvider.currentUser?.token else {
            completion(.success(request))
            return
        }
        
        let isTokenRequest = urlRequest.url?.absoluteString.contains(NetworkConfig.EP.Keycloak.token)
        if isTokenRequest == false {
            let bearerToken = NetworkConfig.Headers.token + token
            request.setValue(bearerToken, forHTTPHeaderField: NetworkConfig.Headers.authorization)
        }
        
        completion(.success(request))
    }
    
    func retry(_ request: Request, for session: Session, dueTo error: Error,
               completion: @escaping (RetryResult) -> Void) {
        guard let statusCode = request.response?.statusCode else {
            completion(.doNotRetry)
            return
        }
        
        print("------ RETRY REQUEST ------")
        print(request.request?.url?.absoluteString ?? "")
        //        print(request.cURLDescription())
        
        guard request.request?.url?.absoluteString.contains(NetworkConfig.EP.Keycloak.token) == false else {
            completion(.doNotRetry)
            return
        }
        
        guard request.retryCount < retryLimit else {
            completion(.doNotRetry)
            return
        }
        
        switch statusCode {
        case 200...299:
            completion(.doNotRetry)
        case 401:
            login { isSuccess in
                isSuccess ? completion(.retry) : completion(.doNotRetry)
            }
        case 443:
            DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                completion(.retry)
            }
        default:
            completion(.retry)
        }
    }
    
    private func login(completion: @escaping (_ isSuccess: Bool) -> Void) {
        UserProvider.shared.login { isSuccess in
            completion(isSuccess)
        }
    }
}

