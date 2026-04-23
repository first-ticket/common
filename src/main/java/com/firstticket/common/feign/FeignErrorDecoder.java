package com.firstticket.common.feign;

import com.firstticket.common.exception.BusinessException;
import com.firstticket.common.response.CommonErrorCode;
import feign.Response;
import feign.codec.ErrorDecoder;

public class FeignErrorDecoder implements ErrorDecoder {
    
    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 400 -> new BusinessException(CommonErrorCode.FEIGN_BAD_REQUEST);
            case 401 -> new BusinessException(CommonErrorCode.FEIGN_UNAUTHORIZED);
            case 403 -> new BusinessException(CommonErrorCode.FEIGN_FORBIDDEN);
            case 404 -> new BusinessException(CommonErrorCode.FEIGN_NOT_FOUND);
            default -> new BusinessException(CommonErrorCode.FEIGN_SERVER_ERROR);
        };
    }
}
