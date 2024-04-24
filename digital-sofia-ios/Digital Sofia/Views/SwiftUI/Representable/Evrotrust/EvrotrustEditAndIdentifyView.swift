//
//  EvrotrustEditAndIdentifyView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 23.10.23.
//

import Foundation

import SwiftUI
import UniformTypeIdentifiers
import EvrotrustSDK

struct EvrotrustEditAndIdentifyView: UIViewControllerRepresentable {
    var completion: EvrotrustViewCompletion?
    
    fileprivate var editVC = Evrotrust.sdk()?.createEvrotrustEditAndIdentifyViewController()
    fileprivate var user: User? {
        return UserProvider.currentUser
    }
    
    init(completion: EvrotrustViewCompletion?) {
        self.completion = completion
    }
    
    func makeUIViewController(context: Context) -> some UIViewController {
        if let editVC = editVC {
            let securityContext = user?.securityContext ?? ""
            editVC.securityContext = securityContext
            editVC.editPersonalDataDelegate = context.coordinator
            LoggingHelper.logSDKEditUserStart(securityContext: securityContext)
            
            return editVC as UIViewController
        }
        
        return UIViewController()
    }
    
    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) { }
    
    func makeCoordinator() -> EvrotrustEditAndIdentifyViewCoordinator {
        EvrotrustEditAndIdentifyViewCoordinator(self)
    }
}

final class EvrotrustEditAndIdentifyViewCoordinator: NSObject, EvrotrustEditPersonalDataViewControllerDelegate {
    
    var parent: EvrotrustEditAndIdentifyView
    
    init(_ parent: EvrotrustEditAndIdentifyView) {
        self.parent = parent
    }
    
    func evrotrustEditPersonalDataDidFinish(_ result: EvrotrustEditPersonalDataResult!) {
        LoggingHelper.logSDKEditUserResult(result: result)
        switch result.status {
        case EvrotrustResultStatus.OK:
            parent.completion?(true, nil)
        case .errorInput:
            parent.completion?(false, .errorInput)
        case .userCanceled:
            parent.completion?(false, .userCancelled)
        case .userNotSetUp:
            parent.completion?(false, .userNotSetUp)
        case .sdkNotSetUp:
            EvrotrustError.sdkNotSetupHandler()
        default: break
        }
    }
}
