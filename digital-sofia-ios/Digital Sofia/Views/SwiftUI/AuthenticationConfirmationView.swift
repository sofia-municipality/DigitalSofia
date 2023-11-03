//
//  AuthenticationConfirmationView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 24.08.23.
//

import SwiftUI

struct AuthenticationConfirmationView: UIViewControllerRepresentable {
    @EnvironmentObject var networkMonitor: NetworkMonitor
    @EnvironmentObject var appState: AppState
    
    func makeUIViewController(context: Context) -> AuthenticationConfirmationViewController {
        let vc = AuthenticationConfirmationViewController()
        vc.appState = appState
        vc.networkMonitor = networkMonitor
        return vc
    }
    
    func updateUIViewController(_ uiViewController: AuthenticationConfirmationViewController, context: Context) { }
}
