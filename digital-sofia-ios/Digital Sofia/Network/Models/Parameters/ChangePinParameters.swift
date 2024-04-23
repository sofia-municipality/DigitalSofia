//
//  ChangePinParameters.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 24.01.24.
//

import Foundation

struct ChangePinParameters {
    var pin: String
    
    func getDictionary() -> [String: Any] {
        return ["pin": pin]
    }
}
