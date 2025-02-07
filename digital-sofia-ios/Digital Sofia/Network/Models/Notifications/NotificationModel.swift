//
//  NotificationModel.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 23.08.23.
//

import Foundation

struct LoginCodeNotificationModel: Codable {
    let code: String
    let expiresAt: String
    
    private enum CodingKeys: String, CodingKey {
        case code, expiresAt
    }
}

struct DocumentNotificationModel: Codable {
    let transactionId: String
    private let status: String
    var docStatus: DocumentStatus
    
    private enum CodingKeys: String, CodingKey {
        case transactionId, status
    }
    
    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        
        transactionId = try container.decode(String.self, forKey: .transactionId)
        
        status = try container.decode(String.self, forKey: .status)
        docStatus = DocumentStatus(rawValue: status) ?? .unsigned
    }
}

struct UserStatusNotificationModel: Codable, Equatable {
    let isIdentified: Bool
    let isReadyToSign: Bool
    
    private enum CodingKeys: String, CodingKey {
        case isIdentified, isReadyToSign
    }
    
    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        
        let identified = try container.decode(String.self, forKey: .isIdentified)
        let readyToSign = try container.decode(String.self, forKey: .isReadyToSign)
        
        isIdentified = Bool(from: identified.lowercased())
        isReadyToSign = Bool(from: readyToSign.lowercased())
    }
}

struct FirebaseConsoleNotificationModel: Codable {
    let aps: AlertModel
    
    private enum CodingKeys: String, CodingKey {
        case aps
    }
}

struct AlertModel: Codable {
    let alert: NotificationModel
    
    private enum CodingKeys: String, CodingKey {
        case alert
    }
}

struct NotificationModel: Codable {
    let body: String
    let title: String
    
    private enum CodingKeys: String, CodingKey {
        case body, title
    }
}
