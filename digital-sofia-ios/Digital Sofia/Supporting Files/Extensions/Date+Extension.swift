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
}

enum DateFormat: String {
    case ddMMyyyy = "dd.MM.yyyy"
    case HHmm = "HH:mm"
    case ZZZ = "ZZZ"
}
