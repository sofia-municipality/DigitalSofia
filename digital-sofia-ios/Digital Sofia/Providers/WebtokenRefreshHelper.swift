//
//  WebtokenRefreshHelper.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 12.12.23.
//

import Foundation

final class WebtokenRefreshHelper {
    private init() { }
    private var timer: Timer?
    
    static let shared = WebtokenRefreshHelper()
    deinit {
        invalidateTimers()
    }
    
    func startTimer(with tokenInfo: TokenInfo?) {
        if timer != nil {
            invalidateTimers()
        }
        
        scheduleTimer(with: tokenInfo)
    }
    
    func invalidateTimers() {
        timer?.invalidate()
        timer = nil
    }
    
    @objc private func timerExpiredHandler() {
        UserProvider.shared.login { _ in }
    }
    
    private func scheduleTimer(with tokenInfo: TokenInfo?) {
        let dateFormat = DateFormat.tokenFormat
        
        if let tokenDate = UserProvider.currentUser?.tokenExpireDateString?.dateFor(format: dateFormat) {
            let now = Date.getFormattedNow(format: .tokenFormat)
            let tokenTime = tokenDate - now
            
            timer = Timer.scheduledTimer(timeInterval: tokenTime,
                                         target: self,
                                         selector: #selector(timerExpiredHandler),
                                         userInfo: nil,
                                         repeats: false)
        }
    }
}
