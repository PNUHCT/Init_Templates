package neoguri.springTemplate.exception.dto;

import lombok.Getter;
import neoguri.springTemplate.exception.exceptionCode.ExceptionCode;

public class BusinessLogicException extends RuntimeException {

    @Getter
    private ExceptionCode exceptionCode;

    public BusinessLogicException(ExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
    }
}
