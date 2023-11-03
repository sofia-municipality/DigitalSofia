//
//  PINBlockProvider.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 23.08.23.
//

import Foundation

final class PINBlockProvider {
    /// TODO: change block interval
    enum PINBlockSeconds: Int {
        case min = 30, med = 60, max = 600 //3600
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
    
    var blockLength: Int {
        return UserDefaults.standard.integer(forKey: AppConfig.UserDefaultsKeys.blockLength)
    }
    
    var isBlocked: Bool {
        if let startDate = UserDefaults.standard.object(forKey: AppConfig.UserDefaultsKeys.blockTime) as? Date {
            if let blockDate = Calendar.current.date(byAdding: .second, value: blockLength, to: startDate) {
                let isBlockedByDate = Date() < blockDate
                
                if isBlockedByDate == false {
                    removeBlock()
                }
                
                return isBlockedByDate
            } else {
                return false
            }
        } else {
            return false
        }
    }
    
    func removeBlock() {
        UserDefaults.standard.removeObject(forKey: AppConfig.UserDefaultsKeys.blockTime)
    }
}
