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
    fileprivate let user = UserProvider.shared.getUser()
    
    init(completion: EvrotrustViewCompletion?) {
        self.completion = completion
    }
    
    func makeUIViewController(context: Context) -> some UIViewController {
        if let editVC = editVC {
            editVC.securityContext = user?.securityContext
            editVC.editPersonalDataDelegate = context.coordinator
            
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
        switch result.status {
        case EvrotrustResultStatus.OK:
            parent.completion?(true, nil)
        case .errorInput:
            parent.completion?(false, EvrotrustError.errorInput)
        case .userCanceled:
            parent.completion?(false, EvrotrustError.userCancelled)
        case .userNotSetUp:
            parent.completion?(false, EvrotrustError.userNotSetUp)
        case .sdkNotSetUp:
            parent.completion?(false, EvrotrustError.sdkNotSetUp)
        default: break
        }
    }
}
