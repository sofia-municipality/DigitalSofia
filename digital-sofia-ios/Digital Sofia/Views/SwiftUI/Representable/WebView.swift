//
//  WebView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 27.04.23.
//

import SwiftUI
import WebKit

struct WebView: UIViewRepresentable {
    @Binding var url: URL?
    @Binding var shouldRefresh: Bool
    var shouldAddAuth = false
    
    fileprivate func loadRequest(in webView: WKWebView) {
        if let url = url {
            webView.load(URLRequest(url: url))
        } else {
            if let googleUrl = URL(string: "https://google.com/") {
                webView.load(URLRequest(url: googleUrl))
            }
        }
    }
    
    func makeUIView(context: UIViewRepresentableContext<WebView>) -> WKWebView {
        let webview = CustomWebView()
        webview.shouldAddAuth = shouldAddAuth
        webview.isOpaque = false
        webview.backgroundColor = DSColors.background.uiColor
        
        loadRequest(in: webview)
        return webview
    }
    
    func updateUIView(_ uiView: WKWebView, context: UIViewRepresentableContext<WebView>) {
        if shouldRefresh {
            loadRequest(in: uiView)
        }
    }
}

class CustomWebView: WKWebView {
    var shouldAddAuth = false
    
    override func load(_ request: URLRequest) -> WKNavigation? {
        guard let mutableRequest: NSMutableURLRequest = request as? NSMutableURLRequest else {
            return super.load(request)
        }
        
        if shouldAddAuth {
            let token = UserProvider.shared.getUser()?.token ?? ""
            let bearerToken = "Bearer \(token)"
            
            mutableRequest.setValue(bearerToken, forHTTPHeaderField: "Authorization")
        }
        
        return super.load(mutableRequest as URLRequest)
    }
}
