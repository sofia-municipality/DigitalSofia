//
//  PINBlockProvider.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 23.08.23.
//

import Foundation

final class PINBlockProvider {
    private init() { }
    
    enum PINBlockSeconds: Int {
        case min = 30, med = 300, max = 3600
    }
    
    static let shared = PINBlockProvider()
    
    func setBlockTime() {
        if UserDefaults.standard.object(forKey: AppConfig.UserDefaultsKeys.blockTime) == nil {
            switch blockLength {
            case 0:
                UserDefaults.standard.set(PINBlockSeconds.min.rawValue, forKey: AppConfig.UserDefaultsKeys.blockLength)
            case PINBlockSeconds.min.rawValue:
                UserDefaults.standard.set(PINBlockSeconds.med.rawValue, forKey: AppConfig.UserDefaultsKeys.blockLength)
            case PINBlockSeconds.med.rawValue:
                UserDefaults.standard.set(PINBlockSeconds.max.rawValue, forKey: AppConfig.UserDefaultsKeys.blockLength)
            default: break
            }
            
            UserDefaults.standard.set(Date(), forKey: AppConfig.UserDefaultsKeys.blockTime)
        }
    }
    
    private var blockLength: Int {
        return UserDefaults.standard.integer(forKey: AppConfig.UserDefaultsKeys.blockLength)
    }
    
    var blockAlertMessage: String {
        var message = ""
        let type = PINBlockSeconds(rawValue: blockLength)
        switch type {
        case .min:
            message = AppConfig.UI.Alert.BlockPin.secondsAlertText.localized.format(blockLength)
        case .med:
            message = AppConfig.UI.Alert.BlockPin.minutesAlertText.localized.format(blockLength.minutesFromSeconds)
        case .max:
            message = AppConfig.UI.Alert.BlockPin.hoursAlertText.localized.format(blockLength.hoursFromSeconds)
        default: break
        }
        
        return message
    }
    
    var isBlocked: Bool {
        if let startDate = UserDefaults.standard.object(forKey: AppConfig.UserDefaultsKeys.blockTime) as? Date {
            if let blockDate = Calendar.current.date(byAdding: .second, value: blockLength, to: startDate) {
                let isBlockedByDate = Date() < blockDate
                
                if isBlockedByDate == false {
                    UserDefaults.standard.removeObject(forKey: AppConfig.UserDefaultsKeys.blockTime)
                }
                
                return isBlockedByDate
            } else {
                return false
            }
        } else {
            return false
        }
    }
    
    func resetBlock() {
        UserDefaults.standard.removeObject(forKey: AppConfig.UserDefaultsKeys.blockTime)
        UserDefaults.standard.removeObject(forKey: AppConfig.UserDefaultsKeys.blockLength)
    }
}

extension Int {
    var minutesFromSeconds: Int {
        return self / 60
    }
    
    var hoursFromSeconds: Int {
        return self / 60 / 60
    }
}
