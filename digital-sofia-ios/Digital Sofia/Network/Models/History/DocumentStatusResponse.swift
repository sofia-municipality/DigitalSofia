//
//  DocumentStatusResponse.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 14.11.23.
//

import Foundation

struct DocumentStatusResponse: Codable {
    var status: String
    
    private enum CodingKeys: String, CodingKey {
        case status
    }
}
