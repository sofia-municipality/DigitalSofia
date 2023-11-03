//
//  PDFWebViewer.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 10.08.23.
//

import SwiftUI

struct PDFWebViewer: View {
    @State private var shouldRefresh = false
    @State private var url: URL?
    
    init(url: URL?) {
        _url = State(initialValue: url)
    }
    
    var body: some View {
        let webView = WebView(url: $url, shouldRefresh: $shouldRefresh, shouldAddAuth: true)
        
        VStack(spacing: 0) {
            CustomNavigationBar()
            
            Rectangle()
                .fill(DSColors.Text.placeholder.opacity(0.1))
                .frame(height: 1)
                .edgesIgnoringSafeArea(.horizontal)
            
            webView
        }
        .navigationBarHidden(true)
        .background(DSColors.background)
    }
}

struct PDFWebViewer_Previews: PreviewProvider {
    static var previews: some View {
        PDFWebViewer(url: nil)
    }
}
