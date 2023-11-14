//
//  History.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 5.04.23.
//

import Foundation

struct DocumentsModel: Codable {
    
    var documents: [DocumentModel]
    
    var pagination: Pagination
    
    private enum CodingKeys: String, CodingKey {
        case documents, pagination
    }
}

struct Pagination: Codable {
    var cursor: String?
    
    private enum CodingKeys: String, CodingKey {
        case cursor
    }
}
