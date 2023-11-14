//
//  PrivacyPolicyView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 7.06.23.
//

import SwiftUI

struct PrivacyPolicyView: View {
    @EnvironmentObject var networkMonitor: NetworkMonitor
    @EnvironmentObject var appState: AppState
    @State private var shouldRefresh = false
    @State private var url: URL?
    
    var body: some View {
        let webView = WebView(url: $url, shouldRefresh: $shouldRefresh)
        
        VStack(spacing: 0) {
            CustomNavigationBar()
            
            if networkMonitor.isConnected {
                webView
            } else {
                NoNetworkConnetionView {
                    shouldRefresh = true
                }
            }
        }
        .onAppear {
            url = URL(string: AppConfig.WebViewPages.privacyPolicy.digitallWebLink)
            shouldRefresh = true
        }
        .onChange(of: appState.language) { newValue in
            url = URL(string: AppConfig.WebViewPages.privacyPolicy.digitallWebLink)
            shouldRefresh = true
        }
        .navigationBarHidden(true)
        .background(DSColors.background)
    }
}

struct PrivacyPolicyView_Previews: PreviewProvider {
    static var previews: some View {
        PrivacyPolicyView()
    }
}
