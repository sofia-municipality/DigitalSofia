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
    @EnvironmentObject var identityConfig: IdentityRequestConfig
    
    var completion: EvrotrustViewCompletion?
    var shouldAddUserInformation: Bool
    
    fileprivate var setupVC = Evrotrust.sdk()?.createEvrotrustSetupViewController()
    fileprivate var user: User? {
        return UserProvider.currentUser
    }
    
    init(shouldAddUserInformation: Bool?, completion: EvrotrustViewCompletion?) {
        self.completion = completion
        self.shouldAddUserInformation = shouldAddUserInformation ?? false
    }
    
    func makeUIViewController(context: Context) -> some UIViewController {
        let securityContext = user?.securityContext ?? user?.pin?.getHashedPassword
        let personalIdentificationNumber = user?.personalIdentificationNumber ?? ""
        
        if let setupVC = setupVC {
            setupVC.isActingAsRegistrationAuthority = false
            setupVC.shouldSkipContactInformation = true
            setupVC.securityContext = securityContext == nil ? identityConfig.newPin.getHashedPassword : securityContext
            setupVC.delegate = context.coordinator
            
            if shouldAddUserInformation {
                let userInformation: EvrotrustUserInformation = EvrotrustUserInformation()
                userInformation.userDataType = EvrotrustUserType.identificationNumber
                userInformation.userDataValue = personalIdentificationNumber
                userInformation.countryCode3 = AppConfig.Evrotrust.CountryCode3.bulgaria
                setupVC.userInformationForCheck = userInformation
            }
            
            LoggingHelper.logSDKUserSetupStart(securityContext: securityContext ?? "",
                                               personalIdentificationNumber: personalIdentificationNumber)
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
        LoggingHelper.logSDKUserSetupResult(result: result)
        switch result.status {
        case EvrotrustResultStatus.OK:
            if result.userSetUp {
                if result.personalIdentificationNumber == parent.user?.personalIdentificationNumber {
                    if result.identified == true {
                        if result.readyToSign == true {
                            UserProvider.shared.updateUserWithETInfo(result: result)
                            parent.completion?(true, nil)
                        } else {
                            parent.completion?(false, .userNotReadyToSign)
                        }
                        
                    } else {
                        parent.completion?(false, .editUser)
                    }
                } else {
                    parent.completion?(false, .errorInput)
                }
            } else {
                parent.completion?(false, .userNotSetUp)
            }
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
