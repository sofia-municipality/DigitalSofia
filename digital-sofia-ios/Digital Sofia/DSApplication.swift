//
//  DSApplication.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 22.11.23.
//

import UIKit

class DSApplication: UIApplication {
    var user: User? {
        return UserProvider.currentUser
    }
    
    override func sendEvent(_ event: UIEvent) {
        super.sendEvent(event)
        if let touches = event.allTouches {
            for touch in touches where touch.phase == .began {
                if UserProvider.hasActiveUser {
                    if LockScreenHelper.shouldLockScreenOnTouch {
                        LockScreenHelper.removeLastTouchTime()
                        
                        let vc = UIApplication.topMostViewController()
                        let loginView = vc?.view.subviews.filter({ $0.className.contains(String(describing: LoginWithBiometricsView.self)) == true || $0.className.contains(String(describing: LoginPINView.self)) == true }).first
                        
                        if loginView == nil {
                            NotificationCenter.default.post(name: NSNotification.Name.lockScreenNotification,
                                                            object: nil,
                                                            userInfo: [:])
                        }
                    } else {
                        LockScreenHelper.saveLastTouchTime()
                    }
                }
            }
        }
    }
}
