//
//  ChangeFcmParameters.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 24.01.24.
//

import Foundation

struct ChangeFcmParameters {
    var fcm: String
    
    func getDictionary() -> [String: Any] {
        return ["fcm": fcm]
    }
}
