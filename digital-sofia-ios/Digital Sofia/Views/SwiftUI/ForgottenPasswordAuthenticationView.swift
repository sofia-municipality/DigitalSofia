//
//  ForgottenPasswordAuthenticationView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 24.08.23.
//

import SwiftUI

struct ForgottenPasswordAuthenticationView: UIViewControllerRepresentable {
    @EnvironmentObject var networkMonitor: NetworkMonitor
    @EnvironmentObject var appState: AppState
    @Environment(\.presentationMode) var presentationMode
    
    func makeUIViewController(context: Context) -> ForgottenPasswordAuthenticationViewController {
        let vc = ForgottenPasswordAuthenticationViewController()
        vc.closeViewController = {
            presentationMode.wrappedValue.dismiss()
        }
        return vc
    }
    
    func updateUIViewController(_ uiViewController: ForgottenPasswordAuthenticationViewController, context: Context) { }
}
