//
//  HistoryItem.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 5.04.23.
//

import Foundation

struct DocumentModel: Codable, Hashable {
    
    var id: String?
    
    var created: String
    
    var modified: String?
    
    var expired: String?
    
    var signed: String?
    
    var rejected: String?
    
    var generated: String?
    
    var validUntill: String?
    
    var evrotrustThreadId: String?
    
    var evrotrustTransactionId: String?
    
    var fileName: String?
    
    var formioId: String?
    
    private var status: String
    
    var docStatus: DocumentStatus
    
    var referenceNumber: String?
    
    var fileUrl: String?
    
    var pdfPath: String {
        return NetworkConfig.Addresses.baseServer + NetworkConfig.EP.API.downloadPDF.format(formioId ?? "")
    }
    
    private enum CodingKeys: String, CodingKey {
        case id = "applicationId",
             created,
             modified,
             expired,
             validUntill,
             signed,
             generated,
             rejected,
             evrotrustThreadId,
             evrotrustTransactionId,
             fileName,
             formioId,
             status,
             referenceNumber,
             fileUrl
    }
    
    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        
        id = try container.decodeIfPresent(String.self, forKey: .id)
        created = try container.decode(String.self, forKey: .created)
        modified = try container.decodeIfPresent(String.self, forKey: .modified)
        expired = try container.decodeIfPresent(String.self, forKey: .expired)
        validUntill = try container.decodeIfPresent(String.self, forKey: .validUntill)
        signed = try container.decodeIfPresent(String.self, forKey: .signed)
        generated = try container.decodeIfPresent(String.self, forKey: .generated)
        rejected = try container.decodeIfPresent(String.self, forKey: .rejected)
        evrotrustThreadId = try container.decodeIfPresent(String.self, forKey: .evrotrustThreadId)
        evrotrustTransactionId = try container.decodeIfPresent(String.self, forKey: .evrotrustTransactionId)
        fileName = try container.decodeIfPresent(String.self, forKey: .fileName)
        formioId = try container.decodeIfPresent(String.self, forKey: .formioId)
        
        status = try container.decode(String.self, forKey: .status).lowercased()
        docStatus = DocumentStatus(rawValue: status) ?? .unsigned
        
        referenceNumber = try container.decodeIfPresent(String.self, forKey: .referenceNumber)
        fileUrl = try container.decodeIfPresent(String.self, forKey: .fileUrl)
    }
}

enum DocumentStatus: String {
    case unsigned = "unsigned",
         signing = "signing",
         signed = "signed",
         expired = "expired",
         rejected = "rejected",
         failed = "failed",
         delivering = "delivering",
         delivered = "generated"
    
    var localisedDescription: String {
        switch self {
        case .unsigned:
            return AppConfig.UI.Documents.unsigned.localized
        case .signing:
            return AppConfig.UI.Documents.signing.localized
        case .signed:
            return AppConfig.UI.Documents.signed.localized
        case .expired:
            return AppConfig.UI.Documents.expired.localized
        case .rejected:
            return AppConfig.UI.Documents.rejected.localized
        case .failed:
            return AppConfig.UI.Documents.failed.localized
        case .delivering:
            return AppConfig.UI.Documents.delivering.localized
        case .delivered:
            return AppConfig.UI.Documents.delivered.localized
        }
    }
}
