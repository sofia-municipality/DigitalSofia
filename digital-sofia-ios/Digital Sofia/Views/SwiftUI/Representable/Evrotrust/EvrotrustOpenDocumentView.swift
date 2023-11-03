//
//  EvrotrustOpenDocumentView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 4.08.23.
//

import SwiftUI
import UniformTypeIdentifiers
import EvrotrustSDK

typealias EvrotrustOpenDocumentViewCompletion = (EvrotrustUserDecision?, EvrotrustError?) -> () 

struct EvrotrustOpenDocumentView: UIViewControllerRepresentable {
    var transactionId: String?
    var completion: EvrotrustOpenDocumentViewCompletion?
    
    fileprivate var docVC = Evrotrust.sdk()?.createEvrotrustOpenDocumentViewController()
    
    init(transactionId: String?, completion: EvrotrustOpenDocumentViewCompletion?) {
        self.transactionId = transactionId
        self.completion = completion
    }
    
    func makeUIViewController(context: Context) -> some UIViewController {
        let user = UserProvider.shared.getUser()
        
        if let docVC = docVC {
            docVC.delegate = context.coordinator
            docVC.securityContext = user?.securityContext
            docVC.transactionID = transactionId
            docVC.isSingleDocument = true
            
            return docVC as UIViewController
        }
        
        return UIViewController()
    }
    
    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) { }
    
    func makeCoordinator() -> EvrotrustOpenDocumentViewCoordinator {
        EvrotrustOpenDocumentViewCoordinator(self)
    }
}

final class EvrotrustOpenDocumentViewCoordinator: NSObject, EvrotrustOpenDocumentViewControllerDelegate {
    
    var parent: EvrotrustOpenDocumentView
    
    init(_ parent: EvrotrustOpenDocumentView) {
        self.parent = parent
    }
    
    func evrotrustOpenSingleDocumentDidFinish(_ result: EvrotrustOpenDocumentResult!) {
        switch result.status {
        case .OK:
            parent.completion?(result.userDecision, nil)
        case .errorInput:
            parent.completion?(nil, EvrotrustError.errorInput)
        case .userCanceled:
            parent.completion?(nil, EvrotrustError.userCancelled)
        case .userNotSetUp:
            parent.completion?(nil, EvrotrustError.userNotSetUp)
        case .sdkNotSetUp:
            parent.completion?(nil, EvrotrustError.sdkNotSetUp)
        default: break
        }
    }
    
    func evrotrustOpenGroupDocumentsDidFinish(_ result: EvrotrustOpenDocumentResult!) { }
}
