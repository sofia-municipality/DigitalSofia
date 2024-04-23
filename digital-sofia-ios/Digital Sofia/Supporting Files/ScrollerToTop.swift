//
//  ScrollerToTop.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 7.02.24.
//

import SwiftUI

struct ScrollerToTop: View {
    let reader: ScrollViewProxy
    @Binding var scrollOnChange: Bool
    
    var body: some View {
        EmptyView()
            .id("topScrollPoint")
            .onChange(of: scrollOnChange) { _ in
                withAnimation {
                    reader.scrollTo("topScrollPoint", anchor: .bottom)
                }
            }
    }
}
