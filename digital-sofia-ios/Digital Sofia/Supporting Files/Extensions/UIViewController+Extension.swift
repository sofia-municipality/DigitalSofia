//
//  UIViewController+Extension.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 4.04.23.
//

import SwiftUI
import UIKit

extension UIViewController {
    func addSwiftUI(someView: some View) {
        let vc = UIHostingController(rootView: someView)
        
        if let swiftuiView = vc.view {
            swiftuiView.translatesAutoresizingMaskIntoConstraints = false
            
            addChild(vc)
            view.addSubview(swiftuiView)
            
            NSLayoutConstraint.activate([
                swiftuiView.centerXAnchor.constraint(equalTo: view.centerXAnchor),
                swiftuiView.centerYAnchor.constraint(equalTo: view.centerYAnchor),
                swiftuiView.heightAnchor.constraint(equalToConstant: UIScreen.main.bounds.height),
                swiftuiView.widthAnchor.constraint(equalToConstant: UIScreen.main.bounds.width)
            ])
            
            vc.didMove(toParent: self)
        }
    }
    
    func removeSwiftUIView(name: String) {
        for view in view.subviews {
            if view.className.contains(name) {
                view.removeFromSuperview()
            }
        }
    }
}

extension UIViewController {
    var appDelegate: AppDelegate? {
        return UIApplication.shared.delegate as? AppDelegate
    }
}
