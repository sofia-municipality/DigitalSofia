//
//  DocumentsViewModel.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 2.08.23.
//

import SwiftUI

@MainActor class DocumentsViewModel: ObservableObject {
    @Published var documents: [DocumentModel] = []
    @Published var isLoading = false
    @Published var isLoadingMore = false
    @Published var networkError: String?
    
    var successfullyFetchedDocuments: (() -> ())?
    private var statuses: [DocumentStatus]
    private var pageInfo: PageInfo?
    private var counter = 0
    
    init(statuses: [DocumentStatus]) {
        self.statuses = statuses
    }
    
    func refreshData() {
        pageInfo = nil
        isLoading = true
        documents = []
        loadPage(cursor: nil)
    }
    
    func loadData() {
        guard let pageInfo = pageInfo else {
            isLoading = true
            loadPage(cursor: nil)
            return
        }
        
        if pageInfo.hasNextPage {
            isLoadingMore = true
            loadPage(cursor: pageInfo.endCursor)
        }
    }
    
    private func loadPage(cursor: String?) {
        let parameters = DocumentsParameters(statuses: statuses, cursor: cursor)
        
        NetworkManager.getDocuments(parameters: parameters) { [weak self] response in
            switch response {
            case .success(let documentsResponse):
                self?.pageInfo = PageInfo(hasNextPage: documentsResponse.pagination.cursor != nil, endCursor: documentsResponse.pagination.cursor)
                
                if cursor == nil {
                    self?.documents = documentsResponse.documents
                } else {
                    self?.documents.append(contentsOf: documentsResponse.documents)
                }
                
                self?.isLoading = false
                self?.isLoadingMore = false
                self?.successfullyFetchedDocuments?()
                
            case .failure(let error):
                self?.isLoading = false
                self?.isLoadingMore = false
                
                if error != NetworkError.tokenExpired && error != NetworkError.noInternetConnection {
                    self?.networkError = error.description
                }
            }
        }
    }
}

public struct PageInfo: Equatable, Codable {
    public let hasNextPage: Bool
    public let endCursor: String?
    public static let `default`: PageInfo = PageInfo(hasNextPage: true, endCursor: nil)
}
