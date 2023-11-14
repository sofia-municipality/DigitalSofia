//
//  ProviderProtocol.swift
//  webrtc-test
//
//  Created by Teodora Georgieva on 10.01.23.
//

import Combine
import Alamofire

protocol ProviderProtocol {
    
    /// Method to create a request based on a decodable object
    /// - Parameters:
    ///   - type: The decodable object type
    ///   - service: The service of the request
    func runRequest<T: Decodable>(type: T.Type, service: ServiceProtocol) -> AnyPublisher<T, NetworkError>
    
    /// Method to create a download request
    /// - Parameters:
    ///   - path: The path to the file to download
    func downloadRequest(filename: String, service: ServiceProtocol) -> AnyPublisher<URL, NetworkError>
}
