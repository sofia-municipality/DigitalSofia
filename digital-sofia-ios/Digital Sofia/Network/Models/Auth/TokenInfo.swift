//
//  TokenInfo.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 31.07.23.
//

import Foundation

struct TokenInfo: Codable {
    var accessToken: String
    var refreshToken: String
    var expiresIn: Int
    var refreshExpiresIn: Int
    
    private enum CodingKeys: String, CodingKey {
        case accessToken = "access_token",
             refreshToken = "refresh_token",
             expiresIn = "expires_in",
             refreshExpiresIn = "refresh_expires_in"
    }
}
