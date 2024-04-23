//
//  AlertProvider.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 28.04.23.
//

import SwiftUI

struct AlertProvider {
    static let okButtonTitle = AppConfig.UI.Titles.Button.ok
    
    static func errorAlert(message: String) -> DismissAlertItem {
        return DismissAlertItem(title: Text(AppConfig.UI.Alert.generalAlertTitle.localized),
                                message: Text(message),
                                dismissButton: .default(Text(okButtonTitle)))
    }
    
    static func errorAlertWithCompletion(message: String, completion: @escaping () -> ()) -> DismissAlertItem {
        return DismissAlertItem(title: Text(AppConfig.UI.Alert.generalAlertTitle.localized),
                                message: Text(message),
                                dismissButton: .default(Text(okButtonTitle)) {
            completion()
        })
    }
    
    static func generalAlert() -> DismissAlertItem {
        return DismissAlertItem(title: Text(AppConfig.UI.Alert.generalAlertTitle.localized),
                                message: Text(AppConfig.UI.Text.somethingWentWrongErrorText.localized),
                                dismissButton: .default(Text(okButtonTitle)))
    }
    
    static func generalAlertWithCompletion(completion: @escaping () -> ()) -> DismissAlertItem {
        return DismissAlertItem(title: Text(AppConfig.UI.Alert.generalAlertTitle.localized),
                                message: Text(AppConfig.UI.Text.somethingWentWrongErrorText.localized),
                                dismissButton: .default(Text(okButtonTitle)) {
            completion()
        })
    }
    
    static func mismatchPINAlert() -> DismissAlertItem {
        return DismissAlertItem(title: Text(AppConfig.UI.Alert.pinMismatchAlertTitle.localized),
                                message: Text(AppConfig.UI.Alert.pinMismatchAlertDetails.localized),
                                dismissButton: .default(Text(okButtonTitle)))
    }
    
    static func wrongPINAlert(attempts: Int) -> DismissAlertItem {
        return DismissAlertItem(title: Text(AppConfig.UI.Alert.wrongPINAlertTitle.localized),
                                message: Text(AppConfig.UI.Alert.wrongPinAlertText.localized.format("\(attempts)")),
                                dismissButton: .default(Text(okButtonTitle)))
    }
    
    static func pinBlockAlert(message: String) -> DismissAlertItem {
        return DismissAlertItem(title: Text(AppConfig.UI.Alert.generalAlertTitle.localized),
                                message: Text(message),
                                dismissButton: .default(Text(okButtonTitle)))
    }
    
    static func successfullPINChange(dismissAction: @escaping () -> ()) -> DismissAlertItem {
        return DismissAlertItem(title: Text(AppConfig.UI.Alert.generalAlertTitle.localized),
                                message: Text(AppConfig.UI.Alert.successfullyChangedPinAlertText.localized),
                                dismissButton: .default(Text(okButtonTitle)) {
            dismissAction()
        })
    }
    
    static func getAlertFor(alertItem: BaseAlertItem) -> Alert {
        switch alertItem {
        case is DismissAlertItem:
            return (alertItem as? DismissAlertItem)?.alert ?? Alert.defaultAlert
        case is PrimaryAndSecondaryAlertItem:
            return (alertItem as? PrimaryAndSecondaryAlertItem)?.alert ?? Alert.defaultAlert
        default:
            return Alert.defaultAlert
        }
    }
}
