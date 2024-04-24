//
//  AuthParameters.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 31.07.23.
//

import Foundation

struct RegisterParameters {
    var clientID: String?
    var scope: String?
    var grantType: String?
    var pin: String?
    var egn: String?
    var phoneNumber: String?
    var email: String?
    var fcm: String?
    
    func getDictionary() -> [String: Any] {
        var dict =  [NetworkConfig.Parameters.clientId: clientID ?? "",
                     NetworkConfig.Parameters.scope: scope ?? "",
                     NetworkConfig.Parameters.grantType: grantType ?? "",
                     NetworkConfig.Parameters.pin: pin ?? "",
                     NetworkConfig.Parameters.egn: egn ?? "",
                     NetworkConfig.Parameters.fcm: fcm ?? "",
        ]
        
        if let phoneNumber = phoneNumber {
            dict[NetworkConfig.Parameters.phoneNumber] = phoneNumber
        }
        
        if let email = email {
            dict[NetworkConfig.Parameters.email] = email
        }
        
        return dict
    }
}
