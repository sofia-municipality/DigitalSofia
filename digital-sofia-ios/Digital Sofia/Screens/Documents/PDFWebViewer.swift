//
//  PDFWebViewer.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 10.08.23.
//

import SwiftUI

struct PDFWebViewer: View {
    @EnvironmentObject var appState: AppState
    
    @State private var shouldRefresh = false
    @State private var url: URL?
    @State private var request: URLRequest?
    @StateObject private var navigationState = WebViewNavigationState()
    
    init(url: URL?) {
        _url = State(initialValue: url)
        if let url = _url.wrappedValue {
            _request = State(initialValue: URLRequest(url: url))
        }
    }
    
    var body: some View {
        VStack(spacing: 0) {
            CustomNavigationBar()
            
            Rectangle()
                .fill(DSColors.Text.placeholder.opacity(0.1))
                .frame(height: 1)
                .edgesIgnoringSafeArea(.horizontal)
            
            WebView(shouldRefresh: $shouldRefresh, request: request, shouldAddAuth: true, navigationState: navigationState)
        }
        .onAppear {
            loadUrl()
        }
        .onTokenRefresh(perform: {
            DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
                shouldRefresh = false
                loadUrl()
            }
        })
        .environmentObject(appState)
        .backgroundAndNavigation()
        .log(view: self)
    }
    
    private func loadUrl() {
        if let url = url {
            request = URLRequest(url: url)
            shouldRefresh = true
        }
    }
}

struct PDFWebViewer_Previews: PreviewProvider {
    static var previews: some View {
        PDFWebViewer(url: nil)
    }
}
