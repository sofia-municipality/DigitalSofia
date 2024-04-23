//
//  LockScreenHelper.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 17.11.23.
//

import Foundation
import SwiftUI

final class LockScreenHelper {
    enum LockScreenMinutes: Int {
        case background = 2, inApp = 5
    }
    
    static func saveEnterBackgroundTime() {
        UserDefaults.standard.set(Date().getFormattedDate(format: .ddMMYYHHmm), forKey: AppConfig.UserDefaultsKeys.enterBackgroundTime)
    }
    
    static var shouldLockScreenOnEnterForeround: Bool {
        return checkTimeExpires(type: .background)
    }
    
    static func removeEnterBackgroundTime() {
        UserDefaults.standard.removeObject(forKey: AppConfig.UserDefaultsKeys.enterBackgroundTime)
    }
    
    static func saveLastTouchTime() {
        UserDefaults.standard.set(Date().getFormattedDate(format: .ddMMYYHHmm), forKey: AppConfig.UserDefaultsKeys.lastTouchTime)
    }
    
    static var shouldLockScreenOnTouch: Bool {
        return checkTimeExpires(type: .inApp)
    }
    
    static func removeLastTouchTime() {
        UserDefaults.standard.removeObject(forKey: AppConfig.UserDefaultsKeys.lastTouchTime)
    }
    
    private static func checkTimeExpires(type: LockScreenMinutes) -> Bool {
        var key = ""
        
        switch type {
        case .background:
            key = AppConfig.UserDefaultsKeys.enterBackgroundTime
        case .inApp:
            key = AppConfig.UserDefaultsKeys.lastTouchTime
        }
        
        if let dateString = UserDefaults.standard.string(forKey: key) {
            if let date = dateString.dateFor(format: .ddMMYYHHmm) {
                let now = Date.getFormattedNow(format: .ddMMYYHHmm)
                
                if now.hasSame(.day, as: date) {
                    let minutes = date.getMinutesDifferenceFromNow()
                    return minutes >= type.rawValue
                }
            }
        }
        
        return false
    }
}
