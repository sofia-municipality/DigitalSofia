//
//  Task.swift
//  webrtc-test
//
//  Created by Teodora Georgieva on 10.01.23.
//

import Foundation

typealias Parameters = [String: Any]

enum NetworkTask {
  case requestPlain
  case requestParameters(Parameters)
}

extension NetworkTask {
    func getParamDictionary() -> [String: Any] {
        switch self {
        case .requestPlain:
            return [:]
        case .requestParameters(let params):
            return params
        }
    }
}
