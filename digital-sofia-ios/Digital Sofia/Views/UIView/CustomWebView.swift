//
//  CustomWebView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 11.12.23.
//

import WebKit

class CustomWebView: WKWebView {
    var shouldAddAuth = false
    
    override func load(_ request: URLRequest) -> WKNavigation? {
        guard let mutableRequest: NSMutableURLRequest = request as? NSMutableURLRequest else {
            return super.load(request)
        }
        
        if shouldAddAuth {
            let token = UserProvider.currentUser?.token ?? ""
            let bearerToken = "Bearer \(token)"
            
            mutableRequest.setValue(bearerToken, forHTTPHeaderField: "Authorization")
        }
        
        mutableRequest.cachePolicy = .reloadIgnoringLocalAndRemoteCacheData
        return super.load(mutableRequest as URLRequest)
    }
    
    func setInspectable() {
        let config = BuildConfiguration.getConfiguration()
        if config == .debug || config == .debugDev {
            perform(Selector(("setInspectable:")), with: true)
        }
    }
}
