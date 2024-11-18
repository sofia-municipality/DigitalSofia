//
//  PaymentWebView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 19.07.24.
//

import SwiftUI

class PaymentUrl: Identifiable {
    let url: URL?
    init(_ url: URL?) { self.url = url }
}

struct PaymentWebView: View {
    @EnvironmentObject var networkMonitor: NetworkMonitor
    @State var paymentUrl: PaymentUrl
    @State private var request: URLRequest?
    @State private var shouldRefresh = false
    @StateObject private var navigationState = WebViewNavigationState()
    @Environment(\.dismiss) private var dismiss
    
    init(paymentUrl: PaymentUrl?) {
        self.paymentUrl = paymentUrl ?? PaymentUrl(URL.defaultURL)
    }
    
    var body: some View {
        NavigationView {
            ZStack {
                DSColors.background
                
                NetworkStack {
                    WebView(shouldRefresh: $shouldRefresh, request: request, navigationState: navigationState)
                }
                .environmentObject(networkMonitor)
                .padding([.top], AppConfig.Dimensions.Padding.XXXL)
            }
            .navigationBarItems(
                trailing: Button(action: dismiss.callAsFunction) {
                    ExitButtonView()
                }.frame(width: 32, height: 32)
            )
        }
        .onAppear {
            loadUrl()
        }
    }
    
    private func loadUrl() {
        if let url = paymentUrl.url {
            request = URLRequest(url: url)
            shouldRefresh = true
        }
    }
}
