//
//  ViewController+EvrotrustDelegate.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 5.12.23.
//

import EvrotrustSDK
import SwiftUI

extension ViewController: EvrotrustCheckUserStatusDelegate {
    func evrotrustCheckUserStatusDelegateDidFinish(_ result: EvrotrustCheckUserStatusResult!) {
        LoggingHelper.logSDKCheckUserStatusResult(result: result)
        switch (result.status) {
        case .sdkNotSetUp:
            EvrotrustError.sdkNotSetupHandler()
            
        case EvrotrustResultStatus.userNotSetUp:
            setNewUserState()
            
        case EvrotrustResultStatus.OK:
            print(result.formattedDescription)
            if result.successfulCheck {
                if result.identified == true {
                    if result.readyToSign == false {
                        if UserProvider.shouldContinueResetPasswordFlow {
                            NetworkManager.subscribeToETUserUpdates() { _ in }
                            showResetPINView(readyToSign: false)
                        } else {
                            showSdkUserAuthFailedView()
                        }
                    } else {
                        if UserProvider.shouldContinueResetPasswordFlow {
                            showResetPINView(readyToSign: true)
                        } else {
                            UserProvider.isVerified ? authenticateUser() : showConfirmDataShareView()
                        }
                    }
                } else if result.identified == false || result.rejected == true {
                    showEditETUser()
                }
            }
            
        default: break
        }
    }
}

extension ViewController: EvrotrustEditPersonalDataViewControllerDelegate {
    func evrotrustEditPersonalDataDidFinish(_ result: EvrotrustEditPersonalDataResult!) {
        switch (result.status) {
        case .sdkNotSetUp:
            EvrotrustError.sdkNotSetupHandler()
            
        case EvrotrustResultStatus.errorInput:
            appState.state = .error
            appState.alertItem = AlertProvider.errorAlert(message: EvrotrustError.errorInput.description)
            
        case EvrotrustResultStatus.userNotSetUp:
            appState.state = .error
            appState.alertItem = AlertProvider.errorAlert(message: EvrotrustError.userNotSetUp.description)
            
        case EvrotrustResultStatus.userCanceled:
            UserProvider.shared.invalidateOldUserSession()
            appState.state = .initial
            
        case EvrotrustResultStatus.OK:
            let editPersonalData: Bool = result.editPersonalData
            if (editPersonalData) {
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) { [weak  self] in
                    self?.authenticateUser()
                }
            }
        default: break
        }
    }
}
