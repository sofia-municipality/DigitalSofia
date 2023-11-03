//
//  HTTPHeaders+Extension.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 4.04.23.
//

import Alamofire

extension HTTPHeaders {
    static func service(_ headers: Headers?) -> HTTPHeaders {
        var afHeaders = HTTPHeaders()
        
        headers?.forEach { header in
            let afHeader = HTTPHeader(name: header.key, value: header.value)
            afHeaders.add(afHeader)
        }
        
        return afHeaders
    }
}
