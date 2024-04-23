//
//  UpdateStatusCodeResponse.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 12.12.23.
//

import Foundation

struct GetCodeResponse: Codable {
    var code: String?
    var expiresIn: Int
    var codeExists: Bool
    
    private enum CodingKeys: String, CodingKey {
        case code, expiresIn, codeExists
    }
}

struct UpdateStatusCodeResponse: Codable {
    var codeUpdated: Bool
    
    private enum CodingKeys: String, CodingKey {
        case codeUpdated
    }
}
