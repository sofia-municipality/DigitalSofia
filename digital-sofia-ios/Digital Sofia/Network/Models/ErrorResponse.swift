//
//  ErrorResponse.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 14.11.23.
//

import Foundation

struct ErrorResponse: Decodable, Error {
    let error: String
    let errorDescription: String?
    
    private enum CodingKeys: String, CodingKey {
        case error, errorDescription = "error_description"
    }
}
