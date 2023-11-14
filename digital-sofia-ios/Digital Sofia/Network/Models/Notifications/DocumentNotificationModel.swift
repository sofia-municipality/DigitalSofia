//
//  DocumentNotificationModel.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 23.08.23.
//

import Foundation

struct DocumentNotificationModel: Codable {
    let body: String
    
    private enum CodingKeys: String, CodingKey {
        case body
    }
}
