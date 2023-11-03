//
//  ViewController.swift
//  Digital Sofia
//
//  Created by Mehmed Kadir on 18.01.23.
//

import UIKit
import EvrotrustSDK

class TestViewController: UIViewController {
    
    private var securityContext: String = ""
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let language = Languages.bulgarian
        LanguageProvider.shared.appLanguage = language
        Evrotrust.sdk()?.setLanguage(language.rawValue)
    }
    
    @IBAction func settingsVC(_ sender: Any) {
        let viewController: EvrotrustSettingsViewController = (Evrotrust.sdk()?.createEvrotrustSettingsViewController())!
        viewController.securityContext = securityContext
        navigationController?.pushViewController(viewController, animated: true)
    }
    
    // MARK: Start
    @IBAction func setupUser(_ sender: Any) {
        print(Evrotrust.sdk()?.sdkSetUp() ?? false)
        if let viewController: EvrotrustSetupViewController = (Evrotrust.sdk()?.createEvrotrustSetupViewController()) {
            viewController.delegate = self
            viewController.isActingAsRegistrationAuthority = false // always has to be false
            //   viewController.securityContext = "fa48ea3449d31b384fe77194f773bcccd0daa0d6dad60942353182cab0654a3e8dce95b9da57e51caeef9f0e0d75dd90bf975b2915af1b7a38b04efb4714604b" // optional parameter
            viewController.shouldSkipContactInformation = true // optional parameter
            
            let userInformation: EvrotrustUserInformation = EvrotrustUserInformation()
            userInformation.userDataType = EvrotrustUserType.identificationNumber
            userInformation.userDataValue = ""
            userInformation.countryCode3 = "BGR" // country code by ISO 3166
            viewController.userInformationForCheck = userInformation // optional parameter
            
            navigationController?.pushViewController(viewController, animated:true)
        } else {
            fatalError("could not set up a view")
        }
    }
    
    @IBAction func checkStatus(_ sender: Any) {
        Evrotrust.sdk()?.checkUserStatus(with: self)
    }
    
    @IBAction func editProfileAction(_ sender: Any) {
        let viewController:EvrotrustEditAndIdentifyViewController = (Evrotrust.sdk()?.createEvrotrustEditAndIdentifyViewController())!
        viewController.editPersonalDataDelegate = self
        viewController.securityContext = securityContext
        self.navigationController?.pushViewController(viewController, animated:true)
    }
    
    
    //MARK: Open Document
    @IBAction func openDocumentAction(_ sender: Any) {
        
        let viewController1: EvrotrustOpenDocumentViewController = (Evrotrust.sdk()?.createEvrotrustOpenDocumentViewController())!
        
        viewController1.delegate = self
        viewController1.securityContext = securityContext
        viewController1.transactionID = ""
        viewController1.isSingleDocument = true
        
        navigationController?.pushViewController(viewController1, animated:true)
    }
}

extension TestViewController: EvrotrustSetupViewControllerDelegate {
    func evrotrustSetupDidFinish(_ result: EvrotrustSetupProfileResult!) {
        switch result.status {
        case EvrotrustResultStatus.sdkNotSetUp:
            break
        case EvrotrustResultStatus.errorInput:
            break
        case EvrotrustResultStatus.userCanceled:
            break;
        case EvrotrustResultStatus.OK:
            if result.userSetUp {
                securityContext = result.securityContext
                let pinCode: String = result.pinCode
                let personalIdentificationNumber: String = result.personalIdentificationNumber
                let countryCode2: String = result.countryCode2 // 2 letter country code by ISO 3166 let countryCode3: String = result.countryCode3 // 3 letter country code by ISO 3166 let phone: String = result.phone
                let fisrtName: String = result.firstName
                let middleName: String = result.middleName
                let lastName: String = result.lastName
                let firstLatinName: String = result.firstLatinName
                let middleLatinName: String = result.middleLatinName
                let lastLatinName: String = result.lastLatinName
                let isIdentified: Bool = result.identified
                let isSupervised: Bool = result.supervised
                let isReadyToSign: Bool = result.readyToSign
                let isRejected: Bool = result.rejected
                
                
                print(securityContext)
                print(pinCode)
                print(personalIdentificationNumber)
                print(countryCode2)
                print(fisrtName)
                print(lastName)
                print(firstLatinName)
                print(middleName)
                print(lastLatinName)
                print(middleLatinName)
                print(isIdentified)
                print(isSupervised)
                print(isReadyToSign)
                print(isRejected)
            }
            break default:
            break
        }
    }
}

