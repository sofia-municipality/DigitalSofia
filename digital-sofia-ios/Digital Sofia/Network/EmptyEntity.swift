//
//  EmptyEntity.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 15.11.23.
//

import Alamofire

struct EmptyEntity: Codable, EmptyResponse {
    static func emptyValue() -> EmptyEntity {
        return EmptyEntity.init()
    }
}
