//
//  AlertModifier.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 5.01.24.
//

import SwiftUI

struct AlertModifier: ViewModifier {
    @EnvironmentObject var appState: AppState
    
    func body(content: Content) -> some View {
        content
            .alert(item: $appState.alertItem) { alertItem in
                return AlertProvider.getAlertFor(alertItem: alertItem)
            }
    }
}

extension View {
    func alert() -> some View {
        self.modifier(AlertModifier())
    }
}
