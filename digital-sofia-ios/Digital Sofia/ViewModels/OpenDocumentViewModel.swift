//
//  OpenDocumentViewModel.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 23.10.23.
//

import SwiftUI
import EvrotrustSDK

enum OpenDocumentCheckUserStatusType {
    case showDocument, showEditProfile, showSetupSDK
}

class OpenDocumentViewModel: NSObject {
    var openDocumentUserDecision: (EvrotrustUserDecision?) -> ()
    var openDocumentErrorHandler: (String) -> ()
    var checkUserStatusResult: (OpenDocumentCheckUserStatusType) -> ()
    var successfullySignedDocument: () -> ()
    
    init(openDocumentUserDecision: @escaping (EvrotrustUserDecision?) -> Void,
         openDocumentErrorHandler: @escaping (String) -> Void,
         checkUserStatusResult: @escaping (OpenDocumentCheckUserStatusType) -> Void,
         successfullySignedDocument: @escaping () -> Void) {
        self.openDocumentUserDecision = openDocumentUserDecision
        self.openDocumentErrorHandler = openDocumentErrorHandler
        self.checkUserStatusResult = checkUserStatusResult
        self.successfullySignedDocument = successfullySignedDocument
    }
    
    func checkUserStatus() {
        Evrotrust.sdk()?.checkUserStatus(with: self)
    }
    
    func openDocument(transactionId: String?) -> some View {
        return EvrotrustOpenDocumentView(transactionId: transactionId) { [weak self] decision, error in
            if let error = error {
                self?.openDocumentErrorHandler(error.description)
            } else {
                self?.openDocumentUserDecision(decision)
            }
        }
    }
    
    func openETSetup() -> some View {
        return EvrotrustSetupView { [weak self] _, error in
            if let error = error {
                self?.openDocumentErrorHandler(error.description)
            }
        }
    }
    
    func openEditProfile() -> some View {
        return EvrotrustEditAndIdentifyView { [weak self] _, error in
            if let error = error {
                self?.openDocumentErrorHandler(error.description)
            }
        }
    }
    
    func verifyDocument(transactionId: String?) {
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            NetworkManager.verifyTransaction(id: transactionId ?? "") { [weak self] response in
                switch response {
                case .success(_):
                    self?.successfullySignedDocument()
                case .failure(let error):
                    if let networkError = error as? NetworkError {
                        self?.openDocumentErrorHandler(networkError.description)
                    }
                }
            }
        }
    }
    
    func sendDocumentStatus(transactionId: String?) {
        NetworkManager.sendDocumentStatus(transactionId: transactionId ?? "") { [weak self] response in
            switch response {
            case .success(_):
                self?.successfullySignedDocument()
            case .failure(let error):
                if let networkError = error as? NetworkError {
                    self?.openDocumentErrorHandler(networkError.description)
                }
            }
        }
    }
}

extension OpenDocumentViewModel: EvrotrustCheckUserStatusDelegate {
    func evrotrustCheckUserStatusDelegateDidFinish(_ result: EvrotrustCheckUserStatusResult!) {
        switch (result.status) {
        case EvrotrustResultStatus.sdkNotSetUp:
            openDocumentErrorHandler(EvrotrustError.sdkNotSetUp.description)
            
        case EvrotrustResultStatus.userNotSetUp:
            checkUserStatusResult(.showSetupSDK)
            
        case EvrotrustResultStatus.OK:
            if result.successfulCheck {
                if result.identified == false {
                    checkUserStatusResult(.showEditProfile)
                } else {
                    checkUserStatusResult(.showDocument)
                }
            }
            
        default: break
        }
    }
}
