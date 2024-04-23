//
//  LoadingLottieView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 10.10.23.
//

import Lottie
import SwiftUI

struct LoadingLottieView: UIViewRepresentable {
    let loopMode: LottieLoopMode
    
    func updateUIView(_ uiView: UIViewType, context: Context) { }
    
    func makeUIView(context: Context) -> Lottie.LottieAnimationView {
        let animationView = LottieAnimationView(name: "loading")
        animationView.play()
        animationView.loopMode = loopMode
        animationView.contentMode = .scaleAspectFit
        return animationView
    }
}
