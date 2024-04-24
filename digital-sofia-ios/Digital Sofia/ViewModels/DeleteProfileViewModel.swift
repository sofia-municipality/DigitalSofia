//
//  DeleteProfileViewModel.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 14.11.23.
//

import Foundation

@MainActor class DeleteProfileViewModel: ObservableObject {
    @Published var hasPendingRequests = false
    
    func deleteUser(completion: @escaping (NetworkError?) -> ()) {
        NetworkManager.deleteUser() { response in
            switch response {
            case .success(_):
                completion(nil)
                UserProvider.shared.logout()
                
            case .failure(let error):
                //                self?.hasPendingRequests = true
                completion(error)
            }
        }
    }
}
