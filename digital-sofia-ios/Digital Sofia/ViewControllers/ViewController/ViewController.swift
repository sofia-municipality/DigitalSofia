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
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        setupWhiteNavBarAppearance()
        subscribeToNotifications()
        showLaunchScreen()
        customizeET()
    }
    
    deinit {
        unsubscribeToNotifications()
        print("\(self) deallocated")
    }
    
    func setupLanguage() {
        if LanguageProvider.shared.appLanguage == nil {
            LanguageProvider.shared.appLanguage = .bulgarian
        }
        
        appState.language = LanguageProvider.shared.appLanguage
    }
    
    func setNewUserState() {
        UserProvider.shared.invalidateOldUserSession()
        appState.state = .initial
    }
    
    func authenticateUser() {
        if UserProvider.hasActiveUser {
            if let user = user {
                setLoginState(user: user)
            }
        } else {
            setNewUserState()
        }
    }
    
    private func setLoginState(user: User) {
        if UserDefaults.standard.object(forKey: AppConfig.UserDefaultsKeys.forceRefreshOldToken) == nil {
            UserProvider.shared.forceRefreshToken()
            UserDefaults.standard.set(true, forKey: AppConfig.UserDefaultsKeys.forceRefreshOldToken)
        }
        
        LoggingHelper.checkUserDebugMode()
        checkPendingDocuments()
        WebtokenRefreshHelper.shared.startTimer(with: nil)
        showLoginScreen(user: user)
    }
    
    func showEditETUser() {
        if let viewController = getEditETUserViewController() {
            viewController.editPersonalDataDelegate = self
            navigationController?.pushViewController(viewController, animated:true)
        }
    }
    
    func showSdkUserAuthFailedView() {
        addSwiftUI(someView: ETSdkAuthenticationFailedView().environmentObject(appState))
    }
    
    func showResetPINView(readyToSign: Bool) {
        addSwiftUI(someView: ForgottenPIN(isResetPin: false, readyToSign: readyToSign)
            .environmentObject(appState)
            .environmentObject(networkMonitor)
            .environmentObject(IdentityRequestConfig()))
    }
    
    func showConfirmDataShareView() {
        addSwiftUI(someView: ConfirmDataShareView()
            .environmentObject(appState)
            .environmentObject(networkMonitor)
            .environmentObject(IdentityRequestConfig()))
    }
    
    private func showNewUserStateAlert(with message: String) {
        appState.alertItem = AlertProvider.errorAlertWithCompletion(message: message, completion: { [weak self] in
            self?.setNewUserState()
        })
    }
    
    private func setupWhiteNavBarAppearance() {
        let appearance = UINavigationBarAppearance()
        appearance.configureWithOpaqueBackground()
        appearance.backgroundColor = .white
        appearance.shadowColor = .clear
        navigationController?.navigationBar.standardAppearance = appearance
        navigationController?.navigationBar.scrollEdgeAppearance = navigationController?.navigationBar.standardAppearance
    }
}
