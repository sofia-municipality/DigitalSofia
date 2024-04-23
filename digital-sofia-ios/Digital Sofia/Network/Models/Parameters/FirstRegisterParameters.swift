//
//  FirstRegisterParameters.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 12.01.24.
//

import Foundation

struct FirstRegisterParameters {
    var personIdentifier: String
    
    func getDictionary() -> [String: Any] {
        return ["personIdentifier": personIdentifier]
    }
}
