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
    var openDocumentErrorHandler: (Error) -> ()
    var checkUserStatusResult: (OpenDocumentCheckUserStatusType) -> ()
    var receivedDocumentStatus: (DocumentStatusResponse?) -> ()
    var verifyIdentityRequest: (DocumentStatusResponse?) -> ()
    var userClosedDocumentView: () -> ()
    
    init(openDocumentUserDecision: @escaping (EvrotrustUserDecision?) -> Void,
         openDocumentErrorHandler: @escaping (Error) -> Void,
         checkUserStatusResult: @escaping (OpenDocumentCheckUserStatusType) -> Void,
         receivedDocumentStatus: @escaping (DocumentStatusResponse?) -> Void,
         verifyIdentityRequest: @escaping (DocumentStatusResponse?) -> Void,
         userClosedDocumentView: @escaping () -> Void) {
        self.openDocumentUserDecision = openDocumentUserDecision
        self.openDocumentErrorHandler = openDocumentErrorHandler
        self.checkUserStatusResult = checkUserStatusResult
        self.receivedDocumentStatus = receivedDocumentStatus
        self.verifyIdentityRequest = verifyIdentityRequest
        self.userClosedDocumentView = userClosedDocumentView
    }
    
    func checkUserStatus() {
        LoggingHelper.logSDKCheckUserStatusStart()
        Evrotrust.sdk()?.checkUserStatus(with: self)
    }
    
    func openDocument(transactionId: String?) -> some View {
        return EvrotrustOpenDocumentView(transactionId: transactionId) { [weak self] decision, error in
            if let error = error {
                switch error {
                case .userCancelled:
                    self?.userClosedDocumentView()
                default:
                    self?.openDocumentErrorHandler(error)
                }
            } else {
                self?.openDocumentUserDecision(decision)
            }
        }
    }
    
    func openETSetup() -> some View {
        return EvrotrustSetupView(shouldAddUserInformation: true) { [weak self] _, error in
            if let error = error {
                self?.openDocumentErrorHandler(error)
            }
        }
    }
    
    func openEditProfile() -> some View {
        return EvrotrustEditAndIdentifyView { [weak self] _, error in
            if let error = error {
                self?.openDocumentErrorHandler(error)
            } else {
                self?.userClosedDocumentView()
            }
        }
    }
    
    func verifyIdentityRequest(transactionId: String?, type: IdentityRequestViewType) {
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.4) { [weak self] in
            self?.verifyIdentityRequest(transactionId: transactionId ?? "", type: type)
        }
    }
    
    private func verifyIdentityRequest(transactionId: String, type: IdentityRequestViewType) {
        let shouldAddContactInfo = type != .forgotPIN
        NetworkManager.authenticateIdentityRequest(evrotrustTransactionId: transactionId, shouldAddContactInfo: shouldAddContactInfo) { [weak self] response in
            switch response {
            case .success(_):
                NetworkManager.registerUser(isLogin: true) { [weak self] response in
                    switch response {
                    case .success(_):
                        self?.verifyIdentityRequest(nil)
                    case .failure(let networkError):
                        self?.openDocumentErrorHandler(networkError)
                    }
                }
            case .failure(let networkError):
                self?.openDocumentErrorHandler(networkError)
            }
        }
    }
    
    func sendDocumentStatus(transactionId: String?) {
        NetworkManager.sendDocumentStatus(transactionId: transactionId ?? "") { [weak self] response in
            switch response {
            case .success(let response):
                self?.receivedDocumentStatus(response)
            case .failure(let networkError):
                self?.openDocumentErrorHandler(networkError)
            }
        }
    }
    
    func sendReceipt(threadId: String?) {
        NetworkManager.getReceiptStatus(threadId: threadId ?? "") { [weak self] response in
            switch response {
            case .success(let response):
                self?.receivedDocumentStatus(response)
            case .failure(let networkError):
                self?.openDocumentErrorHandler(networkError)
            }
        }
    }
}

extension OpenDocumentViewModel: EvrotrustCheckUserStatusDelegate {
    func evrotrustCheckUserStatusDelegateDidFinish(_ result: EvrotrustCheckUserStatusResult!) {
        LoggingHelper.logSDKCheckUserStatusResult(result: result)
        print(result.formattedDescription)
        switch (result.status) {
        case .sdkNotSetUp:
            EvrotrustError.sdkNotSetupHandler()
            
        case EvrotrustResultStatus.userNotSetUp:
            checkUserStatusResult(.showSetupSDK)
            
        case EvrotrustResultStatus.OK:
            if result.successfulCheck {
                if result.identified == false || result.rejected == true {
                    checkUserStatusResult(.showEditProfile)
                } else {
                    checkUserStatusResult(.showDocument)
                }
            } else {
                openDocumentErrorHandler(NetworkError.message(AppConfig.UI.Alert.welcomeUserErrorTitle.localized))
            }
            
        default:
            openDocumentErrorHandler(NetworkError.message(AppConfig.UI.Alert.welcomeUserErrorTitle.localized))
        }
    }
}
