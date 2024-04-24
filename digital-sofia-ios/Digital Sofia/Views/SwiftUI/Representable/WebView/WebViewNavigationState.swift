//
//  NavigationState.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 7.02.24.
//

import SwiftUI
import WebKit

class WebViewNavigationState: NSObject, ObservableObject {
    @Published var downloadComplete: Bool?
    @Published var downloadError: WebViewDownloadError?
    
    let webView = CustomWebView()
}

extension WebViewNavigationState: WKNavigationDelegate {
    func webView(_ webView: WKWebView,
                 decidePolicyFor navigationAction: WKNavigationAction,
                 preferences: WKWebpagePreferences,
                 decisionHandler: @escaping (WKNavigationActionPolicy, WKWebpagePreferences) -> Void) {
        //        print(navigationAction.request.url?.absoluteString ?? "")
        
        if navigationAction.shouldPerformDownload {
            decisionHandler(.download, preferences)
        } else {
            decisionHandler(.allow, preferences)
        }
    }
    
    func webView(_ webView: WKWebView,
                 decidePolicyFor navigationResponse: WKNavigationResponse,
                 decisionHandler: @escaping (WKNavigationResponsePolicy) -> Void) {
        if navigationResponse.canShowMIMEType {
            decisionHandler(.allow)
        } else {
            decisionHandler(.download)
        }
    }
}

extension WebViewNavigationState: WKDownloadDelegate {
    func download(_ download: WKDownload,
                  decideDestinationUsing response: URLResponse,
                  suggestedFilename: String) async -> URL? {
        if let documentsDir = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true).first {
            downloadComplete = false
            downloadError = nil
            
            do {
                let items = try FileManager.default.contentsOfDirectory(atPath: documentsDir)
                let count = items.filter({ $0.contains(suggestedFilename.fileName) }).count
                let fileName = suggestedFilename.fileName  + (count == 0 ? "" : " (\(count))")
                let fullFileName = documentsDir + "/" + fileName + "." + suggestedFilename.fileExtension
                let url = URL(fileURLWithPath: fullFileName)
                return url
            } catch let error {
                let errorDescription = "delete old web view download error: \(error)"
                print(errorDescription)
                LoggingHelper.logGeneral(string: errorDescription)
                return nil
            }
        }
        
        return nil
    }
    
    func webView(_ webView: WKWebView, navigationAction: WKNavigationAction, didBecome download: WKDownload) {
        download.delegate = self
    }
    
    func downloadDidFinish(_ download: WKDownload) {
        downloadComplete = true
    }
    
    public func download(_ download: WKDownload, didFailWithError error: Error, resumeData: Data?) {
        let errorDescription = "webview download didFailWithError: \(error)"
        print(errorDescription)
        LoggingHelper.logGeneral(string: errorDescription)
        downloadError = WebViewDownloadError.message(error.localizedDescription)
    }
}
