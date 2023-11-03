//
//  ShareActivityView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 8.08.23.
//

import SwiftUI

struct ShareActivityView: UIViewControllerRepresentable {
    let items: [Any]
    
    func makeUIViewController(context: UIViewControllerRepresentableContext<ShareActivityView>) -> UIActivityViewController {
        return UIActivityViewController(activityItems: items, applicationActivities: nil)
    }
    
    func updateUIViewController(_ uiViewController: UIActivityViewController, context: UIViewControllerRepresentableContext<ShareActivityView>) {}
}
