//
//  EvrotrustSetupView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 23.10.23.
//

import SwiftUI
import UniformTypeIdentifiers
import EvrotrustSDK

typealias EvrotrustViewCompletion = (Bool?, EvrotrustError?) -> ()

struct EvrotrustSetupView: UIViewControllerRepresentable {
    var completion: EvrotrustViewCompletion?
    
    fileprivate var setupVC = Evrotrust.sdk()?.createEvrotrustSetupViewController()
    fileprivate let user = UserProvider.shared.getUser()
    
    init(completion: EvrotrustViewCompletion?) {
        self.completion = completion
    }
    
    func makeUIViewController(context: Context) -> some UIViewController {
        if let setupVC = setupVC {
            setupVC.isActingAsRegistrationAuthority = false
            setupVC.securityContext = user?.securityContext ?? user?.pin?.getHashedPassword
            setupVC.delegate = context.coordinator
            
            let userInformation: EvrotrustUserInformation = EvrotrustUserInformation()
            userInformation.userDataType = EvrotrustUserType.identificationNumber
            userInformation.userDataValue = user?.personalIdentificationNumber
            userInformation.countryCode3 = AppConfig.Evrotrust.CountryCode3.bulgaria
            setupVC.userInformationForCheck = userInformation
            
            return setupVC as UIViewController
        }
        
        return UIViewController()
    }
    
    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) { }
    
    func makeCoordinator() -> EvrotrustSetupViewCoordinator {
        EvrotrustSetupViewCoordinator(self)
    }
}

final class EvrotrustSetupViewCoordinator: NSObject, EvrotrustSetupViewControllerDelegate {
    
    var parent: EvrotrustSetupView
    
    init(_ parent: EvrotrustSetupView) {
        self.parent = parent
    }
    
    func evrotrustSetupDidFinish(_ result: EvrotrustSetupProfileResult!) {
        switch result.status {
        case EvrotrustResultStatus.OK:
            UserProvider.shared.updateUserWithETInfo(result: result)
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
