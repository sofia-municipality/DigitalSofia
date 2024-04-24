//
//  LoggerModifier.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 30.01.24.
//

import SwiftUI

struct LoggerModifier: ViewModifier {
    var view: (any View)?
    
    func body(content: Content) -> some View {
        content
            .onAppear {
                LoggingHelper.logView(view: view!)
            }
    }
}

extension View {
    func log(view: any View) -> some View {
        self.modifier(LoggerModifier(view: view))
    }
}
