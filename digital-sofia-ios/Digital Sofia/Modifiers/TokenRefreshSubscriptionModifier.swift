//
//  TokenRefreshSubscriptionModifier.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 8.02.24.
//

import SwiftUI

struct TokenRefreshSubscriptionModifier: ViewModifier {
    @EnvironmentObject var appState: AppState
    private let action: (() -> Void)?
    
    init(perform action: (() -> Void)? = nil) {
        self.action = action
    }
    
    func body(content: Content) -> some View {
        content
            .onAppear {
                if appState.tokenRefreshed {
                    appState.tokenRefreshed = false
                }
            }
            .onChange(of: appState.tokenRefreshed) { newValue in
                if newValue {
                    action?()
                    appState.tokenRefreshed = false
                }
            }
    }
}

extension View {
    func onTokenRefresh(perform action: (() -> Void)? = nil) -> some View {
        self.modifier(TokenRefreshSubscriptionModifier(perform: action))
    }
}

