//
//  Session+Extension.swift
//  Digital Sofia
//
//  Created by Teodora Georgieva on 4.04.23.
//

import Alamofire

extension Session {
    func initWith(service: ServiceProtocol, interceptor: RequestInterceptor) -> DataRequest {
        return AF.request(service.requestPath,
                          method: service.method.afMethod,
                          parameters: service.task.getParamDictionary(),
                          headers: .service(service.headers),
                          interceptor: interceptor)
    }

    func initDownloadWith(service: ServiceProtocol, interceptor: RequestInterceptor, to destination: DownloadRequest.Destination?) -> DownloadRequest {
        return AF.download(service.requestPath,
                           method: service.method.afMethod,
                           parameters: service.task.getParamDictionary(),
                           headers: .service(service.headers),
                           interceptor: interceptor, to: destination)
    }
}
