//
//  SettingsViewModel.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 3.12.24.
//

import Foundation

@MainActor class SettingsViewModel: ObservableObject {
    @Published var navigateToDeleteUser = false
    @Published var canDeleteUser = false
    @Published var isLoading = false
    @Published var networkError: String?
    
    func checkDeleteStatus() {
        isLoading = true
        NetworkManager.checkUserForDeletion() { [weak self] response in
            self?.isLoading = false
            switch response {
            case .success(_):
                self?.navigateToDeleteUser = true
                self?.canDeleteUser = true
            case .failure(let error):
                if error.description == "User has unfinished application" {
                    self?.navigateToDeleteUser = true
                    self?.canDeleteUser = false
                } else {
                    self?.networkError = error.description
                    self?.navigateToDeleteUser = false
                }
            }
        }
    }
}

