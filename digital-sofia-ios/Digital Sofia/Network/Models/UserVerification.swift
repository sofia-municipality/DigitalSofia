//
//  UserVerification.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 20.10.23.
//

import Foundation

struct UserVerification: Codable {
    var exists: Bool
    var hasPin: Bool
    
    private enum CodingKeys: String, CodingKey {
        case exists = "userExist", hasPin
    }
}

struct PINVerification: Codable {
    var matches: Bool
    
    private enum CodingKeys: String, CodingKey {
        case matches
    }
}
