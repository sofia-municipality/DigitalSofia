//
//  ServiceProtocol.swift
//  webrtc-test
//
//  Created by Teodora Georgieva on 10.01.23.
//

import UIKit

typealias Headers = [String: String]

/// Protocol to enchance a service
protocol ServiceProtocol {
    
    /// Proporty to return the base url
    var baseURL: URL { get }
    
    /// Property to return each individual path of the service
    var path: String { get }
    
    /// Property to return each individual method type of the service
    var method: SMHTTPMethod { get }
    
    /// Property to return each individual task of the service
    var task: NetworkTask { get }
    
    /// Property to return the headers for each task of the service
    var headers: Headers? { get }
    
    /// Property to return the encoding of the parameters of the service
    var parametersEncoding: ParametersEncoding { get }
}

extension ServiceProtocol {
    var requestPath: URL {
        guard let path = baseURL.appendingPathComponent(path).absoluteString.removingPercentEncoding else {
            return URL.defaultURL
        }
        
        return URL(string: path)!
    }
}

