//
//  DeleteProfileViewModel.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 14.11.23.
//

import Foundation

@MainActor class DeleteProfileViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var networkError: String?
    
    func deleteUser(completion: @escaping (NetworkError?) -> ()) {
        isLoading = true
        NetworkManager.deleteUser() { [weak self] response in
            self?.isLoading = false
            switch response {
            case .success(_):
                UserProvider.shared.logout()
                completion(nil)
            case .failure(let error):
                self?.networkError = error.description
                completion(error)
            }
        }
    }
}
