//
//  ForgottenPasswordAuthenticationViewController.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 24.08.23.
//

import UIKit
import EvrotrustSDK
import SwiftUI

class ForgottenPasswordAuthenticationViewController: BaseAuthenticationViewController {
    
    var closeViewController: (() -> ())?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        detailLabel?.text = AppConfig.UI.Text.forgottenPasswordConfirmAuthText.localized
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
        
        let detailsLabelOriginY = (titleLabel?.frame.maxY ?? 0) + padding * 2
        detailLabel?.frame = CGRect(origin: .zero, size: CGSize(width: UIScreen.main.bounds.width - padding * 2, height: UIScreen.main.bounds.height * 0.2))
        detailLabel?.center.x = view.center.x
        detailLabel?.frame.origin.y = detailsLabelOriginY
        
        let buttonYOrigin = view.frame.maxY - AppConfig.Dimensions.Standart.buttonHeight * 2
        
        let innerButtonPadding = AppConfig.Dimensions.Padding.XXL
        let buttonsWidth = (noButton?.intrinsicContentSize.width ?? 0) + (yesButton?.intrinsicContentSize.width ?? 0)
        let maxPaddingWidth = UIScreen.main.bounds.width - (buttonsWidth + innerButtonPadding)
        let buttonPadding = maxPaddingWidth / 2
        
        noButton?.frame = CGRect(origin: CGPoint(x: buttonPadding, y: buttonYOrigin), size: noButton?.intrinsicContentSize ?? .zero)
        yesButton?.frame = CGRect(origin: CGPoint(x: (noButton?.frame.maxX ?? 0) + innerButtonPadding, y: noButton?.frame.origin.y ?? 0),
                                  size: yesButton?.intrinsicContentSize ?? .zero)
    }
    
    override func yesButtonClicked(_ sender: UIButton) {
        if let setupVC = getETSetupViewController(isFromForgottenPasswordFlow: true) {
            setupVC.delegate = self
            navigationController?.pushViewController(setupVC, animated: true)
        }
    }
    
    override func noButtonClicked(_ sender: UIButton) {
        closeViewController?()
    }
    
    private func showEditETUser() {
        if let viewController = getEditETUserViewController() {
            viewController.editPersonalDataDelegate = self
            navigationController?.pushViewController(viewController, animated:true)
        }
    }
    
    private func goToChangePIN() {
        let changePINView = ChangePINView(state: PINViewState.new, shouldGoToHome: true)
            .environmentObject(networkMonitor)
            .environmentObject(appState)
        let hostingController = UIHostingController(rootView: changePINView)
        navigationController?.pushViewController(hostingController, animated: true)
    }
    
    private func closeVC() {
        logout()
        closeViewController?()
    }
}

extension ForgottenPasswordAuthenticationViewController: EvrotrustSetupViewControllerDelegate {
    func evrotrustSetupDidFinish(_ result: EvrotrustSetupProfileResult!) {
        switch result.status {
        case EvrotrustResultStatus.OK:
            if result.userSetUp {
                let user = UserProvider.shared.getUser()
                
                if user?.personalIdentificationNumber == result.personalIdentificationNumber {
                    UserProvider.shared.updateUserWithETInfo(result: result)
                    
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) { [weak  self] in
                        guard let self = self else { return }
                        if  result.identified == false {
                            self.showEditETUser()
                        } else {
                            self.goToChangePIN()
                        }
                    }
                } else {
                    appState.alertItem = AlertProvider.errorAlertWithCompletion(message: AppConfig.UI.Text.authenticationErrorText.localized, completion: { [weak self] in
                        self?.closeVC()
                    })
                }
            } else {
                closeVC()
            }
        default: closeVC()
        }
    }
}

extension ForgottenPasswordAuthenticationViewController: EvrotrustEditPersonalDataViewControllerDelegate {
    func evrotrustEditPersonalDataDidFinish(_ result: EvrotrustEditPersonalDataResult!) {
        switch (result.status) {
        case EvrotrustResultStatus.OK:
            let editPersonalData: Bool = result.editPersonalData
            if (editPersonalData) {
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) { [weak  self] in
                    self?.goToChangePIN()
                }
            }
        default: closeVC()
        }
    }
}
