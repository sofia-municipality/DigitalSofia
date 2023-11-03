//
//  ViewController.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 4.04.23.
//

import UIKit
import EvrotrustSDK
import SwiftUI

class ViewController: BaseViewController {
    
    var showLoginOptions = false
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        if !showLoginOptions {
            setupWhiteNavBarAppearance()
            setupNotifications()
        } else {
            showLoginScreen()
        }
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self, name: NSNotification.Name.evrotrustSDKSetupReceivedNotification, object: nil)
        NotificationCenter.default.removeObserver(self, name: NSNotification.Name.tokenExpiredNotification, object: nil)
        NotificationCenter.default.removeObserver(self, name: NSNotification.Name.logoutUserNotification, object: nil)
        NotificationCenter.default.removeObserver(self, name: NSNotification.Name.openDocumentNotification, object: nil)
        NotificationCenter.default.removeObserver(self, name: NSNotification.Name.checkPendingDocumentsNotification, object: nil)
        
#if DEBUG
        print("\(self) deallocated")
#endif
    }
    
    private func setupNotifications() {
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(receiverETSDKSetupResult(_:)),
                                               name: NSNotification.Name.evrotrustSDKSetupReceivedNotification,
                                               object: nil)
        
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(tokenExpiredNotificationHandler(_:)),
                                               name: NSNotification.Name.tokenExpiredNotification,
                                               object: nil)
        
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(logoutUserNotificationHandler(_:)),
                                               name: NSNotification.Name.logoutUserNotification,
                                               object: nil)
        
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(openDocumentNotificationHandler(_:)),
                                               name: NSNotification.Name.openDocumentNotification,
                                               object: nil)
        
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(checkPendingDocumentsNotification(_:)),
                                               name: NSNotification.Name.checkPendingDocumentsNotification,
                                               object: nil)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        showLaunchScreen()
        customizeET()
    }
    
    private func setupLanguage() {
        if LanguageProvider.shared.appLanguage == nil {
            LanguageProvider.shared.appLanguage = .bulgarian
        }
        
        appState.language = LanguageProvider.shared.appLanguage
    }
    
    @objc private func receiverETSDKSetupResult(_ notification: NSNotification) {
        if let result = notification.userInfo?[AppConfig.Notifications.UserInfoKeys.evrotrustSDKSetup] as? Bool {
            if result {
                setupLanguage()
                Evrotrust.sdk()?.checkUserStatus(with: self)
            } else {
                appState.state = .error
                appState.alertItem = AlertProvider.errorAlert(message: EvrotrustError.sdkNotSetUp.description)
            }
        }
    }
    
    @objc private func tokenExpiredNotificationHandler(_ notification: NSNotification) {
        UserProvider.shared.deleteUser()
        appState.state = .tokenExpired
        popToSelf { [weak self] in
            self?.appState.state = .error
            self?.appState.alertItem = AlertProvider.errorAlertWithCompletion(message: NetworkError.tokenExpired.description, completion: {
                self?.appState.state = .initial
            })
        }
    }
    
    @objc private func logoutUserNotificationHandler(_ notification: NSNotification) {
        appState.state = .wasLoggedOut
        popToSelf { [weak self] in
            self?.appState.state = .initial
        }
    }
    
    @objc private func openDocumentNotificationHandler(_ notification: NSNotification) {
        if let id = notification.userInfo?[AppConfig.Notifications.UserInfoKeys.evrotrustTransactionId] as? String {
            showETDocumentView(transactionId: id)
        }
    }
    
    @objc private func checkPendingDocumentsNotification(_ notification: NSNotification) {
        checkPendingDocuments()
    }
    
    private func popToSelf(completion: @escaping () -> ()) {
        for view in view.subviews {
            if view.className.contains(String(describing: LaunchScreen.self)) == false {
                view.removeFromSuperview()
            }
        }
        
        if let vc = initialVC {
            navigationController?.popToViewController(viewController: vc, animated: true, completion: {
                completion()
            })
        }
    }
    
    private func setupWhiteNavBarAppearance() {
        let appearance = UINavigationBarAppearance()
        appearance.configureWithOpaqueBackground()
        appearance.backgroundColor = .white
        appearance.shadowColor = .clear
        navigationController?.navigationBar.standardAppearance = appearance
        navigationController?.navigationBar.scrollEdgeAppearance = navigationController?.navigationBar.standardAppearance
    }
    
    private func authenticateUser() {
        if let user = user {
            if user.token == nil {
                appState.state = .initial
            } else {
                setLoginState(user: user)
            }
        }
    }
    
    private func setLoginState(user: User) {
        if user.verified {
            checkPendingDocuments()
            showLoginScreen(user: user)
        } else {
            checkPendingDocuments { [weak self] transactionId, error in
                if let id = transactionId {
                    self?.showETDocumentView(transactionId: id) { decision, error in
                        if let error = error {
                            self?.showNewUserStateAlert(with: error.description)
                        } else {
                            switch decision {
                            case .approved:
                                NetworkManager.verifyTransaction(id: id) { response in
                                    switch response {
                                    case .success(_):
                                        self?.showLoginScreen(user: user)
                                    case .failure(let error):
                                        if let _ = error as? NetworkError {
                                            self?.showNewUserStateAlert(with: AppConfig.UI.Text.authenticationErrorText.localized)
                                        }
                                    }
                                }
                            default:
                                self?.appState.alertItem = AlertProvider.errorAlert(message: AppConfig.UI.Alert.shareDataDocDeclineAlertText.localized)
                            }
                        }
                    }
                } else if let _ = error {
                    self?.appState.alertItem = AlertProvider.errorAlert(message: AppConfig.UI.Text.authenticationErrorText.localized)
                }
            }
        }
    }
    
    private func customizeET() {
        let customization: EvrotrustCustomization = EvrotrustCustomization()
        customization.mainColor1 = DSColors.ETCustomColors.evrotrustMainColor1.uiColor
        customization.mainColor2 = DSColors.ETCustomColors.evrotrustMainColor2.uiColor
        customization.mainColor3 = DSColors.ETCustomColors.evrotrustMainColor3.uiColor
        customization.backgroundColor1 = DSColors.ETCustomColors.evrotrustBackgroundColor1.uiColor
        customization.backgroundColor2 = DSColors.ETCustomColors.evrotrustBackgroundColor2.uiColor
        customization.backgroundColor3 = DSColors.ETCustomColors.evrotrustBackgroundColor3.uiColor
        customization.textColor1 = DSColors.ETCustomColors.evrotrustTextColor1.uiColor
        customization.textColor2 = DSColors.ETCustomColors.evrotrustTextColor2.uiColor
        customization.hintTextColor = DSColors.ETCustomColors.evrotrustHintTextColor.uiColor
        
        
        if let logo = UIImage(named: ImageProvider.logo),
           let _ = UIImage(named: ImageProvider.loader),
           let _ = UIImage(named: ImageProvider.person),
           let _ = UIImage(named: ImageProvider.statusOK) {
            customization.imageCustomizations.questionsTitleImage = logo
            customization.imageCustomizations.contactsTitleImage = logo
            customization.imageCustomizations.documentsTitleImage = logo
            customization.imageCustomizations.scanInstructionsImage = logo
        }
        
        customization.setCustomFont("SofiaSans-Regular", withFileName: "SofiaSans-Regular", andFileExtension: "ttf")
        Evrotrust.sdk()?.setCustomization(customization)
    }
    
    private func showEditETUser() {
        if let viewController = getEditETUserViewController() {
            viewController.editPersonalDataDelegate = self
            navigationController?.pushViewController(viewController, animated:true)
        }
    }
    
    private func showNewUserStateAlert(with message: String) {
        appState.alertItem = AlertProvider.errorAlertWithCompletion(message: message, completion: { [weak self] in
            self?.setNewUserState()
        })
    }
    
    private func setNewUserState() {
        UserProvider.shared.deleteUser()
        appState.state = .initial
    }
}

