//
//  UIApplication+Extension.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 13.12.23.
//

import UIKit

extension UIApplication {
    var currentWindow: UIWindow? {
        connectedScenes
            .filter({$0.activationState == .foregroundActive})
            .map({$0 as? UIWindowScene})
            .compactMap({$0})
            .first?.windows
            .filter({$0.isKeyWindow}).first
    }
    
    class func topMostViewController(base: UIViewController? = UIApplication.shared.currentWindow?.rootViewController) -> UIViewController? {
        
        if let nav = base as? UINavigationController {
            return topMostViewController(base: nav.visibleViewController)
        }
        
        if let tab = base as? UITabBarController {
            let moreNavigationController = tab.moreNavigationController
            
            if let top = moreNavigationController.topViewController, top.view.window != nil {
                return topMostViewController(base: top)
            } else if let selected = tab.selectedViewController {
                return topMostViewController(base: selected)
            }
        }
        if let presented = base?.presentedViewController {
            return topMostViewController(base: presented)
        }
        return base
    }
}
