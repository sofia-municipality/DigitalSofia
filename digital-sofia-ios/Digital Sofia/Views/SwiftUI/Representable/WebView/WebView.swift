//
//  WebView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 27.04.23.
//

import SwiftUI
import WebKit

struct WebView: UIViewRepresentable {
    @Binding var shouldRefresh: Bool
    var request: URLRequest?
    var shouldAddAuth = false
    var navigationState: WebViewNavigationState
    
    func makeUIView(context: UIViewRepresentableContext<WebView>) -> WKWebView {
        let webview = navigationState.webView
        webview.shouldAddAuth = shouldAddAuth
        webview.isOpaque = false
        webview.backgroundColor = DSColors.background.uiColor
        webview.navigationDelegate = navigationState
        webview.setInspectable()
        
        loadRequest()
        return webview
    }
    
    func updateUIView(_ uiView: WKWebView, context: UIViewRepresentableContext<WebView>) {
        if shouldRefresh {
            loadRequest()
        }
    }
    
    fileprivate func loadRequest() {
        if let request = request {
            let _ = navigationState.webView.load(request)
        }
    }
}
