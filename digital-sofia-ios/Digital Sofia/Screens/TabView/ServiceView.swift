//
//  ServiceView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 7.06.23.
//

import SwiftUI

struct ServiceView: View {
    @EnvironmentObject var appState: AppState
    @EnvironmentObject var networkMonitor: NetworkMonitor
    @State private var shouldRefresh = false
    @State private var url: URL?
    
    var body: some View {
        let webView = WebView(url: $url, shouldRefresh: $shouldRefresh)
        
        ZStack {
            DSColors.background
            
            if networkMonitor.isConnected {
                webView
            } else {
                NoNetworkConnetionView {
                    shouldRefresh = true
                }
            }
        }
        .onAppear {
            url = URL(string: AppConfig.WebViewPages.services.digitallWebLink)
            shouldRefresh = true
        }
        .onChange(of: appState.language) { newValue in
            url = URL(string: AppConfig.WebViewPages.services.digitallWebLink)
            shouldRefresh = true
        }
    }
}

struct ServiceView_Previews: PreviewProvider {
    static var previews: some View {
        ServiceView()
    }
}
