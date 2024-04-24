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
            return NetworkConfig.EP.API.requestIdentity.format(egn)
        case .authenticateIdentityRequest(parameters: let parameters):
            return NetworkConfig.EP.API.authenticateIdentityRequest.format(parameters.evrotrustTransactionId)
        case .uploadLogFile(egn: let egn):
            return NetworkConfig.EP.API.uploadLogFile.format(egn)
        }
    }
    
    var method: SMHTTPMethod {
        switch self {
        case .documents, .downloadPDF, .sendDocumentStatus, .requestIdentity:
            return .get
        case .authenticateIdentityRequest, .uploadLogFile:
            return .post
        }
    }
    
    var task: NetworkTask {
        switch self {
        case .documents(let parameters):
            return .requestParameters(parameters.getDictionary())
        case .authenticateIdentityRequest(let parameters):
            return .requestParameters(parameters.getDictionary())
        case .downloadPDF, .sendDocumentStatus, .requestIdentity, .uploadLogFile:
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
        case .requestIdentity, .documents, .downloadPDF, .sendDocumentStatus, .uploadLogFile:
            return .url
        case .authenticateIdentityRequest:
            return .json
        }
    }
    
    case requestIdentity(egn: String)
    case authenticateIdentityRequest(parameters: AuthenticateIdentityRequestParamaters)
    case documents(documentsParameters: DocumentsParameters)
    case downloadPDF(formioId: String)
    case sendDocumentStatus(transactionId: String)
    case uploadLogFile(egn: String)
}
