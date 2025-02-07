//
//  DSWebView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 11.12.23.
//

import SwiftUI

enum DSWebViewType {
    case services, myServices, faq, contacts, privacyPolicy
    
    var urlString: String {
        switch self {
        case .services:
            return AppConfig.WebViewPages.services.digitallWebLink
        case .myServices:
            return AppConfig.WebViewPages.myServices.digitallWebLink
        case .faq:
            return AppConfig.WebViewPages.faq.digitallWebLink
        case .contacts:
            return AppConfig.WebViewPages.contacts.digitallWebLink
        case .privacyPolicy:
            return AppConfig.WebViewPages.privacyPolicy.digitallWebLink
        }
    }
}

struct DSWebView: View {
    @EnvironmentObject var appState: AppState
    @EnvironmentObject var networkMonitor: NetworkMonitor
    var type: DSWebViewType
    
    @State private var request: URLRequest?
    @State private var shouldRefresh = false
    @StateObject private var navigationState = WebViewNavigationState()
    @State private var paymentUrl: PaymentUrl?
    
    init(type: DSWebViewType) {
        self.type = type
    }
    
    var body: some View {
        ZStack {
            DSColors.background
            
            NetworkStack {
                WebView(shouldRefresh: $shouldRefresh, request: request, navigationState: navigationState)
            }
            .environmentObject(networkMonitor)
        }
        .onAppear {
            if let topVC = UIApplication.topMostViewController() {
                if !String(describing: topVC).contains("CAMImagePickerCameraViewController") {
                    loadUrl()
                }
            } else {
                loadUrl()
            }
        }
        .onChange(of: appState.language) { newValue in
            loadUrl()
        }
        .onChange(of: navigationState.downloadComplete) { newValue in
            if newValue == true {
                appState.alertItem = AlertProvider.errorAlert(message: AppConfig.UI.Alert.successfullyDownloadedWebviewFileAlertText.localized)
            }
        }
        .onChange(of: navigationState.downloadError) { newValue in
            appState.alertItem = AlertProvider.generalAlert()
        }
        .onTokenRefresh(perform: {
            loadUrl()
        })
        .environmentObject(appState)
        .navigationBarHidden(true)
        .onAppear {
            navigationState.paymentStarted = { url in handlePayment(to: url)}
        }
        .sheet(item: $paymentUrl, onDismiss: {
            loadUrl()
        }, content: { item in
            PaymentWebView(paymentUrl: item)
                .environmentObject(networkMonitor)
        })
    }
    
    private func loadUrl() {
        if let url = URL(string: type.urlString) {
            request = URLRequest(url: url)
            shouldRefresh = true
        }
    }
    
    private func handlePayment(to url: URL?) {
        paymentUrl = PaymentUrl(url)
    }
}