extension ViewController: EvrotrustCheckUserStatusDelegate {
    func evrotrustCheckUserStatusDelegateDidFinish(_ result: EvrotrustCheckUserStatusResult!) {
        switch (result.status) {
        case EvrotrustResultStatus.sdkNotSetUp:
            appState.state = .error
            appState.alertItem = AlertProvider.errorAlert(message: EvrotrustError.sdkNotSetUp.description)
            
        case EvrotrustResultStatus.userNotSetUp:
            setNewUserState()
            
        case EvrotrustResultStatus.OK:
            if result.successfulCheck {
                if result.identified == false {
                    showEditETUser()
                } else {
                    authenticateUser()
                }
            }
            
        default: break
        }
    }
}

extension ViewController: EvrotrustEditPersonalDataViewControllerDelegate {
    func evrotrustEditPersonalDataDidFinish(_ result: EvrotrustEditPersonalDataResult!) {
        switch (result.status) {
        case EvrotrustResultStatus.sdkNotSetUp:
            appState.state = .error
            appState.alertItem = AlertProvider.errorAlert(message: EvrotrustError.sdkNotSetUp.description)
            
        case EvrotrustResultStatus.errorInput:
            appState.state = .error
            appState.alertItem = AlertProvider.errorAlert(message: EvrotrustError.errorInput.description)
            
        case EvrotrustResultStatus.userNotSetUp:
            appState.state = .error
            appState.alertItem = AlertProvider.errorAlert(message: EvrotrustError.userNotSetUp.description)
            
        case EvrotrustResultStatus.userCanceled:
            UserProvider.shared.deleteUser()
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
