//
//  HTTPMethod.swift
//  webrtc-test
//
//  Created by Teodora Georgieva on 10.01.23.
//

import Alamofire

enum SMHTTPMethod: String {
    case get = "GET"
    case post = "POST"
    case put = "PUT"
    case delete = "DELETE"
}

extension SMHTTPMethod {
    var afMethod: HTTPMethod {
        switch self {
        case .get:
            return .get
        case .post:
            return .post
        case .put:
            return .put
        case .delete:
            return .delete
        }
    }
}
