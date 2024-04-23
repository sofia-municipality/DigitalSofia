//
//  TransitionFullScreenCover.swift
//  FullScreenCoverTest
//
//  Created by Korsun Yevhenii on 11.07.2022.
//

import SwiftUI

struct TransitionFullScreenCover<Content: View>: View {
    @Binding var isPresented: Bool
    @ViewBuilder var content: Content
    
    var body: some View {
        ZStack {
            content
        }
    }
}

extension View {
    func transitionFullScreenCover<Content>(isPresented: Binding<Bool>,
                                            transition: AnyTransition = .opacity,
                                            content: @escaping () -> Content) -> some View where Content : View {
        ZStack {
            self
            
            ZStack {
                if isPresented.wrappedValue {
                    TransitionFullScreenCover(isPresented: isPresented, content: content)
                        .transition(transition)
                }
            }
        }
    }
}
