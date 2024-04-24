//
//  NetworkManager+Documents.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 12.01.24.
//

import UIKit

extension NetworkManager {
    static func getDocuments(parameters: DocumentsParameters, completion: @escaping (NetworkResponse<DocumentsModel>) -> ()) {
        provider.runRequest(type: DocumentsModel.self, service: APIService.documents(documentsParameters: parameters))
            .sink(receiveCompletion: { result in
                switch result {
                case .finished:
                    break
                case .failure(let error):
                    completion(.failure(error))
                }
            }) { documents in
                completion(.success(documents))
            }
            .store(in: &cancellables)
    }
    
    static func downloadFile(filename: String, service: ServiceProtocol, completion: @escaping (NetworkResponse<URL>) -> ()) {
        provider.downloadRequest(filename: filename, service: service)
            .sink(receiveCompletion: { result in
                switch result {
                case .finished:
                    break
                case .failure(let error):
                    completion(.failure(error))
                }
            }) { url in
                completion(.success(url))
            }
            .store(in: &cancellables)
        
    }
    
    static func sendDocumentStatus(transactionId: String, completion: @escaping (NetworkResponse<DocumentStatusResponse>) -> ()) {
        provider.runRequest(type: DocumentStatusResponse.self, service: APIService.sendDocumentStatus(transactionId: transactionId))
            .sink(receiveCompletion: { result in
                switch result {
                case .finished:
                    break
                case .failure(let error):
                    completion(.failure(error))
                }
            }) { response in
                completion(.success(response))
            }
            .store(in: &cancellables)
    }
}
