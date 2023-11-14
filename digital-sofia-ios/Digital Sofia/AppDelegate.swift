//
//  AppDelegate.swift
//  Digital Sofia
//
//  Created by Mehmed Kadir on 18.01.23.
//

import UIKit
import EvrotrustSDK
import Firebase
import FirebaseMessaging

//Изпращам линк за сваляне на SDK за iOS и Android с разрешени изпратените по-долу App IDs: https://we.tl/t-pGidKOqvkX
//Уникален номер на SDK: nDvKBf2Jb2nEVPmP
//
//Изпращам и тестови данни за API, които да използвате:
//Vendor №: fAqWz8DrTCRbdKQ7
//Vendor API key: 33c236eb-cbbe-46b3-93fb-g378b554g4g3

@main
class AppDelegate: UIResponder, UIApplicationDelegate, EvrotrustSetupSDKDelegate {
    
    var window: UIWindow?
    let applicationNumber = "nDvKBf2Jb2nEVPmP"
    var fcmToken: String?
    
    func evrotrustSetupSDKDidFinish(_ result: EvrotrustSetupSDKResult!) {
        switch result.status {
        case EvrotrustResultStatus.OK:
            NotificationCenter.default.post(name: NSNotification.Name.evrotrustSDKSetupReceivedNotification,
                                            object: nil,
                                            userInfo: [AppConfig.Notifications.UserInfoKeys.evrotrustSDKSetup: result.isSetUp])
            
#if DEBUG
            print("is SDK setUP -> \(result.isSetUp)")
            print("is there a new version -> \(result.hasNewVersion)")
            print("is in maintenance -> \(result.isInMaintenance)")
#endif
        default:
            break
        }
    }
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Override point for customization after application launch.
        Evrotrust.sdk().setupSDKWhitAppNumber(applicationNumber, environment: EvrotrustEnvironment.test, andDelegate: self)
        FirebaseApp.configure()
        
        Messaging.messaging().delegate = self
        UNUserNotificationCenter.current().delegate = self
        
        let standardAppearance = UITabBarAppearance()
        UITabBar.appearance().scrollEdgeAppearance = standardAppearance
        UITabBar.appearance().barTintColor = UIColor.white
        
        PermissionProvider.getNotificationsPermission { success in
#if DEBUG
            print("notifications permitted: \(success)")
#endif
        }
        
        return true
    }
    
    // MARK: UISceneSession Lifecycle
    
    func application(_ application: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
        // Called when a new scene session is being created.
        // Use this method to select a configuration to create the new scene with.
        return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
    }
    
    func application(_ application: UIApplication, didDiscardSceneSessions sceneSessions: Set<UISceneSession>) {
        // Called when the user discards a scene session.
        // If any sessions were discarded while the application was not running, this will be called shortly after application:didFinishLaunchingWithOptions.
        // Use this method to release any resources that were specific to the discarded scenes, as they will not return.
    }
}

extension AppDelegate : UNUserNotificationCenterDelegate {
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification,
                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        let userInfo = notification.request.content.userInfo
        handleNotification(userInfo: userInfo)
        
        // Change this to your preferred presentation option
        completionHandler(UNNotificationPresentationOptions.banner)
    }
    
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse,
                                withCompletionHandler completionHandler: @escaping () -> Void) {
        let userInfo = response.notification.request.content.userInfo
        handleNotification(userInfo: userInfo)
        completionHandler()
    }
    
    private func handleNotification(userInfo: [AnyHashable : Any]) {
        NotificationCenter.default.post(name: NSNotification.Name.openDocumentNotification,
                                        object: nil,
                                        userInfo: [AppConfig.Notifications.UserInfoKeys.evrotrustTransactionId: ""])
        
        if let apsDict = userInfo["aps"] as? NSDictionary {
            if let alertInfo = apsDict["alert"] as? NSDictionary {
                if let data = try? JSONSerialization.data(withJSONObject: alertInfo, options: .prettyPrinted) {
                    do {
                        let customer = try JSONDecoder().decode(DocumentNotificationModel.self, from: data)
                        print(customer.body)
                    } catch {
                        print(error.localizedDescription)
                    }
                }
            }
        }
    }
}

extension AppDelegate: MessagingDelegate {
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        self.fcmToken = fcmToken
        
#if DEBUG
        print("Firebase registration token: \(fcmToken ?? "")")
#endif
    }
}
