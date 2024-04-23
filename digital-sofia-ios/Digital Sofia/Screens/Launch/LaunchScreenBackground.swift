//
//  LaunchScreenBackground.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 31.07.23.
//

import SwiftUI

struct LaunchScreenBackground<Content>: View where Content: View {
    var navigationItem: AnyView?
    let content: Content
    
    var body : some View {
        ZStack {
            Image(ImageProvider.launchScreenSplashBg)
                .resizable()
                .aspectRatio(contentMode: .fill)
                .ignoresSafeArea()
            
            VStack {
                if let item = navigationItem {
                    item
                        .padding([.top], -40)
                        .ignoresSafeArea()
                }
                
                Image(ImageProvider.logo)
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: UIScreen.main.bounds.width * 0.5, height: UIScreen.main.bounds.width * 0.4)
                
                content
            }
            .padding([.top, .bottom], UIScreen.main.bounds.width * 0.25)
        }
        .navigationBarHidden(true)
        .navigationBarBackButtonHidden(true)
    }
}

struct LaunchScreenBackground_Previews: PreviewProvider {
    static var previews: some View {
        LaunchScreenBackground(content: EmptyView())
    }
}
