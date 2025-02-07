//
//  APIService.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 16.01.24.
//

import UIKit

enum APIService: ServiceProtocol {
    
    var baseURL: URL {
        return URL(string: NetworkConfig.Addresses.baseServer)!
    }
    
    var path: String {
        switch self {
        case .documents:
            return NetworkConfig.EP.API.documents
        case .downloadPDF(formioId: let id):
            return NetworkConfig.EP.API.downloadPDF.format(id)
        case .sendDocumentStatus(transactionId: let transactionId):
            return NetworkConfig.EP.API.sendDocumentStatus.format(transactionId)
        case .requestIdentity(egn: let egn):
            let language = LanguageProvider.shared.appLanguage?.short ?? ""
            return NetworkConfig.EP.API.requestIdentity.format(egn, language)
        case .authenticateIdentityRequest(parameters: let parameters):
            return NetworkConfig.EP.API.authenticateIdentityRequest.format(parameters.evrotrustTransactionId)
        case .uploadLogFile(egn: let egn):
            return NetworkConfig.EP.API.uploadLogFile.format(egn)
        case .getReceiptStatus(threadId: let threadId):
            return NetworkConfig.EP.API.getReceiptStatus.format(threadId)
        case .sdkAuthFailedStatus:
            return NetworkConfig.EP.API.sdkAuthFailedStatus
        }
    }
    
    var method: SMHTTPMethod {
        switch self {
        case .documents, .downloadPDF, .sendDocumentStatus, .requestIdentity, .getReceiptStatus:
            return .get
        case .authenticateIdentityRequest, .uploadLogFile, .sdkAuthFailedStatus:
            return .post
        }
    }
    
    var task: NetworkTask {
        switch self {
        case .documents(let parameters):
            return .requestParameters(parameters.getDictionary())
        case .authenticateIdentityRequest(let parameters):
            return .requestParameters(parameters.getDictionary())
        case .sdkAuthFailedStatus(let parameters):
            return .requestParameters(parameters.getDictionary())
        case .downloadPDF, .sendDocumentStatus, .requestIdentity, .uploadLogFile, .getReceiptStatus:
            return .requestPlain
        }
    }
    
    var headers: Headers? {
        var headers = [NetworkConfig.Headers.accept: NetworkConfig.Headers.applicationJson,
                       NetworkConfig.Headers.contentType: NetworkConfig.Headers.applicationJson]
        
        switch self {
        case .downloadPDF(formioId: _):
            headers = [NetworkConfig.Headers.accept: NetworkConfig.Headers.applicationPdf]
        case .uploadLogFile(egn: _):
            headers = [:]
        default: break
        }
        
        return headers
    }
    
    var parametersEncoding: ParametersEncoding {
        switch self {
        case .requestIdentity, .documents, .downloadPDF, .sendDocumentStatus, .uploadLogFile, .getReceiptStatus:
            return .url
        case .authenticateIdentityRequest, .sdkAuthFailedStatus:
            return .json
        }
    }
    
    case requestIdentity(egn: String)
    case authenticateIdentityRequest(parameters: AuthenticateIdentityRequestParamaters)
    case documents(documentsParameters: DocumentsParameters)
    case downloadPDF(formioId: String)
    case sendDocumentStatus(transactionId: String)
    case getReceiptStatus(threadId: String)
    case uploadLogFile(egn: String)
    case sdkAuthFailedStatus(parameters: SDKAuthFailedStatusParameters)
}
