//
//  DocumentsNotificationHelper.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 22.01.24.
//

import Foundation

class DocumentsNotificationHelper {
    private init() { }
    static let shared = DocumentsNotificationHelper()
    
    private static var defaults: UserDefaults {
        return UserDefaults.standard
    }
    
    private static var serviceTab: HomeTabBarItemType {
        return .service
    }
    
    static func setTab(notification: NSNotification, appState: AppState) {
        var selectedTab = serviceTab
        
        switch notification.name {
        case NSNotification.Name.signedDocumentNotification,
            NSNotification.Name.expiredDocumentNotification:
            selectedTab = .docments
            
        case NSNotification.Name.pendingDocumentNotification,
            NSNotification.Name.pendingDeliveryDocumentNotification:
            selectedTab = .pendingDocuments
            appState.hasPendingDocuments = true
            
        default: break
        }
        
        appState.refreshDocuments = true
        defaults.set(selectedTab.rawValue, forKey: AppConfig.UserDefaultsKeys.selectedTab)
    }
    
    static func resetTab() {
        if defaults.integer(forKey: AppConfig.UserDefaultsKeys.selectedTab) != serviceTab.rawValue {
            defaults.set(serviceTab.rawValue, forKey: AppConfig.UserDefaultsKeys.selectedTab)
        }
    }
}
