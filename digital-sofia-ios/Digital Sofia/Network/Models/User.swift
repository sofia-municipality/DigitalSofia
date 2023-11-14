//
//  User.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 4.08.23.
//

import Foundation

struct User: Codable {
    var token: String?
    var refreshToken: String?
    
    var pin: String?
    var useBiometrics: Bool = false
    
    var email: String?
    
    var verified: Bool = false
    
    var firstName: String?
    var middleName: String?
    var lastName: String?
    
    var firstLatinName: String?
    var middleLatinName: String?
    var lastLatinName: String?
    
    var securityContext: String?
    var phone: String?
    var personalIdentificationNumber: String?
    
    var fullName: String {
        return (firstName ?? "") + " " + (middleName ?? "") + " " + (lastName ?? "")
    }
    
    var fullLatinName: String {
        return (firstLatinName ?? "") + " " + (middleLatinName ?? "") + " " + (lastLatinName ?? "")
    }
}
