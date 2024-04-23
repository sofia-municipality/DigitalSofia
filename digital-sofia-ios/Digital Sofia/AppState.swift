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
    
    @Published var loginRequestCode: String?
    @Published var notificationTabToOpen: Int = 0
    
    @Published var hasPendingDocuments: Bool = false
    @Published var shouldLockScreen: Bool = false
    @Published var tokenRefreshed: Bool = false
    @Published var refreshDocuments: Bool = false
}

enum InitialAppState {
    case initial, home, wasLoggedOut, tokenExpired, lockScreen, error
}
