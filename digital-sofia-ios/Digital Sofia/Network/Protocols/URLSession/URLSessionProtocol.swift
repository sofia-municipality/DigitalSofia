//
//  URLSessionProtocol.swift
//  webrtc-test
//
//  Created by Teodora Georgieva on 10.01.23.
//

import Foundation

/// Protocol to add additional functionality to the URLSession methods
//protocol URLSessionProtocol {
//  typealias DataTaskResult = (Data?, URLResponse?, Error?) -> ()
//  
//  /// Method to create a task that retrieves the contents of a URL based on the specified URL request object, and calls a handler upon completion
//  ///
//  /// - Parameters:
//  ///   - request: A URL request object
//  ///   - completionHandler: The completion handler to call when the load request is complete.
//  /// - Returns: A new sesson data task
//  func dataTask(request: URLRequest, completionHandler: @escaping DataTaskResult) -> URLSessionDataTask
//}
//
//extension URLSession: URLSessionProtocol {
//  func dataTask(request: URLRequest, completionHandler: @escaping DataTaskResult) -> URLSessionDataTask {
//    return dataTask(with: request, completionHandler: completionHandler)
//  }
//}
