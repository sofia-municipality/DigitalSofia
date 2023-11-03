//
//  AuthenticationConfirmationViewController.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 28.04.23.
//

import UIKit
import SwiftUI
import EvrotrustSDK

class AuthenticationConfirmationViewController: BaseAuthenticationViewController {
    
    private enum AuthenticationVCType: Equatable {
        case indentification, declineIndentification, error(description: String)
    }
    
    private var controllerType: AuthenticationVCType = .indentification {
        didSet {
            switch controllerType {
            case .error(let description):
                setupErrorImageView()
                detailLabel?.text = description
                noButton?.isHidden = true
                
            case .declineIndentification:
                setupErrorImageView()
                detailLabel?.text = AppConfig.UI.Text.authenticationRejectDetailsText.localized
                noButton?.isHidden = false
                setButtonTitle(button: noButton, title: AppConfig.UI.Titles.Button.back.localized)
                
            default: break
            }
            
            setButtonTitle(button: yesButton, title: AppConfig.UI.Titles.Screens.authentication.localized)
            view.setNeedsLayout()
        }
    }
    
    private var transactionId = ""
    
    override func viewDidLoad() {
        super.viewDidLoad()
    }
    
    deinit {
#if DEBUG
        print("\(self) deallocated")
#endif
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        let padding = AppConfig.Dimensions.Padding.XXXL
        
        titleLabel?.frame = CGRect(x: 0, y: padding, width: UIScreen.main.bounds.width * 0.8, height: AppConfig.Dimensions.Standart.buttonHeight)
        titleLabel?.center.x = view.center.x
        
        var detailsLabelOriginY = (titleLabel?.frame.maxY ?? 0) + padding * 2
        
        if controllerType != .indentification {
            let errorImageSize = AppConfig.Dimensions.Standart.iconHeight * 2
            errorImageView?.frame = CGRect(origin: .zero, size: CGSize(width: errorImageSize, height: errorImageSize))
            errorImageView?.center.x = view.center.x
            errorImageView?.frame.origin.y = detailsLabelOriginY
            
            detailsLabelOriginY = (errorImageView?.frame.maxY ?? 0) + padding * 2
        }
        
        detailLabel?.frame = CGRect(origin: .zero, size: CGSize(width: UIScreen.main.bounds.width - padding * 2, height: UIScreen.main.bounds.height * 0.2))
        detailLabel?.center.x = view.center.x
        detailLabel?.frame.origin.y = detailsLabelOriginY
        
        let buttonYOrigin = view.frame.maxY - AppConfig.Dimensions.Standart.buttonHeight * 2
        
        switch controllerType {
        case .error(_):
            yesButton?.frame = CGRect(origin: .zero, size: yesButton?.intrinsicContentSize ?? .zero)
            yesButton?.center.x = view.center.x
            yesButton?.frame.origin.y = buttonYOrigin
        default:
            let innerButtonPadding = AppConfig.Dimensions.Padding.XXL
            let buttonsWidth = (noButton?.intrinsicContentSize.width ?? 0) + (yesButton?.intrinsicContentSize.width ?? 0)
            let maxPaddingWidth = UIScreen.main.bounds.width - (buttonsWidth + innerButtonPadding)
            let padding = maxPaddingWidth / 2
            
            noButton?.frame = CGRect(origin: CGPoint(x: padding, y: buttonYOrigin), size: noButton?.intrinsicContentSize ?? .zero)
            yesButton?.frame = CGRect(origin: CGPoint(x: (noButton?.frame.maxX ?? 0) + innerButtonPadding, y: noButton?.frame.origin.y ?? 0),
                                      size: yesButton?.intrinsicContentSize ?? .zero)
        }
    }
    
    @objc override func noButtonClicked(_ sender: UIButton) {
        if controllerType == .declineIndentification {
            if let controllers = navigationController?.viewControllers {
                for controller in controllers {
                    if controller.isKind(of: ViewController.self) {
                        (controller as? ViewController)?.showLoginOptions = true
                        navigationController?.popToViewController(controller, animated: true)
                    }
                }
            }
        } else {
            controllerType = .declineIndentification
        }
    }
    
