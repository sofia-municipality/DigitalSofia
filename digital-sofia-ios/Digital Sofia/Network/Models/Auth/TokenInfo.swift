//
//  TokenInfo.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 31.07.23.
//

import Foundation

struct TokenInfo: Codable {
    var accessToken: String?
    var refreshToken: String?
    
    private enum CodingKeys: String, CodingKey {
        case accessToken = "access_token",
             refreshToken = "refresh_token"
    }
    
    init(accessToken: String, refreshToken: String) {
        self.accessToken = accessToken
        self.refreshToken = refreshToken
    }
}
