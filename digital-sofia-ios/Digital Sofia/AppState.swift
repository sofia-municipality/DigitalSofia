//
//  AppState.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 21.07.23.
//

import Combine
import Foundation

final class AppState: ObservableObject {
    @Published var settingsViewId = UUID()
    @Published var language: Languages?
    @Published var alertItem: BaseAlertItem?
    @Published var state: InitialAppState?
    @Published var hasPendingDocuments: Bool = false
}

enum InitialAppState {
    case initial, home, wasLoggedOut, tokenExpired, error
}
