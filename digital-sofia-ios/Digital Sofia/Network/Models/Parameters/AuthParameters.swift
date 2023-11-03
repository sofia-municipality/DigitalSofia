//
//  AuthParameters.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 31.07.23.
//

import Foundation

struct RegisterParameters {
    var clientID: String?
    var clientSecret: String?
    var scope: String?
    var grantType: String?
    var pin: String?
    var egn: String?
    var phoneNumber: String?
    var email: String?
    var fcm: String?
    
    func getDictionary() -> [String: Any] {
        return [NetworkConfig.Parameters.clientId: clientID ?? "",
                NetworkConfig.Parameters.clientSecret: clientSecret ?? "",
                NetworkConfig.Parameters.scope: scope ?? "",
                NetworkConfig.Parameters.grantType: grantType ?? "",
                NetworkConfig.Parameters.pin: pin ?? "",
                NetworkConfig.Parameters.egn: egn ?? "",
                NetworkConfig.Parameters.phoneNumber: phoneNumber ?? "",
                NetworkConfig.Parameters.email: email ?? "",
                NetworkConfig.Parameters.fcm: fcm ?? ""]
    }
}

struct RefreshTokeParameters {
    var refreshToken: String?
    var clientID: String?
    var clientSecret: String?
    var scope: String?
    var grantType: String?
    
    func getDictionary() -> [String: Any] {
        return [NetworkConfig.Parameters.refreshToken: refreshToken ?? "",
                NetworkConfig.Parameters.clientId: clientID ?? "",
                NetworkConfig.Parameters.clientSecret: clientSecret ?? "",
                NetworkConfig.Parameters.scope: scope ?? "",
                NetworkConfig.Parameters.grantType: grantType ?? ""]
    }
}
