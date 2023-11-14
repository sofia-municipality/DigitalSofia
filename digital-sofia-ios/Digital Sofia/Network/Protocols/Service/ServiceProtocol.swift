//
//  ServiceProtocol.swift
//  webrtc-test
//
//  Created by Teodora Georgieva on 10.01.23.
//

import UIKit

typealias Headers = [String: String]

/// Protocol to enchance a service
protocol ServiceProtocol {
    
    /// Proporty to return the base url
    var baseURL: URL { get }
    
    /// Property to return each individual path of the service
    var path: String { get }
    
    /// Property to return each individual method type of the service
    var method: SMHTTPMethod { get }
    
    /// Property to return each individual task of the service
    var task: NetworkTask { get }
    
    /// Property to return the headers for each task of the service
    var headers: Headers? { get }
    
    /// Property to return the encoding of the parameters of the service
    var parametersEncoding: ParametersEncoding { get }
}

extension ServiceProtocol {
    var requestPath: URL {
        guard let path = baseURL.appendingPathComponent(path).absoluteString.removingPercentEncoding else {
            return URL.defaultURL
        }
        
        return URL(string: path)!
    }
}

enum DSService: ServiceProtocol {
    
    var baseURL: URL {
        switch self {
        case .register, .refreshToken, .verifyPersonalId, .verifyPIN:
            return URL(string: NetworkConfig.Addresses.tokenServer)!
        default: return URL(string: NetworkConfig.Addresses.baseServer)!
        }
    }
    
    var path: String {
        switch self {
        case .documents:
            return NetworkConfig.EP.documents
        case .register, .refreshToken:
            return NetworkConfig.EP.token
        case .downloadPDF(formioId: let id):
            return NetworkConfig.EP.downloadPDF.format(id)
        case .verifyTransaction(id: let id):
            return NetworkConfig.EP.verifyTransaction.format(id)
        case .verifyPersonalId(egn: let egn):
            return NetworkConfig.EP.verifyPersonalId.format(egn)
        case .verifyPIN(egn: let egn, pin: let pin):
            return NetworkConfig.EP.verifyPIN.format(egn, pin)
        case .sendDocumentStatus(transactionId: let transactionId):
            return NetworkConfig.EP.sendDocumentStatus.format(transactionId)
        }
    }
    
    var method: SMHTTPMethod {
        switch self {
        case .documents, .downloadPDF, .verifyTransaction, .verifyPersonalId, .verifyPIN, .sendDocumentStatus:
            return .get
        case .register, .refreshToken:
            return .post
        }
    }
    
    var task: NetworkTask {
        switch self {
        case .documents(let parameters):
            return .requestParameters(parameters.getDictionary())
        case .register(let parameters):
            return .requestParameters(parameters.getDictionary())
        case .refreshToken(let parameters):
            return .requestParameters(parameters.getDictionary())
        case .verifyTransaction:
            return .requestParameters(VerifyDocumentParameters().getDictionary())
        case .downloadPDF, .verifyPersonalId, .verifyPIN, .sendDocumentStatus:
            return .requestPlain
        }
    }
    
    var headers: Headers? {
        var headers = [NetworkConfig.Headers.accept: NetworkConfig.Headers.applicationJson]
        
        switch self {
        case .register, .refreshToken:
            headers = [NetworkConfig.Headers.contentType: NetworkConfig.Headers.form]
        case .downloadPDF(formioId: _):
            headers = [NetworkConfig.Headers.accept: NetworkConfig.Headers.applicationPdf]
        default: break
        }
        
        return headers
    }
    
    var parametersEncoding: ParametersEncoding {
        switch self {
        case .documents, .downloadPDF, .verifyTransaction, .verifyPersonalId, .verifyPIN, .sendDocumentStatus:
            return .url
        case .register, .refreshToken:
            return .json
        }
    }
    
    case documents(documentsParameters: DocumentsParameters)
    case downloadPDF(formioId: String)
    case register(parameters: RegisterParameters)
    case refreshToken(parameters: RefreshTokeParameters)
    case verifyTransaction(id: String)
    case verifyPersonalId(egn: String)
    case verifyPIN(egn: String, pin: String)
    case sendDocumentStatus(transactionId: String)
}
