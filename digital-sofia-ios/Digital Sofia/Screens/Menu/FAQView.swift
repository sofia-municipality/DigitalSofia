//
//  FAQView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 1.08.23.
//

import SwiftUI

struct FAQView: View {
    @EnvironmentObject var networkMonitor: NetworkMonitor
    @EnvironmentObject var appState: AppState
    @State private var shouldRefresh = false
    @State private var url: URL?
    
    var body: some View {
        let webView = WebView(url: $url, shouldRefresh: $shouldRefresh)
        
        VStack(spacing: 0) {
            CustomNavigationBar()
            
            Rectangle()
                .fill(DSColors.Text.placeholder.opacity(0.1))
                .frame(height: 1)
                .edgesIgnoringSafeArea(.horizontal)
            
            if networkMonitor.isConnected {
                webView
            } else {
                NoNetworkConnetionView {
                    shouldRefresh = true
                }
            }
        }
        .onAppear {
            url = URL(string: AppConfig.WebViewPages.faq.digitallWebLink)
            shouldRefresh = true
        }
        .onChange(of: appState.language) { newValue in
            url = URL(string: AppConfig.WebViewPages.faq.digitallWebLink)
            shouldRefresh = true
        }
        .navigationBarHidden(true)
        .background(DSColors.background)
    }
}

struct FAQView_Previews: PreviewProvider {
    static var previews: some View {
        FAQView()
    }
}