    @objc override func yesButtonClicked(_ sender: UIButton) {
        if let setupVC = getETSetupViewController(isFromForgottenPasswordFlow: false) {
            setupVC.delegate = self
            navigationController?.pushViewController(setupVC, animated: true)
        }
    }
    
    private func showConfirmDataShareScreen() {
        register { [weak self] errorDescription in
            if let error = errorDescription {
                self?.controllerType = .error(description: AppConfig.UI.Text.authenticationErrorText.localized)
            } else {
                self?.presentDataShareView()
            }
        }
    }
    
    private func presentDataShareView() {
        let dataShareView = ConfirmDataShareView()
            .environmentObject(networkMonitor)
            .environmentObject(appState)
        
        let hostingController = UIHostingController(rootView: dataShareView)
        navigationController?.pushViewController(hostingController, animated: true)
    }
    
    private func register(completion: @escaping (String?) -> ()) {
        loadingView?.isHidden = false
        if let user = UserProvider.shared.getUser(), let fcmToken = appDelegate?.fcmToken {
            NetworkManager.registerUser(user: user, fcmToken: fcmToken) { [weak self] response in
                self?.loadingView?.isHidden = true
                switch response {
                case .failure(let error):
                    if let networkError = error as? NetworkError {
                        completion(networkError.description)
                    }
                case .success(_):
                    completion(nil)
                }
            }
        }
    }
    
    private func showEditETUser() {
        if let viewController = getEditETUserViewController() {
            viewController.editPersonalDataDelegate = self
            navigationController?.pushViewController(viewController, animated:true)
        }
    }
}

extension AuthenticationConfirmationViewController: EvrotrustSetupViewControllerDelegate {
    func evrotrustSetupDidFinish(_ result: EvrotrustSetupProfileResult!) {
        switch result.status {
        case EvrotrustResultStatus.sdkNotSetUp:
            controllerType = .error(description: EvrotrustError.sdkNotSetUp.description)
            
        case EvrotrustResultStatus.errorInput:
            controllerType = .error(description: EvrotrustError.errorInput.description)
            
        case EvrotrustResultStatus.userCanceled:
            controllerType = .declineIndentification
            
        case EvrotrustResultStatus.OK:
            if result.userSetUp {
                UserProvider.shared.updateUserWithETInfo(result: result)
                
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) { [weak  self] in
                    if result.rejected {
                        self?.showEditETUser()
                    } else {
                        self?.showConfirmDataShareScreen()
                    }
                }
            } else {
                controllerType = .error(description: EvrotrustError.errorInput.description)
            }
            
        default:
            controllerType = .error(description: AppConfig.UI.Text.authenticationErrorText.localized)
        }
    }
}

extension AuthenticationConfirmationViewController: EvrotrustEditPersonalDataViewControllerDelegate {
    func evrotrustEditPersonalDataDidFinish(_ result: EvrotrustEditPersonalDataResult!) {
        switch (result.status) {
        case EvrotrustResultStatus.sdkNotSetUp:
            controllerType = .error(description: EvrotrustError.sdkNotSetUp.description)
            
        case EvrotrustResultStatus.errorInput:
            controllerType = .error(description: EvrotrustError.errorInput.description)
            
        case EvrotrustResultStatus.userNotSetUp:
            controllerType = .error(description: EvrotrustError.userNotSetUp.description)
            
        case EvrotrustResultStatus.userCanceled:
            controllerType = .error(description: EvrotrustError.userCancelled.description)
            
        case EvrotrustResultStatus.OK:
            let editPersonalData: Bool = result.editPersonalData
            if (editPersonalData) {
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) { [weak  self] in
                    self?.showConfirmDataShareScreen()
                }
            }
        default:
            controllerType = .error(description: AppConfig.UI.Text.authenticationErrorText.localized)
        }
    }
}
