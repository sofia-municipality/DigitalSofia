//
//  UpdateStatusCodeParameters.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 12.12.23.
//

import Foundation

struct UpdateStatusCodeParameters {
    var code: String
    var status: LoginRequestStatus
    
    func getDictionary() -> [String: Any] {
        return ["code": code,
                "status": status.rawValue]
    }
}
