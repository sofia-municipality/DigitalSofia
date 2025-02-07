//
//  NetworkManager+Receipts.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 21.11.24.
//

import UIKit

extension NetworkManager {
    static func getReceiptStatus(threadId: String, completion: @escaping (NetworkResponse<DocumentStatusResponse>) -> ()) {
        provider.runRequest(type: DocumentStatusResponse.self, service: APIService.getReceiptStatus(threadId: threadId))
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
