//
//  SDKAuthFailedStatusParameters.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 6.01.25.
//

import Foundation

struct SDKAuthFailedStatusParameters {
    var identificationNumber: String
    
    func getDictionary() -> [String: Any] {
        return ["identification_number": identificationNumber]
    }
}
