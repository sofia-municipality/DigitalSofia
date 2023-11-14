//
//  BaseAuthenticationViewController.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 24.08.23.
//

import UIKit
import SwiftUI
import EvrotrustSDK

class BaseAuthenticationViewController: BaseViewController {
    
    private var buttonFont = DSFonts.getCustomUIFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.medium)
    
    var titleLabel: UILabel? {
        didSet {
            if let label = titleLabel {
                label.text = AppConfig.UI.Titles.Screens.authentication.localized
                label.numberOfLines = 1
                label.textAlignment = .center
                label.font = DSFonts.getCustomUIFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.regular, size: DSFonts.FontSize.XXXL)
                view.addSubview(label)
            }
        }
    }
    
    var errorImageView: UIImageView? {
        didSet {
            if let imageView = errorImageView {
                imageView.image = UIImage(named: ImageProvider.statusError)
                view.addSubview(imageView)
            }
        }
    }
    
    var detailLabel: UILabel? {
        didSet {
            if let label = detailLabel {
                label.text = AppConfig.UI.Text.authenticationConfirmDetailsText.localized
                label.numberOfLines = 0
                label.textAlignment = .center
                label.font = DSFonts.getCustomUIFont(family: DSFonts.FontFamily.sofiaSans, weight: DSFonts.FontWeight.light, size: DSFonts.FontSize.XL)
                view.addSubview(label)
            }
        }
    }
    
    var noButton: AdaptableSizeButton? {
        didSet {
            if let button = noButton {
                button.configuration = getButtonConfiguration(title: AppConfig.UI.Titles.Button.decline.localized,
                                                              titleColor: DSColors.indigo.uiColor)
                button.addTarget(self, action: #selector(noButtonClicked), for: .touchUpInside)
                view.addSubview(button)
            }
        }
    }
    
    var yesButton: AdaptableSizeButton? {
        didSet {
            if let button = yesButton {
                button.configuration = getButtonConfiguration(title: AppConfig.UI.Titles.Button.accept.localized,
                                                              titleColor: .white,
                                                              backgroundColor: DSColors.Blue.blue6.uiColor,
                                                              cornerRadius: AppConfig.Dimensions.CornerRadius.mini)
                button.addTarget(self, action: #selector(yesButtonClicked), for: .touchUpInside)
                view.addSubview(button)
            }
        }
    }
    
    var loadingView: LottieLoadingUIView? {
        didSet {
            if let loadingView = loadingView {
                loadingView.isHidden = true
                view.addSubview(loadingView)
            }
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        setupUI()
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
    }
    
    func setButtonTitle(button: UIButton?, title: String) {
        button?.configuration?.attributedTitle = AttributedString(title, attributes: AttributeContainer([NSAttributedString.Key.font:buttonFont]))
    }
    
    func setupErrorImageView() {
        if errorImageView == nil {
            errorImageView = UIImageView()
        }
    }
    
    @objc func noButtonClicked(_ sender: UIButton) { }
    @objc func yesButtonClicked(_ sender: UIButton) { }
    
    func getETSetupViewController(isFromForgottenPasswordFlow: Bool) -> EvrotrustSetupViewController? {
        let pin = user?.pin ?? ""
        
        if let viewController: EvrotrustSetupViewController = (Evrotrust.sdk()?.createEvrotrustSetupViewController()) {
            viewController.isActingAsRegistrationAuthority = false
            viewController.shouldSkipContactInformation = true
            viewController.securityContext = isFromForgottenPasswordFlow ? user?.securityContext : pin.getHashedPassword
            
            if isFromForgottenPasswordFlow == false {
                let userInformation: EvrotrustUserInformation = EvrotrustUserInformation()
                userInformation.userDataType = EvrotrustUserType.identificationNumber
                userInformation.userDataValue = user?.personalIdentificationNumber
                userInformation.countryCode3 = AppConfig.Evrotrust.CountryCode3.bulgaria
                viewController.userInformationForCheck = userInformation
            }

            return viewController
        }
        
        return nil
    }
    
    func getETDocumentView(transactionId: String) -> EvrotrustOpenDocumentViewController? {
        if let documentViewController = Evrotrust.sdk()?.createEvrotrustOpenDocumentViewController() {
            documentViewController.securityContext = user?.securityContext
            documentViewController.transactionID = transactionId
            documentViewController.isSingleDocument = true
            
            return documentViewController
        }
        
        return nil
    }
    
    private func setupUI() {
        titleLabel = UILabel()
        detailLabel = UILabel()
        noButton = AdaptableSizeButton()
        yesButton = AdaptableSizeButton()
        loadingView = LottieLoadingUIView(frame: view.bounds)
    }
    
    private func getButtonConfiguration(title: String, titleColor: UIColor, backgroundColor: UIColor? = nil, cornerRadius: CGFloat? = nil) -> UIButton.Configuration {
        var configuration = UIButton.Configuration.filled()
        let inset = AppConfig.Dimensions.Padding.medium
        configuration.contentInsets = NSDirectionalEdgeInsets(top: inset, leading: inset, bottom: inset, trailing: inset)
        configuration.attributedTitle = AttributedString(title, attributes: AttributeContainer([NSAttributedString.Key.font:buttonFont]))
        configuration.baseForegroundColor = titleColor
        configuration.baseBackgroundColor = backgroundColor ?? .clear
        configuration.background.cornerRadius = cornerRadius ?? 0
        return configuration
    }
}
