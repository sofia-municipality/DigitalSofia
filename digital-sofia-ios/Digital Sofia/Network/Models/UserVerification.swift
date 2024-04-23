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
    var isVerified: Bool
    var hasContactInfo: Bool
    
    private enum CodingKeys: String, CodingKey {
        case exists = "userExist", hasPin, isVerified, hasContactInfo
    }
}
