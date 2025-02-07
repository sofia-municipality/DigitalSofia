//
//  PermissionProvider.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 4.04.23.
//

import UIKit

struct PermissionProvider {
    static func getNotificationsPermission(completion: ((Bool)->Void)? = nil) {
        let authOptions: UNAuthorizationOptions = [.alert, .badge, .sound]
        UNUserNotificationCenter.current().requestAuthorization(options: authOptions) { (success, error) in
            print("notifications permitted: \(success)")
            if success {
                DispatchQueue.main.async {
                    UIApplication.shared.registerForRemoteNotifications()
                }
            }
            completion?(success)
        }
    }
    
    static func getNotificationsStatus(completion: ((UNAuthorizationStatus)->Void)? = nil) {
        let current = UNUserNotificationCenter.current()
        current.getNotificationSettings(completionHandler: { (settings) in
            completion?(settings.authorizationStatus)
        })
    }
}
