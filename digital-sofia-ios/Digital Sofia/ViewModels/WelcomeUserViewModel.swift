//
//  WelcomeUserViewModel.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 14.11.23.
//

import Foundation

@MainActor class WelcomeUserViewModel: ObservableObject {
    enum WelcomeUserViewState: Equatable {
        case success, error(description: String)
    }
    
    @Published var state: WelcomeUserViewState?
}
