//
//  BackgroundBlurView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 5.04.24.
//

import SwiftUI

struct BackgroundBlurView: UIViewRepresentable {
    let blurStyle: UIBlurEffect
    
    func makeUIView(context: Context) -> UIView {
        let view = UIVisualEffectView(effect: blurStyle)
        DispatchQueue.main.async {
            view.superview?.superview?.backgroundColor = .clear
        }
        return view
    }
    
    func updateUIView(_ uiView: UIView, context: Context) {}
}
