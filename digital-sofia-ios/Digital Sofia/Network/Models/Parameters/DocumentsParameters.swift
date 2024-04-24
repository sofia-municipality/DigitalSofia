//
//  DocumentsParameters.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 2.08.23.
//

import Foundation

struct DocumentsParameters {
    var statuses: [DocumentStatus]
    var cursor: String?
    
    func getDictionary() -> [String: Any] {
        let statusString = statuses.map({ $0.rawValue }).joined(separator: ",")
        
        return ["status": statusString,
                "cursor": cursor ?? ""]
    }
}
