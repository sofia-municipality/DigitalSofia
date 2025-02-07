//
//  BaseViewController.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 23.08.23.
//

import UIKit
import SwiftUI
import EvrotrustSDK

class BaseViewController: UIViewController {
    
    @ObservedObject var appState = AppState()
    @ObservedObject var networkMonitor = NetworkMonitor()
    
    var user: User? {
        return UserProvider.currentUser
    }
    
    var initialVC: ViewController? {
        return navigationController?.viewControllers.first as? ViewController
    }
    
    private var openDocumentHostingController: UIHostingController<EvrotrustOpenDocumentView>?
    
    override func viewDidLoad() {
        super.viewDidLoad()
    }
    
    func showLoginScreen(user: User) {
        addSwiftUI(someView: LoginScreenHelper.loginScreen
            .environmentObject(appState)
            .environmentObject(networkMonitor))
    }
    
    func showLaunchScreen() {
        addSwiftUI(someView: LaunchScreen(appState: appState).environmentObject(networkMonitor))
    }
    
    func showLoginScreen() {
        addSwiftUI(someView: InitialView(appState: appState).environmentObject(networkMonitor))
    }
    
    func showUIAlertView(message: String, completion: @escaping () -> ()) {
        let alert = UIAlertController(title: AppConfig.UI.Alert.generalAlertTitle.localized, message: message, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: AppConfig.UI.Titles.Button.ok, style: .default, handler: { _ in
            completion()
        }))
        present(alert, animated: true, completion: nil)
    }
    
    func navigateToTabbarView() {
        let tabbarView = TabbarView()
            .environmentObject(appState)
            .environmentObject(networkMonitor)
        
        let hostingController = UIHostingController(rootView: tabbarView)
        navigationController?.pushViewController(hostingController, animated: true)
    }
    
    func checkPendingDocuments() {
        let parameters = DocumentsParameters(statuses: [.signing, .delivering], cursor: nil)
        
        NetworkManager.getDocuments(parameters: parameters) { [weak self] response in
            switch response {
            case .success(let documentsResponse):
                self?.appState.hasPendingDocuments = documentsResponse.documents.count > 0
            default: break
            }
        }
    }
    
    func showETDocumentView(transactionId: String, completion: EvrotrustOpenDocumentViewCompletion? = nil) {
        let documentView = EvrotrustOpenDocumentView(transactionId: transactionId) { [weak self] decision, error in
            self?.closeETDocumentView()
            completion?(decision, error)
        }
        
        openDocumentHostingController = UIHostingController(rootView: documentView)
        if let openDocumentHostingController = openDocumentHostingController {
            navigationController?.pushViewController(openDocumentHostingController, animated: true)
        }
    }
    
    private func closeETDocumentView() {
        if let viewControllers = self.navigationController?.viewControllers {
            let filtered = viewControllers.filter { vc in
                let vcClassName = String(describing: vc.self)
                return vcClassName.contains("EvrotrustDocumentDetailsViewController") == false && vcClassName.contains("EvrotrustOpenDocumentView") == false
            }
            
            navigationController?.setViewControllers(filtered, animated: true)
        }
    }
    
    func getEditETUserViewController() -> EvrotrustEditAndIdentifyViewController? {
        if let viewController: EvrotrustEditAndIdentifyViewController = (Evrotrust.sdk()?.createEvrotrustEditAndIdentifyViewController()) {
            viewController.securityContext = user?.securityContext
            return viewController
        }
        
        return nil
    }
}
