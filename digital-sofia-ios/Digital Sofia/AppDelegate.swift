//
//  AppDelegate.swift
//  Digital Sofia
//
//  Created by Mehmed Kadir on 18.01.23.
//

import UIKit
import EvrotrustSDK
import FirebaseCore
import FirebaseMessaging
import Alamofire

//Изпращам линк за сваляне на SDK за iOS и Android с разрешени изпратените по-долу App IDs: https://we.tl/t-pGidKOqvkX
//Уникален номер на SDK: nDvKBf2Jb2nEVPmP
//
//Изпращам и тестови данни за API, които да използвате:
//Vendor №: fAqWz8DrTCRbdKQ7
//Vendor API key: 33c236eb-cbbe-46b3-93fb-g378b554g4g3

class AppDelegate: UIResponder, UIApplicationDelegate, EvrotrustSetupSDKDelegate {
    
    var window: UIWindow?
    var fcmToken: String?
    
    func evrotrustSetupSDKDidFinish(_ result: EvrotrustSetupSDKResult!) {
        switch result.status {
        case EvrotrustResultStatus.OK:
            print("is SDK setUP -> \(result.isSetUp)")
            print("is there a new version -> \(result.hasNewVersion)")
            print("is in maintenance -> \(result.isInMaintenance)")
            LoggingHelper.logSDKSetupResult(result)
            
            DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                NotificationCenter.default.post(name: NSNotification.Name.evrotrustSDKSetupReceivedNotification,
                                                object: nil,
                                                userInfo: [AppConfig.Notifications.UserInfoKeys.evrotrustSDKSetup: result.isSetUp])
            }
            
        default:
            break
        }
    }
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        if UserDefaults.standard.object(forKey: AppConfig.UserDefaultsKeys.forceRefreshKeychainUser) == nil {
            UserProvider.shared.updateKeychainUser()
            UserDefaults.standard.set(true, forKey: AppConfig.UserDefaultsKeys.forceRefreshKeychainUser)
        }
        
        if let filePath = Bundle.main.path(forResource: BuildConfiguration.plistName, ofType: "plist"),
           let fileopts = FirebaseOptions(contentsOfFile: filePath) {
            FirebaseApp.configure(options: fileopts)
        } else {
            FirebaseApp.configure()
        }
        
        LoggingHelper.logSDKSetupStart()
        Evrotrust.sdk().setupSDKWhitAppNumber(EvrotrustConfig.appNumber,
                                              environment: EvrotrustConfig.environment,
                                              andDelegate: self)
        
        Messaging.messaging().delegate = self
        UNUserNotificationCenter.current().delegate = self
        
        PermissionProvider.getNotificationsPermission()
        
        let standardAppearance = UITabBarAppearance()
        UITabBar.appearance().scrollEdgeAppearance = standardAppearance
        UITabBar.appearance().barTintColor = UIColor.white
        
        DocumentsNotificationHelper.resetTab()
        AF.sessionConfiguration.requestCachePolicy = .reloadIgnoringLocalAndRemoteCacheData
        
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
    func application(application: UIApplication,
                     didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
    }
    
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification) async -> UNNotificationPresentationOptions {
        let userInfo = notification.request.content.userInfo
        handleNotification(userInfo: userInfo)
        return [[.sound, .banner, .badge]]
    }
    
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse) async {
        let userInfo = response.notification.request.content.userInfo
        handleNotification(userInfo: userInfo)
    }
    
    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable : Any]) async -> UIBackgroundFetchResult {
        handleNotification(userInfo: userInfo)
        return UIBackgroundFetchResult.newData
    }
    
    private func handleNotification(userInfo: [AnyHashable : Any]) {
        print(userInfo as NSDictionary)
        
        if let data = try? JSONSerialization.data(withJSONObject: userInfo, options: .prettyPrinted) {
            do {
                if let loginPayload = try? JSONDecoder().decode(LoginCodeNotificationModel.self, from: data) {
                    FirebaseNotificationHelper.sendNotifiactionFor(type: .login, payload: loginPayload.code)
                } else if let documentPayload = try? JSONDecoder().decode(DocumentNotificationModel.self, from: data) {
                    var notificationType: FirebaseNotificationType = .pendingDocument
                    
                    switch documentPayload.docStatus {
                    case .signing:
                        notificationType = .pendingDocument
                    case .signed:
                        notificationType = .signedDocument
                    case .expired:
                        notificationType = .expiredDocument
                    default: break
                    }
                    
                    FirebaseNotificationHelper.sendNotifiactionFor(type: notificationType, payload: documentPayload.transactionId)
                } else if let payload = try? JSONDecoder().decode(FirebaseConsoleNotificationModel.self, from: data) {
                    print(payload)
                }
            }
        }
    }
}

extension AppDelegate: MessagingDelegate {
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        self.fcmToken = fcmToken
        print("Firebase registration token: \(fcmToken ?? "")")
        
        if let user = UserProvider.currentUser {
            if let fcm = user.firebaseToken {
                if fcm != fcmToken {
                    print("try to update keycloak fcm")
                    NetworkManager.changeFCM(completion: { response in
                        switch response {
                        case .success(_):
                            print("successfully updated fcm in keycloak")
                            UserProvider.shared.updateFirebaseToken()
                        default: break
                        }
                    })
                }
            } else {
                UserProvider.shared.updateFirebaseToken()
            }
        }
    }
}