extension TestViewController: EvrotrustOpenDocumentViewControllerDelegate {
    
    func evrotrustOpenSingleDocumentDidFinish(_ result: EvrotrustOpenDocumentResult!) { }
    
    func evrotrustOpenGroupDocumentsDidFinish(_ result: EvrotrustOpenDocumentResult!) {
        switch (result.status) {
        case EvrotrustResultStatus.sdkNotSetUp:
            print("sdkNotSetUp")
        case EvrotrustResultStatus.errorInput:
            print("errorInput")
        case EvrotrustResultStatus.userCanceled:
            print("userCanceled")
        case EvrotrustResultStatus.userNotSetUp:
            print("userNotSetUP")
        case EvrotrustResultStatus.OK:
            let userDecision: EvrotrustUserDecision = result.userDecision // Approved, Rejected, No Choice break
            print(userDecision)
        default:
            break
        }
    }
}

extension TestViewController: EvrotrustCheckUserStatusDelegate {
    func evrotrustCheckUserStatusDelegateDidFinish(_ result: EvrotrustCheckUserStatusResult!) {
        switch (result.status) {
        case EvrotrustResultStatus.sdkNotSetUp:
            print("sdkNotSetUp")
        case EvrotrustResultStatus.userNotSetUp:
            print("userNotSetUp")
        case EvrotrustResultStatus.OK:
            print("status is OK")
            let successfulCheck: Bool = result.successfulCheck
            if (successfulCheck) {
                print("identified \(result.identified)")
                print("supervised \(result.supervised)")
                print("readyToSign \(result.readyToSign)")
                print("rejected \(result.rejected)")
                print("confirmedPhone \(result.confirmedPhone)")
                print("confirmedEmail \(result.confirmedEmail)")
            }
        default:
            break
        }
    }
}

extension TestViewController:EvrotrustEditPersonalDataViewControllerDelegate, EvrotrustSettingsViewControllerDelegate, EvrotrustUserSetUpDelegate, EvrotrustUserSetUpOnlineDelegate {
    
    func evrotrustEditPersonalDataDidFinish(_ result: EvrotrustEditPersonalDataResult!) {
        switch (result.status) {
        case EvrotrustResultStatus.sdkNotSetUp:
            break
        case EvrotrustResultStatus.errorInput:
            break
        case EvrotrustResultStatus.userNotSetUp:
            break
        case EvrotrustResultStatus.userCanceled:
            break
        case EvrotrustResultStatus.OK:
            let editPersonalData: Bool = result.editPersonalData
            if (editPersonalData) {
                //        let identified: Bool = result.identified
                //        let supervised: Bool = result.supervised
                //        let readyToSign: Bool = result.readyToSign
                //        let rejected: Bool = result.rejected
            }
            break;
        @unknown default:
            fatalError()
        }
    }
    
    func evrotrustUserSetUpOnlineDelegateDidFinish(_ result: EvrotrustUserSetUpOnlineResult!) {
        switch (result.status) {
        case EvrotrustResultStatus.sdkNotSetUp:
            print("sdk not setUP")
        case EvrotrustResultStatus.OK:
            let successfulCheck: Bool = result.successfulCheck
            if (successfulCheck) {
                let userSetUp: Bool = result.userSetUp
                print("user setUPOnline -> \(userSetUp)")
            }
        default:
            break
        }
    }
    
    func evrotrustUserSetUpDelegateDidFinish(_ result: EvrotrustUserSetUpResult!) {
        switch (result.status) {
        case EvrotrustResultStatus.sdkNotSetUp:
            print("sdk not setUP")
        case EvrotrustResultStatus.OK:
            let userSetUp: Bool = result.userSetUp
            print("user setUP -> \(userSetUp)")
        default:
            break
        }
    }
    
    func evrotrustSettingsDidFinish(_ result: EvrotrustResult!) {
        switch (result.status) {
        case EvrotrustResultStatus.sdkNotSetUp: break
        default: break
        }
    }
}
