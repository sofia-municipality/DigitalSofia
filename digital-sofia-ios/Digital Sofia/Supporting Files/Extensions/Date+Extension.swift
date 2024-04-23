//
//  Date+Extension.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 26.04.23.
//

import Foundation

extension Date {
    static let iso8601Formatter: ISO8601DateFormatter = {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withFullDate,
                                   .withTime,
                                   .withDashSeparatorInDate,
                                   .withColonSeparatorInTime]
        return formatter
    }()
}

extension Date {
    func getFormattedDate(format: DateFormat) -> String {
        let dateformat = DateFormatter()
        dateformat.dateFormat = format.rawValue
        return dateformat.string(from: self)
    }
    
    static func getFormattedNow(format: DateFormat) -> Date {
        let dateformat = DateFormatter()
        dateformat.dateFormat = format.rawValue
        let stringDate = dateformat.string(from: Date())
        return stringDate.dateFor(format: format) ?? Date()
    }
}

enum DateFormat: String {
    case ddMMyyyy = "dd.MM.yyyy"
    case HHmm = "HH:mm"
    case ZZZ = "ZZZ"
    case ddMMYYHHmm = "dd/MM/yyyy HH:mm"
    case log = "dd-MM-yyyy-HH-mm-ss"
    case tokenFormat = "yyyy-MM-dd HH:mm:ss"
}

extension Date {
    func getMinutesDifferenceFromNow() -> Int {
        let calendar = Calendar.current
        let startDateTimeComponents = calendar.dateComponents([.hour, .minute], from: self)
        let endDateTimeComponents = calendar.dateComponents([.hour, .minute], from: Date())
        
        let difference = calendar.dateComponents([.minute], from: startDateTimeComponents, to: endDateTimeComponents).minute
        return difference ?? 0
    }
}

extension Date {
    func fullDistance(from date: Date, resultIn component: Calendar.Component, calendar: Calendar = .current) -> Int? {
        calendar.dateComponents([component], from: self, to: date).value(for: component)
    }
    
    func distance(from date: Date, only component: Calendar.Component, calendar: Calendar = .current) -> Int {
        let days1 = calendar.component(component, from: self)
        let days2 = calendar.component(component, from: date)
        return days1 - days2
    }
    
    func hasSame(_ component: Calendar.Component, as date: Date) -> Bool {
        distance(from: date, only: component) == 0
    }
}

extension Date {
    func adding(seconds: Int) -> Date {
        return Calendar.current.date(byAdding: .second, value: seconds, to: self)!
    }
}

extension Date {
    var utcTimestamp: String {
        let utcISODateFormatter = ISO8601DateFormatter()
        return utcISODateFormatter.string(from: self)
    }
}

extension Date {
    static func - (lhs: Date, rhs: Date) -> TimeInterval {
        return lhs.timeIntervalSinceReferenceDate - rhs.timeIntervalSinceReferenceDate
    }
}
