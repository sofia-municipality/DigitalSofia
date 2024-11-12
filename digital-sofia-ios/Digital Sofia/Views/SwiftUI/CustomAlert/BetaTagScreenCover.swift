//
//  BetaTagScreenCover.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 5.04.24.
//

import SwiftUI

struct BetaTagScreenCover<Content: View>: View {
    @Binding var isPresented: Bool
    @ViewBuilder var content: Content
    
    var body: some View {
        content
            .transitionFullScreenCover(isPresented: $isPresented) {
                alertView()
            }
    }
    
    private func alertView() -> some View {
        ZStack {
            Color.white
                .opacity(0.1)
                .ignoresSafeArea()
            
            CustomAlertView(title: AppConfig.UI.Tag.title.localized,
                            message: Text(AppConfig.UI.Tag.info.localized) + Text("address@sofia.bg").underline())
        }
        .onTapGesture {
            withAnimation {
                isPresented.toggle()
            }
        }
        .background(BackgroundBlurView(blurStyle: UIBlurEffect(style: .light)))
        .ignoresSafeArea()
    }
}
