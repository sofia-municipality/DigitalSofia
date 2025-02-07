//
//  KeycloakService.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 22.01.24.
//

import UIKit

enum KeycloakService: ServiceProtocol {
    
    var baseURL: URL {
        switch self {
        case .checkUserForDeletion, .deleteUser:
            return URL(string: NetworkConfig.Addresses.baseServer)!
        default:
            return URL(string: NetworkConfig.Addresses.tokenServer)!
        }
    }
    
    var path: String {
        switch self {
        case .register:
            return NetworkConfig.EP.Keycloak.token
        case .firstRegister:
            return NetworkConfig.EP.Keycloak.firstRegister
        case .verifyPersonalId(egn: let egn):
            return NetworkConfig.EP.Keycloak.verifyPersonalId.format(egn)
        case .changePIN:
            return NetworkConfig.EP.Keycloak.changePIN
        case .deleteUser, .checkUserForDeletion:
            return NetworkConfig.EP.API.deleteUser
        case .updateAuthenticationCodeStatus:
            return NetworkConfig.EP.Keycloak.updateAuthenticationCodeStatus
        case .getAuthenticationCode:
            return NetworkConfig.EP.Keycloak.getAuthenticationCode
        case .changeFCM:
            return NetworkConfig.EP.Keycloak.changeFCM
        case .checkDebugMode(egn: let egn):
            return NetworkConfig.EP.Keycloak.checkDebugMode.format(egn)
        }
    }
    
    var method: SMHTTPMethod {
        switch self {
        case .verifyPersonalId, .getAuthenticationCode, .checkDebugMode, .checkUserForDeletion:
            return .get
        case .register, .changePIN, .updateAuthenticationCodeStatus, .firstRegister, .changeFCM:
            return .post
        case .deleteUser:
            return .delete
        }
    }
    
    var task: NetworkTask {
        switch self {
        case .firstRegister(let parameters):
            return .requestParameters(parameters.getDictionary())
        case .register(let parameters):
            return .requestParameters(parameters.getDictionary())
        case .updateAuthenticationCodeStatus(let parameters):
            return .requestParameters(parameters.getDictionary())
        case .changePIN(let parameters):
            return .requestParameters(parameters.getDictionary())
        case .changeFCM(let parameters):
            return .requestParameters(parameters.getDictionary())
        case .verifyPersonalId, .deleteUser, .getAuthenticationCode, .checkDebugMode, .checkUserForDeletion:
            return .requestPlain
        }
    }
    
    var headers: Headers? {
        var headers = [NetworkConfig.Headers.accept: NetworkConfig.Headers.applicationJson,
                       NetworkConfig.Headers.contentType: NetworkConfig.Headers.applicationJson]
        
        switch self {
        case .register:
            headers = [NetworkConfig.Headers.contentType: NetworkConfig.Headers.form]
        default: break
        }
        
        return headers
    }
    
    var parametersEncoding: ParametersEncoding {
        switch self {
        case .register, .verifyPersonalId, .deleteUser, .getAuthenticationCode, .checkDebugMode, .checkUserForDeletion:
            return .url
        case .firstRegister, .updateAuthenticationCodeStatus, .changePIN, .changeFCM:
            return .json
        }
    }
    
    case firstRegister(parameters: FirstRegisterParameters)
    case register(parameters: RegisterParameters)
    case verifyPersonalId(egn: String)
    case changePIN(parameters: ChangePinParameters)
    case deleteUser
    case checkUserForDeletion
    case updateAuthenticationCodeStatus(parameters: UpdateStatusCodeParameters)
    case getAuthenticationCode
    case changeFCM(parameters: ChangeFcmParameters)
    case checkDebugMode(egn: String)
}
