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
    
    let user = UserProvider.shared.getUser()
    
    var initialVC: ViewController? {
        return navigationController?.viewControllers.first as? ViewController
    }
    
    private var openDocumentHostingController: UIHostingController<EvrotrustOpenDocumentView>?
    
    override func viewDidLoad() {
        super.viewDidLoad()
    }
    
    func showLoginScreen(user: User) {
        if user.useBiometrics {
            addSwiftUI(someView: LoginWithBiometricsView()
                .environmentObject(appState)
                .environmentObject(networkMonitor))
        } else {
            addSwiftUI(someView: LoginPINView()
                .environmentObject(appState)
                .environmentObject(networkMonitor))
        }
    }
    
    func showLaunchScreen() {
        addSwiftUI(someView: LaunchScreen(appState: appState).environmentObject(networkMonitor))
    }
    
    func showHomeScreen() {
        addSwiftUI(someView: TabbarView(appState: appState).environmentObject(networkMonitor))
    }
    
    func showLoginScreen() {
        addSwiftUI(someView: InitialView(appState: appState).environmentObject(networkMonitor))
    }
    
    func navigateToTabbarView() {
        let tabbarView = TabbarView(appState: appState).environmentObject(networkMonitor)
        let hostingController = UIHostingController(rootView: tabbarView)
        navigationController?.pushViewController(hostingController, animated: true)
    }
    
    func checkPendingDocuments(completion: ((String?, NetworkError?) -> ())? = nil) {
        let parameters = DocumentsParameters(statuses: [.signing], cursor: nil)
        
        NetworkManager.getDocuments(parameters: parameters) { [weak self] response in
            switch response {
            case .success(let documentsResponse):
                self?.appState.hasPendingDocuments = documentsResponse.documents.count > 0
                completion?(documentsResponse.documents.first?.evrotrustTransactionId, nil)
            case .failure(let error):
                if let networkError = error as? NetworkError {
                    completion?(nil, networkError)
                }
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
    
    func logout() {
        UserProvider.shared.logout()
    }
    
    func getEditETUserViewController() -> EvrotrustEditAndIdentifyViewController? {
        if let viewController: EvrotrustEditAndIdentifyViewController = (Evrotrust.sdk()?.createEvrotrustEditAndIdentifyViewController()) {
            viewController.securityContext = user?.securityContext
            return viewController
        }
        
        return nil
    }
}
