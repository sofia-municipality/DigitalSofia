//
//  NetworkError.swift
//  webrtc-test
//
//  Created by Teodora Georgieva on 10.01.23.
//

import Foundation

public enum NetworkError: Error, Comparable {
    // SERVER
    case unknown
    case `internal`
    case notFound
    case forbidden
    case noJSONData
    case unavailable
    case parsing
    case badRequest
    case tokenExpired
    case noInternetConnection
    case timeOut
    
    // VALIDATION
    case invalidUserData
    case invalidUser
    case logoutCountExceeded
    
    // GENERAL
    case message(String)
    
    public var description: String {
        switch self {
        case .noJSONData:
            return AppConfig.ErrorLocalisations.Network.noJSONData.localized
        case .internal:
            return AppConfig.ErrorLocalisations.Network.internalServer.localized
        case .notFound:
            return AppConfig.ErrorLocalisations.Network.notFound.localized
        case .forbidden:
            return AppConfig.ErrorLocalisations.Network.forbidden.localized
        case .unavailable:
            return AppConfig.ErrorLocalisations.Network.unavailable.localized
        case .unknown:
            return AppConfig.ErrorLocalisations.Network.unknown.localized
        case .badRequest:
            return AppConfig.ErrorLocalisations.Network.badRequest.localized
        case .parsing:
            return AppConfig.ErrorLocalisations.Network.parsing.localized
        case .tokenExpired:
            return AppConfig.ErrorLocalisations.Network.tokenExpired.localized
        case .noInternetConnection:
            return AppConfig.ErrorLocalisations.Network.noInternetConnection.localized
        case .timeOut:
            return AppConfig.ErrorLocalisations.Network.noInternetConnection.localized
        case .invalidUserData, .invalidUser:
            return AppConfig.ErrorLocalisations.Network.invalidUserData.localized
        case .logoutCountExceeded:
            return AppConfig.ErrorLocalisations.Network.logoutCountExceeded.localized
        case .message(let message):
            return message
        }
    }
    
    static func mapErrorBy(statusCode: Int) -> NetworkError {
        switch statusCode {
        case 400:
            return NetworkError.badRequest
        case 401:
            return NetworkError.tokenExpired
        case 403:
            return NetworkError.forbidden
        case 404:
            return NetworkError.notFound
        case 503:
            return NetworkError.unavailable
        case 500:
            return NetworkError.internal
        default:
            return NetworkError.unknown
        }
    }
    
    public var serverErrorDescription: String {
        switch self {
        case .logoutCountExceeded:
            return "Login count exceeded!"
        case .invalidUserData:
            return "invalid user data"
        case .invalidUser:
            return "User does not exist!"
        default: return ""
        }
    }
}
