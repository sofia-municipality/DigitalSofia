//
//  KeyboardInactivityHandler.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 30.01.24.
//

import SwiftUI
import Combine

class KeyboardInactivityHandlerConfig: ObservableObject {
    @Published var startTimer: Bool = false
    @Published var stopTimer: Bool = false
    
    var inactivityDelay: Double = 2
    
    init(inactivityDelay: Double? = nil) {
        self.inactivityDelay = inactivityDelay ?? 1
    }
}

struct KeyboardInactivityHandler<T: View>: View {
    @EnvironmentObject var config: KeyboardInactivityHandlerConfig
    
    var content: T
    
    @State private var timer: Timer.TimerPublisher?
    @State private var timerSubscription: Cancellable?
    @State private var shouldHideKeyboard = false
    
    init(@ViewBuilder content: () -> T) {
        self.content = content()
    }
    
    var body: some View {
        ZStack(alignment: .topTrailing) {
            content
        }
        .onAppear {
            timer = Timer.publish(every: config.inactivityDelay, on: .main, in: .common)
        }
        .onChange(of: config.startTimer) { start in
            if start {
                startTimer()
            }
        }
        .onChange(of: config.stopTimer) { start in
            if start {
                invalidate()
            }
        }
        .onChange(of: shouldHideKeyboard) { newValue in
            if newValue {
                hideKeyboard()
                shouldHideKeyboard = false
            }
        }
    }
    
    private func startTimer() {
        timerSubscription = timer?.autoconnect().sink(receiveValue: { _ in
            shouldHideKeyboard = true
            resetConfig()
            invalidate()
        })
        
    }
    
    private func invalidate() {
        if timerSubscription != nil {
            timerSubscription?.cancel()
            timerSubscription = nil
            resetConfig()
        }
    }
    
    private func resetConfig() {
        config.startTimer = false
        config.stopTimer = false
    }
}
