//
//  ViewController+Notifications.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 5.12.23.
//

import UIKit
import EvrotrustSDK

extension ViewController {
    func subscribeToNotifications() {
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
        
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(lockScreenNotificationHandler(_:)),
                                               name: NSNotification.Name.lockScreenNotification,
                                               object: nil)
        
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(loginNotificationHandler(_:)),
                                               name: NSNotification.Name.loginFromPortalNotification,
                                               object: nil)
        
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(documentNotificationHandler(_:)),
                                               name: NSNotification.Name.pendingDocumentNotification,
                                               object: nil)
        
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(documentNotificationHandler(_:)),
                                               name: NSNotification.Name.pendingDeliveryDocumentNotification,
                                               object: nil)
        
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(documentNotificationHandler(_:)),
                                               name: NSNotification.Name.signedDocumentNotification,
                                               object: nil)
        
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(documentNotificationHandler(_:)),
                                               name: NSNotification.Name.expiredDocumentNotification,
                                               object: nil)
        
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(tokenRefreshedNotification(_:)),
                                               name: NSNotification.Name.tokenRefreshedNotification,
                                               object: nil)
        
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(evrotrustNotSetupNotificationHandler(_:)),
                                               name: NSNotification.Name.evrotrustSDKNotSetupNotification,
                                               object: nil)
    }
    
    func unsubscribeToNotifications() {
        NotificationCenter.default.removeObserver(self, name: NSNotification.Name.evrotrustSDKSetupReceivedNotification, object: nil)
        NotificationCenter.default.removeObserver(self, name: NSNotification.Name.tokenExpiredNotification, object: nil)
        NotificationCenter.default.removeObserver(self, name: NSNotification.Name.logoutUserNotification, object: nil)
        NotificationCenter.default.removeObserver(self, name: NSNotification.Name.openDocumentNotification, object: nil)
        NotificationCenter.default.removeObserver(self, name: NSNotification.Name.checkPendingDocumentsNotification, object: nil)
        NotificationCenter.default.removeObserver(self, name: NSNotification.Name.lockScreenNotification, object: nil)
        NotificationCenter.default.removeObserver(self, name: NSNotification.Name.loginFromPortalNotification, object: nil)
        NotificationCenter.default.removeObserver(self, name: NSNotification.Name.signedDocumentNotification, object: nil)
        NotificationCenter.default.removeObserver(self, name: NSNotification.Name.expiredDocumentNotification, object: nil)
        NotificationCenter.default.removeObserver(self, name: NSNotification.Name.pendingDocumentNotification, object: nil)
        NotificationCenter.default.removeObserver(self, name: NSNotification.Name.tokenRefreshedNotification, object: nil)
        NotificationCenter.default.removeObserver(self, name: NSNotification.Name.evrotrustSDKNotSetupNotification, object: nil)
    }
    
    @objc private func receiverETSDKSetupResult(_ notification: NSNotification) {
        if let result = notification.userInfo?[AppConfig.Notifications.UserInfoKeys.evrotrustSDKSetup] as? Bool {
            if result {
                setupLanguage()
                LoggingHelper.logSDKCheckUserStatusStart()
                Evrotrust.sdk()?.checkUserStatus(with: self)
            } else {
                appState.state = .error
                appState.alertItem = AlertProvider.errorAlert(message: EvrotrustError.sdkNotSetUp.description)
            }
        }
    }
    
    @objc private func tokenExpiredNotificationHandler(_ notification: NSNotification) {
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
    
    @objc private func evrotrustNotSetupNotificationHandler(_ notification: NSNotification) {
        appState.state = .wasLoggedOut
        popToSelf { [weak self] in
            self?.appState.state = .error
            self?.appState.alertItem = AlertProvider.errorAlertWithCompletion(message: EvrotrustError.sdkNotSetUp.description, completion: {
                self?.appState.state = .initial
            })
        }
    }
    
    @objc private func openDocumentNotificationHandler(_ notification: NSNotification) {
        if let id = notification.userInfo?[AppConfig.Notifications.UserInfoKeys.evrotrustTransactionId] as? String {
            showETDocumentView(transactionId: id)
        }
    }
    
    @objc private func lockScreenNotificationHandler(_ notification: NSNotification) {
        appState.shouldLockScreen = true
    }
    
    @objc private func loginNotificationHandler(_ notification: NSNotification) {
        if UserProvider.isVerified {
            if let code = notification.userInfo?[AppConfig.Notifications.payload] as? String {
                appState.loginRequestCode = code
            }
        }
    }
    
    @objc private func documentNotificationHandler(_ notification: NSNotification) {
        DocumentsNotificationHelper.setTab(notification: notification, appState: appState)
    }
    
    @objc private func checkPendingDocumentsNotification(_ notification: NSNotification) {
        checkPendingDocuments()
    }
    
    @objc private func tokenRefreshedNotification(_ notification: NSNotification) {
        if appState.tokenRefreshed {
            appState.tokenRefreshed = false
        }
        
        appState.tokenRefreshed = true
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
}
