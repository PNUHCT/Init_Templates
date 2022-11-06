package TemplateProject.Template.global.exception.dto;

import TemplateProject.Template.global.exception.exceptionCode.ExceptionCode;
import lombok.Getter;

public class BusinessLoginException extends RuntimeException{
    @Getter
    private ExceptionCode exceptionCode;

    public BusinessLoginException(ExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
    }
}
