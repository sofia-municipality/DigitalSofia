//
//  CheckDebugModeResponse.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 1.02.24.
//

import Foundation

struct CheckDebugModeResponse: Codable {
    private var logLevel: Int
    var debugMode: Bool
    
    private enum CodingKeys: String, CodingKey {
        case logLevel
    }
    
    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        logLevel = try container.decode(Int.self, forKey: .logLevel)
        debugMode = logLevel == 0 ? false : true
    }
}
