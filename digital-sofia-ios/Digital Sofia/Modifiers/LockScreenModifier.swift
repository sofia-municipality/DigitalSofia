//
//  LockScreenModifier.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 5.01.24.
//

import SwiftUI

struct LockScreenModifier: ViewModifier {
    @EnvironmentObject var appState: AppState
    @EnvironmentObject var networkMonitor: NetworkMonitor
    
    @State private var showLockcreen = false
    
    func body(content: Content) -> some View {
        content
            .onChange(of: appState.shouldLockScreen) { newValue in
                if newValue == true {
                    showLockcreen = true
                }
            }
            .fullScreenCover(isPresented: $showLockcreen, content: {
                loginView()
            })
    }
    
    private func loginView() -> some View {
        LoginScreenHelper.onLockScreenCompletion = {
            showLockcreen = false
            appState.shouldLockScreen = false
        }
        
        return NavigationView {
            LoginScreenHelper.loginScreen
                .environmentObject(networkMonitor)
                .environmentObject(appState)
        }
        .navigationBarHidden(true)
    }
}

extension View {
    func lockScreen() -> some View {
        self.modifier(LockScreenModifier())
    }
}
