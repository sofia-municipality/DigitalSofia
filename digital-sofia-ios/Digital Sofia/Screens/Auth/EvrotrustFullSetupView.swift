//
//  EvrotrustFullSetupView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 22.01.24.
//

import SwiftUI

struct EvrotrustFullSetupView: View {
    @Environment(\.dismiss) var dismiss
    
    var shouldAddUserInformation: Bool = false
    var completion: EvrotrustViewCompletion?
    
    @State private var showEditUserView = false
    
    var body: some View {
        VStack(spacing: 0) {
            navigation()
            
            EvrotrustSetupView(shouldAddUserInformation: shouldAddUserInformation, completion: { success, error in
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                    if let error = error {
                        switch error {
                        case .editUser:
                            showEditUserView = true
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
        NavigationLink(destination: EvrotrustEditAndIdentifyView(completion: { success, error in
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
                completion?(success, error)
                showEditUserView = false
                dismiss()
            }
        })
            .ignoresSafeArea(),
                       isActive: $showEditUserView) { EmptyView() }
    }
}

#Preview {
    EvrotrustFullSetupView()
}
