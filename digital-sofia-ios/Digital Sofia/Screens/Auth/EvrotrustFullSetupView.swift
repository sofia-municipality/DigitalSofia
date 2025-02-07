//
//  EvrotrustFullSetupView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 22.01.24.
//

import SwiftUI

struct EvrotrustFullSetupView: View {
    @Environment(\.dismiss) var dismiss
    @EnvironmentObject var appState: AppState
    
    var shouldAddUserInformation: Bool = false
    var completion: EvrotrustViewCompletion?
    
    @State private var showEditUserView = false
    
    var body: some View {
        VStack(spacing: 0) {
            navigation()
            
            EvrotrustSetupView(shouldAddUserInformation: shouldAddUserInformation, completion: { success, error in
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                    if let error = error {
                        switch error {
                        case .editUser:
                            showEditUserView = true
                        case .userNotReadyToSign:
                            completion?(false, error)
                            dismiss()
                        default:
                            completion?(false, error)
                            dismiss()
                        }
                    } else {
                        completion?(success, nil)
                        dismiss()
                    }
                }
            })
        }
        .log(view: self)
    }
    
    private func navigation() -> some View {
        VStack {
            NavigationLink(destination: EvrotrustEditAndIdentifyView(completion: { success, error in
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                    completion?(success, error)
                    showEditUserView = false
                    dismiss()
                }
            })
                .ignoresSafeArea(),
                           isActive: $showEditUserView) { EmptyView() }
        }
    }
}

#Preview {
    EvrotrustFullSetupView()
}
