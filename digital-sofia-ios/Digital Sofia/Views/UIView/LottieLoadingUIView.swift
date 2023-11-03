//
//  LottieLoadingUIView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 13.10.23.
//

import UIKit
import Lottie

class LottieLoadingUIView: UIView {
    private var lottieAnimationView: LottieAnimationView? {
        didSet {
            lottieAnimationView?.loopMode = .loop
            lottieAnimationView?.contentMode = .scaleAspectFit
            lottieAnimationView?.play()
            addSubview(lottieAnimationView!)
        }
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        setupView()
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        
        setupView()
    }
    
    private func setupView() {
        backgroundColor = UIColor.black.withAlphaComponent(0.5)
        
        lottieAnimationView = LottieAnimationView(name: "loading")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        lottieAnimationView?.frame = bounds
        lottieAnimationView?.transform = CGAffineTransform(scaleX: 0.2, y: 0.2)
    }
}
