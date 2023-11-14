//
//  NetworkConfig.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 4.04.23.
//

import Foundation

enum Environments {
    case dev, DHCHOTest, DHCHODev
}

struct NetworkConfig {
    static var enviroment: Environments {
        return Environments.DHCHODev
    }
    
    struct EP {
        static let documents = "documents"
        static let token = "auth/realms/eServices/protocol/openid-connect/token"
        static let downloadPDF = "documents/%@/serve"
        static let verifyTransaction = "documents/authenticate/%@"
        static let verifyPersonalId = "auth/realms/eServices/personIdentifier/user?personIdentifier=%@"
        static let verifyPIN = "auth/realms/eServices/personIdentifier/pin?personIdentifier=%@&pin=%@"
        static let sendDocumentStatus = "documents/%@/status"
    }
    
    struct Addresses {
        static var baseServer: String {
            switch enviroment {
            case .dev:
                return ""
            case .DHCHOTest:
                return ""
            case .DHCHODev:
                return ""
            }
        }
        
        static var tokenServer: String {
            switch enviroment {
            case .dev:
                return ""
            case .DHCHOTest:
                return ""
            case .DHCHODev:
                return ""
            }
        }
        
        static var clientSecret: String {
            switch enviroment {
            case .dev:
                return ""
            case .DHCHOTest:
                return ""
            case .DHCHODev:
                return ""
            }
        }
        
        static var web: String {
            switch enviroment {
            case .dev:
                return ""
            case .DHCHOTest:
                return ""
            case .DHCHODev:
                return ""
            }
        }
    }
    
    struct Variables {
        static let clientID = "sofia-forms-flow-web"
        static let clientSecretLogin = ""
        static let grantTypePassword = "password"
        static let grantTypeRefresh = "refresh_token"
        static let scope = "openid"
    }
    
    struct Headers {
        static let authorization = "Authorization"
        static let token = "Bearer "
        
        static let accept = "accept"
        static let applicationJson = "application/json"
        static let applicationPdf = "application/pdf"
        
        static let contentType = "Content-Type"
        static let form = "application/x-www-form-urlencoded"
    }
    
    struct Parameters {
        static let clientId = "client_id"
        static let clientSecret = "client_secret"
        static let scope = "scope"
        static let grantType = "grant_type"
        static let pin = "pin"
        static let egn = "egn"
        static let phoneNumber = "phoneNumber"
        static let email = "email"
        static let fcm = "fcm"
        static let refreshToken = "refresh_token"
        static let username = "username"
        static let password = "password"
        static let status = "status"
        static let cursor = "cursor"
    }
    
    struct TestData {
        static let username = ""
        static let password = ""
        static let usernameMarto = ""
        static let passwordMarto = ""
        static let usernameZdravko = ""
        static let passwordZdravko = ""
        static let dummyPDFFileURL = ""
        static let dummyMultiPagePDFFileURL = ""
        static let expiredToken = ""
    }
}
