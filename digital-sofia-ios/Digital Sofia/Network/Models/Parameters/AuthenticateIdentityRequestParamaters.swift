//
//  AuthenticateIdentityRequestParamaters.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 12.01.24.
//

import Foundation

struct AuthenticateIdentityRequestParamaters {
    var pin: String?
    var email: String?
    var phoneNumber: String?
    var fcm: String?
    var evrotrustTransactionId: String
    
    func getDictionary() -> [String: Any] {
        var dict = ["pin": pin ?? "",
                    "fcm": fcm ?? ""]
        
        if let email = email {
            dict["email"] = email
        }
        
        if let phoneNumber = phoneNumber {
            dict["phoneNumber"] = phoneNumber
        }
        
        return dict
    }
}
