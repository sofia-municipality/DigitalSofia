//
//  LoadingView.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 28.04.23.
//

import SwiftUI
import Lottie

private struct LoadingView: View {
    var body: some View {
        ZStack {
            Rectangle()
                .fill(.black)
                .ignoresSafeArea()
                .opacity(0.5)
            
            LoadingLottieView(loopMode: .loop)
                .scaleEffect(0.2)
        }
    }
}

#Preview {
    LoadingView()
}

struct LoadingStack<T: View>: View {
    @Binding var isPresented: Bool
    var content: T
    
    init(isPresented: Binding<Bool>, @ViewBuilder content: () -> T) {
        self._isPresented = isPresented
        self.content = content()
    }
    
    var body: some View {
        ZStack(alignment: .topTrailing) {
            content.disabled(isPresented)
            
            if isPresented == true {
                LoadingView()
            }
        }
    }
}
