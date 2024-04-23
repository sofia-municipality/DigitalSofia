//
//  BackgroundViewModifier.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 8.01.24.
//

import SwiftUI

struct BackgroundViewModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
            .background(DSColors.background)
            .navigationBarHidden(true)
    }
}

extension View {
    func backgroundAndNavigation() -> some View {
        self.modifier(BackgroundViewModifier())
    }
}
